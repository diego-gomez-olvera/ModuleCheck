/*
 * Copyright (C) 2021 Rick Busarow
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

import modulecheck.api.Finding
import modulecheck.api.Finding.LogElement
import modulecheck.api.Fixable
import modulecheck.api.positionOfStatement
import modulecheck.core.internal.statementOrNullIn
import modulecheck.parsing.ConfigurationName
import modulecheck.parsing.ModuleDependencyDeclaration
import modulecheck.parsing.Project2

abstract class DependencyFinding(
  override val problemName: String
) : Fixable,
  Finding {

  abstract val dependencyProject: Project2
  abstract val configurationName: ConfigurationName

  override val positionOrNull by lazy {
    val statement = statementTextOrNull ?: return@lazy null

    buildFile.readText()
      .positionOfStatement(statement)
  }

  override val statementOrNull: ModuleDependencyDeclaration? by lazy {
    dependencyProject
      .statementOrNullIn(buildFile, configurationName)
  }
  override val statementTextOrNull: String? by lazy {
    statementOrNull?.statementWithSurroundingText
  }

  override fun logElement(): LogElement {
    return LogElement(
      dependentPath = dependentPath,
      problemName = problemName,
      sourceOrNull = fromStringOrEmpty(),
      dependencyPath = dependencyProject.path,
      positionOrNull = positionOrNull,
      buildFile = buildFile
    )
  }

  abstract fun fromStringOrEmpty(): String

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is DependencyFinding) return false

    if (problemName != other.problemName) return false
    if (dependencyProject != other.dependencyProject) return false
    if (configurationName != other.configurationName) return false

    return true
  }

  override fun hashCode(): Int {
    var result = problemName.hashCode()
    result = 31 * result + dependencyProject.hashCode()
    result = 31 * result + configurationName.hashCode()
    return result
  }
}
