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

package modulecheck.api.rule

import modulecheck.api.finding.Finding
import modulecheck.api.settings.ChecksSettings
import modulecheck.api.settings.ModuleCheckSettings
import modulecheck.project.McProject

interface ModuleCheckRule<T : Finding> {

  val id: String
  val description: String

  suspend fun check(project: McProject): List<T>
  fun shouldApply(checksSettings: ChecksSettings): Boolean
}

interface ReportOnlyRule<T : Finding> : ModuleCheckRule<T>
interface SortRule<T : Finding> : ModuleCheckRule<T>

fun interface RuleFactory {

  fun create(settings: ModuleCheckSettings): List<ModuleCheckRule<out Finding>>
}
