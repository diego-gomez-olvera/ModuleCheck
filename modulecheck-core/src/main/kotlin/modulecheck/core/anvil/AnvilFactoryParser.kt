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

package modulecheck.core.anvil

import modulecheck.api.context.importsForSourceSetName
import modulecheck.api.context.jvmFilesForSourceSetName
import modulecheck.api.context.possibleReferencesForSourceSetName
import modulecheck.parsing.java.JavaFile
import modulecheck.parsing.psi.KotlinFile
import modulecheck.project.McProject
import modulecheck.project.SourceSetName
import modulecheck.utils.lazyDeferred
import net.swiftzer.semver.SemVer

object AnvilFactoryParser {

  private const val anvilMergeComponent = "com.squareup.anvil.annotations.MergeComponent"
  private const val daggerComponent = "dagger.Component"
  private const val daggerInject = "dagger.Inject"
  private const val daggerModule = "dagger.Module"

  @Suppress("MagicNumber")
  private val minimumAnvilVersion = SemVer(2, 0, 11)

  @Suppress("ComplexMethod")
  suspend fun parse(project: McProject): List<CouldUseAnvilFinding> {
    val anvil = project.anvilGradlePlugin ?: return emptyList()

    if (anvil.generateDaggerFactories) return emptyList()

    val anvilVersion = anvil.version

    val hasAnvil = anvilVersion >= minimumAnvilVersion

    if (!hasAnvil) return emptyList()

    val allImports = project.importsForSourceSetName(SourceSetName.MAIN) +
      project.importsForSourceSetName(SourceSetName.ANDROID_TEST) +
      project.importsForSourceSetName(SourceSetName.TEST)

    val maybeExtra = lazyDeferred {
      project.possibleReferencesForSourceSetName(SourceSetName.ANDROID_TEST) +
        project.possibleReferencesForSourceSetName(SourceSetName.MAIN) +
        project.possibleReferencesForSourceSetName(SourceSetName.TEST)
    }

    val createsComponent = allImports.contains(daggerComponent) ||
      allImports.contains(anvilMergeComponent) ||
      maybeExtra.await().contains(daggerComponent) ||
      maybeExtra.await().contains(anvilMergeComponent)

    if (createsComponent) return emptyList()

    val usesDaggerInJava = project
      .jvmFilesForSourceSetName(SourceSetName.MAIN)
      .filterIsInstance<JavaFile>()
      .any { file ->
        file.imports.contains(daggerInject) ||
          file.imports.contains(daggerModule) ||
          file.maybeExtraReferences.contains(daggerInject) ||
          file.maybeExtraReferences.contains(daggerModule)
      }

    if (usesDaggerInJava) return emptyList()

    val usesDaggerInKotlin = project
      .jvmFilesForSourceSetName(SourceSetName.MAIN)
      .filterIsInstance<KotlinFile>()
      .any { file ->
        file.imports.contains(daggerInject) ||
          file.imports.contains(daggerModule) ||
          file.maybeExtraReferences.contains(daggerInject) ||
          file.maybeExtraReferences.contains(daggerModule)
      }

    if (!usesDaggerInKotlin) return emptyList()

    val couldBeAnvil =
      !allImports.contains(daggerComponent) && !maybeExtra.await().contains(daggerComponent)

    return if (couldBeAnvil) {
      listOf(CouldUseAnvilFinding(project.buildFile, project.path))
    } else {
      listOf()
    }
  }
}
