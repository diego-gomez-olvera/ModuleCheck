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

import modulecheck.api.ConfigurationName
import modulecheck.api.Finding.Position
import modulecheck.api.Project2
import modulecheck.core.internal.positionIn
import modulecheck.psi.DslBlockVisitor
import java.io.File

data class InheritedImplementationDependencyFinding(
  override val dependentPath: String,
  override val buildFile: File,
  override val dependencyProject: Project2,
  val dependencyPath: String,
  override val configurationName: ConfigurationName,
  val from: Project2?
) : DependencyFinding("inheritedImplementation") {

  override val dependencyIdentifier = dependencyPath + " from: ${from?.path}"

  override fun positionOrNull(): Position? {
    return from?.positionIn(buildFile, configurationName)
  }

  override fun fix(): Boolean = synchronized(buildFile) {
    val visitor = DslBlockVisitor("dependencies")

    val fromPath = from?.path ?: return false

    val kotlinBuildFile = kotlinBuildFileOrNull() ?: return false

    val result = visitor.parse(kotlinBuildFile) ?: return false

    val match = result.elements.firstOrNull {
      val text = it.psiElement.text

      text.contains("\"$fromPath\"") && text.contains(configurationName.value)
    }
      ?.toString() ?: return false

    val newDeclaration = match.replaceFirst(fromPath, dependencyPath)

    // This won't match without .trimStart()
    val newDependencies = result.blockText.replaceFirst(
      oldValue = match.trimStart(),
      newValue = (newDeclaration + "\n" + match).trimStart()
    )

    val text = buildFile.readText()

    val newText = text.replaceFirst(result.blockText, newDependencies)

    buildFile.writeText(newText)

    return true
  }
}
