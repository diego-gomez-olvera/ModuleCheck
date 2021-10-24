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

package modulecheck.core.rule.sort

import modulecheck.api.settings.ModuleCheckSettings
import modulecheck.core.parse
import modulecheck.core.rule.ModuleCheckRule
import modulecheck.parsing.PluginBlockParser
import modulecheck.parsing.PluginDeclaration
import modulecheck.parsing.Project2

class SortPluginsRule(
  override val settings: ModuleCheckSettings
) : ModuleCheckRule<SortPluginsFinding>() {

  override val id = "SortPlugins"
  override val description =
    "Sorts Gradle plugins which are applied using the plugins { ... } block"

  private val comparables: Array<(PluginDeclaration) -> Comparable<*>> =
    settings
      .sort
      .pluginComparators
      .map { it.toRegex() }
      .map { regex ->
        { str: String -> !str.matches(regex) }
      }
      .map { booleanLambda ->
        { dec: PluginDeclaration ->

          booleanLambda.invoke(dec.declarationText)
        }
      }.toTypedArray()

  @Suppress("SpreadOperator")
  private val comparator: Comparator<PluginDeclaration> = compareBy(*comparables)

  override fun check(project: Project2): List<SortPluginsFinding> {
    val block = PluginBlockParser.parse(project.buildFile) ?: return emptyList()

    val sortedPlugins = block.sortedPlugins(comparator)

    val sorted = block.contentString == sortedPlugins

    return if (sorted) {
      emptyList()
    } else {
      listOf(SortPluginsFinding(project.path, project.buildFile, comparator))
    }
  }
}
