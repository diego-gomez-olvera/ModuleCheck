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

package modulecheck.core

import modulecheck.api.finding.AddsDependency
import modulecheck.api.finding.ModifiesDependency
import modulecheck.api.finding.RemovesDependency
import modulecheck.parsing.gradle.ConfigurationName
import modulecheck.parsing.gradle.DependenciesBlock
import modulecheck.parsing.gradle.ModuleDependencyDeclaration
import modulecheck.project.ConfiguredProjectDependency
import modulecheck.project.McProject
import org.jetbrains.kotlin.util.prefixIfNot

data class OverShotDependencyFinding(
  override val dependentProject: McProject,
  override val newDependency: ConfiguredProjectDependency,
  override val oldDependency: ConfiguredProjectDependency,
  override val configurationName: ConfigurationName
) : AbstractProjectDependencyFinding("overshot"),
  ModifiesDependency,
  AddsDependency,
  RemovesDependency {

  override val dependencyProject get() = oldDependency.project
  override val dependencyIdentifier: String get() = newDependency.path

  override val message: String
    get() = "The dependency is not used in the source set for which it is configured, but it is " +
      "used in another source set which inherits from the first.  For example, a test-only " +
      "dependency which is declared via `implementation` instead of `testImplementation`."

  override suspend fun fix(): Boolean {

    val blocks = dependentProject.buildFileParser
      .dependenciesBlocks()

    val sourceDeclaration = blocks.firstNotNullOfOrNull { block ->

      block.getOrEmpty(dependencyProject.path, oldDependency.configurationName)
        .firstOrNull()
    } ?: return false

    val positionBlockDeclarationPair = blocks.firstNotNullOfOrNull { block ->

      val match = matchingDeclaration(block) ?: return@firstNotNullOfOrNull null

      block to match
    } ?: return false

    val (block, positionDeclaration) = positionBlockDeclarationPair

    val newDeclaration = sourceDeclaration.replace(
      configurationName, testFixtures = newDependency.isTestFixture
    )

    val oldStatement = positionDeclaration.statementWithSurroundingText
    val newStatement = oldStatement.plus(
      newDeclaration.statementWithSurroundingText
        .prefixIfNot("\n")
    )

    val newBlock = block.lambdaContent.replaceFirst(
      oldValue = oldStatement,
      newValue = newStatement
    )

    val fileText = buildFile.readText()
      .replace(block.lambdaContent, newBlock)

    buildFile.writeText(fileText)

    // dependencyProject.removeDependencyWithDelete(oldDependency)
    // dependencyProject.addDependency(newDependency)

    return true
  }

  private fun matchingDeclaration(block: DependenciesBlock) = block.settings
    .filterIsInstance<ModuleDependencyDeclaration>()
    .maxByOrNull { declaration -> declaration.configName == configurationName }
    ?: block.settings
      .filterNot { it is ModuleDependencyDeclaration }
      .maxByOrNull { declaration -> declaration.configName == configurationName }
    ?: block.settings
      .lastOrNull()

  override fun fromStringOrEmpty(): String = ""

  override fun toString(): String {
    return "OverShotDependency(\n" +
      "\tdependentPath='$dependentPath', \n" +
      "\tbuildFile=$buildFile, \n" +
      "\tdependencyProject=$dependencyProject, \n" +
      "\tdependencyIdentifier='$dependencyIdentifier', \n" +
      "\tconfigurationName=$configurationName\n" +
      ")"
  }
}
