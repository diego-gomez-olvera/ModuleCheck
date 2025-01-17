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

package modulecheck.core.kapt

import modulecheck.api.finding.Deletable
import modulecheck.api.finding.Finding
import modulecheck.api.finding.Finding.Position
import modulecheck.api.finding.Fixable
import modulecheck.api.finding.Problem
import modulecheck.api.finding.removeDependencyWithComment
import modulecheck.api.finding.removeDependencyWithDelete
import modulecheck.core.rule.KAPT_PLUGIN_FUN
import modulecheck.core.rule.KAPT_PLUGIN_ID
import modulecheck.parsing.gradle.Declaration
import modulecheck.project.McProject
import modulecheck.utils.LazyDeferred
import modulecheck.utils.lazyDeferred
import java.io.File

data class UnusedKaptPluginFinding(
  override val dependentProject: McProject,
  override val dependentPath: String,
  override val buildFile: File
) : Finding, Problem, Fixable, Deletable {

  override val message: String
    get() = "The `$KAPT_PLUGIN_ID` plugin dependency declared, " +
      "but no processor dependencies are declared."

  override val dependencyIdentifier = KAPT_PLUGIN_ID

  override val findingName = "unusedKaptPlugin"

  override val positionOrNull: LazyDeferred<Position?> = lazyDeferred {
    val text = buildFile
      .readText()

    val lines = text.lines()

    val row = lines
      .indexOfFirst { line ->
        line.contains("id(\"$KAPT_PLUGIN_ID\")") ||
          line.contains(KAPT_PLUGIN_FUN) ||
          line.contains("plugin = \"$KAPT_PLUGIN_ID\")")
      }

    if (row < 0) return@lazyDeferred null

    val col = lines[row]
      .indexOfFirst { it != ' ' }

    Position(row + 1, col + 1)
  }

  override val declarationOrNull: LazyDeferred<Declaration?> = lazyDeferred {

    sequenceOf(
      "id(\"$KAPT_PLUGIN_ID\")",
      "id \"$KAPT_PLUGIN_ID\"",
      "id '$KAPT_PLUGIN_ID'",
      KAPT_PLUGIN_FUN
    ).firstNotNullOfOrNull { id ->
      dependentProject.buildFileParser.pluginsBlock()?.getById(id)
    }
  }
  override val statementTextOrNull: LazyDeferred<String?> = lazyDeferred {
    declarationOrNull.await()?.statementWithSurroundingText
  }

  override suspend fun fix(): Boolean {

    val declaration = declarationOrNull.await() ?: return false

    dependentProject.removeDependencyWithComment(declaration, fixLabel())

    return true
  }

  override suspend fun delete(): Boolean {

    val declaration = declarationOrNull.await() ?: return false

    dependentProject.removeDependencyWithDelete(declaration)

    return true
  }
}
