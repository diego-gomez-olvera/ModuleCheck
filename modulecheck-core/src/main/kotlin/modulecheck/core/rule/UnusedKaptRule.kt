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

package modulecheck.core.rule

import modulecheck.api.KaptMatcher
import modulecheck.api.asMap
import modulecheck.api.context.ReferenceName
import modulecheck.api.context.importsForSourceSetName
import modulecheck.api.context.kaptDependencies
import modulecheck.api.context.referencesForSourceSetName
import modulecheck.api.finding.Finding
import modulecheck.api.rule.ModuleCheckRule
import modulecheck.api.settings.ChecksSettings
import modulecheck.api.settings.ModuleCheckSettings
import modulecheck.core.kapt.UnusedKaptPluginFinding
import modulecheck.core.kapt.UnusedKaptProcessorFinding
import modulecheck.core.kapt.defaultKaptMatchers
import modulecheck.project.McProject

const val KAPT_PLUGIN_ID = "org.jetbrains.kotlin.kapt"
internal const val KAPT_PLUGIN_FUN = "kotlin(\"kapt\")"

class UnusedKaptRule(
  private val settings: ModuleCheckSettings
) : ModuleCheckRule<Finding> {

  private val kaptMatchers: List<KaptMatcher>
    get() = settings.additionalKaptMatchers + defaultKaptMatchers

  override val id = "UnusedKapt"
  override val description = "Finds unused kapt processor dependencies " +
    "and warns if the kapt plugin is applied but unused"

  override suspend fun check(project: McProject): List<Finding> {
    val matchers = kaptMatchers.asMap()

    val kaptDependencies = project.kaptDependencies()
    val allKaptDependencies = kaptDependencies.all()

    return project
      .configurations
      .keys
      .filter { it.value.startsWith("kapt") }
      .map { configName ->
        configName to project.importsForSourceSetName(configName.toSourceSetName()) +
          project.referencesForSourceSetName(configName.toSourceSetName())
      }
      .flatMap { (configurationName, imports) ->
        val processors = kaptDependencies.get(configurationName)

        // unused means that none of the processor's annotations are used in any import
        val unusedProcessors = processors
          .filter { matchers[it.name]?.matchedIn(imports) == false }

        val unusedProcessorFindings = unusedProcessors
          .map { processor ->
            UnusedKaptProcessorFinding(
              dependentProject = project,
              dependentPath = project.path,
              buildFile = project.buildFile,
              oldDependency = processor,
              configurationName = configurationName
            )
          }

        val pluginIsUnused = allKaptDependencies
          .size == unusedProcessorFindings.size && project.hasKapt && unusedProcessorFindings.isNotEmpty()

        if (pluginIsUnused) {
          unusedProcessorFindings + UnusedKaptPluginFinding(
            dependentProject = project,
            dependentPath = project.path,
            buildFile = project.buildFile
          )
        } else {
          unusedProcessorFindings
        }
      }
  }

  override fun shouldApply(checksSettings: ChecksSettings): Boolean {
    return checksSettings.unusedKapt
  }

  private fun KaptMatcher.matchedIn(
    imports: Set<ReferenceName>
  ): Boolean = annotationImports
    .map { it.toRegex() }
    .any { annotationRegex ->

      imports.any { import ->

        annotationRegex.matches(import)
      }
    }
}
