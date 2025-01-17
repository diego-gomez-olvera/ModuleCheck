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

import modulecheck.api.rule.ModuleCheckRule
import modulecheck.api.settings.ChecksSettings
import modulecheck.api.settings.ModuleCheckSettings
import modulecheck.core.OverShotDependencyFinding
import modulecheck.core.context.overshotDependencies
import modulecheck.project.McProject

class OverShotDependencyRule(
  val settings: ModuleCheckSettings
) : ModuleCheckRule<OverShotDependencyFinding> {

  override val id = "OverShotDependency"
  override val description = "Finds project dependencies which aren't used by the declaring" +
    " configuration, but are used by a dependent configuration."

  override suspend fun check(project: McProject): List<OverShotDependencyFinding> {
    return project.overshotDependencies()
      .all()
      .filterNot { it.dependencyProject.path in settings.ignoreUnusedFinding }
      .sortedByDescending { it.configurationName }
  }

  override fun shouldApply(checksSettings: ChecksSettings): Boolean {
    return checksSettings.overShotDependency
  }
}
