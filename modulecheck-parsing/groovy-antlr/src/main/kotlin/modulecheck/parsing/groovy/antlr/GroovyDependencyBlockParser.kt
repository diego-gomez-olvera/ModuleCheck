/*
 * Copyright (C) 2021-2022 Rick Busarow
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("RegExpRedundantEscape")

package modulecheck.parsing.groovy.antlr

import groovyjarjarantlr4.v4.runtime.tree.RuleNode
import modulecheck.parsing.gradle.MavenCoordinates
import modulecheck.parsing.gradle.ModuleRef
import modulecheck.parsing.gradle.asConfigurationName
import org.apache.groovy.parser.antlr4.GroovyParser.BlockStatementContext
import org.apache.groovy.parser.antlr4.GroovyParser.ClosureContext
import org.apache.groovy.parser.antlr4.GroovyParser.ExpressionListElementContext
import org.apache.groovy.parser.antlr4.GroovyParser.NlsContext
import org.apache.groovy.parser.antlr4.GroovyParser.ScriptStatementContext
import org.apache.groovy.parser.antlr4.GroovyParser.SepContext
import org.apache.groovy.parser.antlr4.GroovyParser.StringLiteralContext
import org.apache.groovy.parser.antlr4.GroovyParserBaseVisitor
import javax.inject.Inject

class GroovyDependencyBlockParser @Inject constructor() {

  fun parse(file: String): List<GroovyDependenciesBlock> = parse(file) {
    val dependenciesBlocks = mutableListOf<GroovyDependenciesBlock>()

    val rawModuleNameVisitor = object : GroovyParserBaseVisitor<String?>() {

      override fun shouldVisitNextChild(
        node: RuleNode?,
        currentResult: String?
      ): Boolean = currentResult == null

      override fun visitStringLiteral(ctx: StringLiteralContext?): String? {
        return ctx?.originalText()?.replace("""["']""".toRegex(), "")
      }
    }

    // visits the config block which might follow a dependency declaration,
    // such as for `exclude` or a `reason`
    val closureVisitor = object : GroovyParserBaseVisitor<String?>() {

      override fun shouldVisitNextChild(
        node: RuleNode?,
        currentResult: String?
      ): Boolean = currentResult == null

      override fun visitClosure(ctx: ClosureContext?): String? {
        return ctx?.originalText()
      }
    }

    val projectDepVisitor = object : GroovyParserBaseVisitor<Pair<String, String>?>() {

      override fun shouldVisitNextChild(
        node: RuleNode?,
        currentResult: Pair<String, String>?
      ): Boolean = currentResult == null

      fun ExpressionListElementContext.configClosure() =
        children.firstNotNullOfOrNull { it.accept(closureVisitor) }

      override fun visitExpressionListElement(
        ctx: ExpressionListElementContext?
      ): Pair<String, String>? {

        // if the statement includes a config block (it isn't null), then delete it
        fun String.maybeRemove(token: String?): String {
          token ?: return this
          return replace(token, "").trimEnd()
        }

        return visitChildren(ctx) ?: when (ctx?.start?.text) {
          "projects" -> {
            val original = ctx.originalText()

            original.removePrefix("projects.").let { typeSafe ->
              // Groovy parsing includes any config closure in this context,
              // so it would be `projects.foo.bar { exclude ... }`
              // remove that config closure from the full module access since it's actually part
              // of the parent configuration statement
              original.maybeRemove(ctx.configClosure()) to typeSafe
            }
          }
          "project" -> {
            val original = ctx.originalText()

            // Groovy parsing includes any config closure in this context,
            // so it would be `project(':foo:bar') { exclude ... }`
            // remove that config closure from the full module access since it's actually part
            // of the parent configuration statement
            ctx.accept(rawModuleNameVisitor)?.let { name ->
              original.maybeRemove(ctx.configClosure()) to name
            }
          }
          else -> {
            null
          }
        }
      }
    }

    val unknownArgumentVisitor = object : GroovyParserBaseVisitor<String?>() {

      override fun shouldVisitNextChild(
        node: RuleNode?,
        currentResult: String?
      ): Boolean = currentResult == null

      override fun visitExpressionListElement(ctx: ExpressionListElementContext): String {
        return ctx.originalText()
      }
    }

    val visitor = object : GroovyParserBaseVisitor<Unit>() {

      var pendingBlockNoInspectionComment: String? = null

      override fun visitNls(ctx: NlsContext) {
        super.visitNls(ctx)

        pendingBlockNoInspectionComment = NO_INSPECTION_REGEX.find(ctx.text)
          ?.destructured
          ?.component1()
      }

      override fun visitScriptStatement(ctx: ScriptStatementContext?) {

        val statement = ctx?.statement()

        if (statement?.start?.text == "dependencies") {

          val originalBlockBody = statement.parentOfType<ScriptStatementContext>()
            ?.originalText()
            ?: return

          val blockBody = BLOCK_BODY_REGEX.find(originalBlockBody)
            ?.groupValues
            ?.get(1)
            ?.removePrefix("\n")
            ?: return

          val blockSuppressed = pendingBlockNoInspectionComment?.split(",")
            ?.map { it.trim() }
            .orEmpty()
          pendingBlockNoInspectionComment = null

          val dependenciesBlock = GroovyDependenciesBlock(
            fullText = statement.originalText(),
            lambdaContent = blockBody,
            suppressAll = blockSuppressed
          )

          super.visitScriptStatement(ctx)

          val blockStatementVisitor = object : GroovyParserBaseVisitor<Unit>() {

            var pendingNoInspectionComment: String? = null

            override fun visitSep(ctx: SepContext) {
              super.visitSep(ctx)

              pendingNoInspectionComment = NO_INSPECTION_REGEX.find(ctx.text)
                ?.destructured
                ?.component1()
            }

            override fun visitBlockStatement(ctx: BlockStatementContext) {

              val config = ctx.start.text

              val suppressed = pendingNoInspectionComment?.split(",")
                ?.map { it.trim() }
                .orEmpty()
              pendingNoInspectionComment = null

              val moduleNamePair = projectDepVisitor.visit(ctx)

              if (moduleNamePair != null) {
                val (moduleAccess, moduleRef) = moduleNamePair
                dependenciesBlock.addModuleStatement(
                  configName = config.asConfigurationName(),
                  parsedString = ctx.originalText(),
                  moduleRef = ModuleRef.from(moduleRef),
                  moduleAccess = moduleAccess,
                  suppressed = suppressed
                )
                return
              }

              val mavenCoordinates = rawModuleNameVisitor.visit(ctx)
                ?.let { MavenCoordinates.parseOrNull(it) }

              if (mavenCoordinates != null) {
                dependenciesBlock.addNonModuleStatement(
                  configName = config.asConfigurationName(),
                  parsedString = ctx.originalText(),
                  coordinates = mavenCoordinates,
                  suppressed = suppressed
                )
                return
              }

              val argument = unknownArgumentVisitor.visit(ctx) ?: return

              dependenciesBlock.addUnknownStatement(
                configName = config.asConfigurationName(),
                parsedString = ctx.originalText(),
                argument = argument,
                suppressed = suppressed
              )
            }
          }

          blockStatementVisitor.visit(ctx)

          dependenciesBlocks.add(dependenciesBlock)
        }
      }
    }

    parser.accept(visitor)

    return dependenciesBlocks
  }

  companion object {
    val BLOCK_BODY_REGEX = """dependencies\s*\{([\s\S]*)\}""".toRegex()
    val NO_INSPECTION_REGEX = """//noinspection \s*([\s\S]*)$""".toRegex()
  }
}
