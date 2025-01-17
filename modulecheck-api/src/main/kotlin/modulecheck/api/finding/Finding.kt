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

package modulecheck.api.finding

import modulecheck.parsing.gradle.ConfigurationName
import modulecheck.parsing.gradle.Declaration
import modulecheck.project.McProject
import modulecheck.utils.LazyDeferred
import java.io.File

interface Finding {

  val dependentProject: McProject

  val findingName: String

  val dependentPath: String
  val message: String
  val buildFile: File

  val positionOrNull: LazyDeferred<Position?>

  suspend fun toResult(fixed: Boolean): FindingResult {
    return FindingResult(
      dependentPath = dependentPath,
      problemName = findingName,
      sourceOrNull = null,
      dependencyPath = "",
      positionOrNull = positionOrNull.await(),
      buildFile = buildFile,
      message = message,
      fixed = fixed
    )
  }

  data class Position(
    val row: Int,
    val column: Int
  ) : Comparable<Position> {
    fun logString(): String = "($row, $column): "
    override fun compareTo(other: Position): Int {
      return row.compareTo(other.row)
    }
  }

  data class FindingResult(
    val dependentPath: String,
    val problemName: String,
    val sourceOrNull: String?,
    val dependencyPath: String,
    val positionOrNull: Position?,
    val buildFile: File,
    val message: String,
    val fixed: Boolean
  ) {
    val filePathString: String = "${buildFile.path}: ${positionOrNull?.logString().orEmpty()}"
  }
}

interface DependencyFinding {

  val declarationOrNull: LazyDeferred<Declaration?>
  val statementTextOrNull: LazyDeferred<String?>
}

interface ProjectDependencyFinding {
  val dependencyProject: McProject
  val configurationName: ConfigurationName
}
