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

package modulecheck.api.context

import modulecheck.api.KaptProcessor
import modulecheck.project.ConfigurationName
import modulecheck.project.McProject
import modulecheck.project.ProjectContext
import modulecheck.project.ProjectContext.Element
import modulecheck.project.all
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

data class KaptDependencies(
  internal val delegate: ConcurrentMap<ConfigurationName, Set<KaptProcessor>>
) : ConcurrentMap<ConfigurationName, Set<KaptProcessor>> by delegate,
  Element {

  override val key: ProjectContext.Key<KaptDependencies>
    get() = Key

  companion object Key : ProjectContext.Key<KaptDependencies> {
    override suspend operator fun invoke(project: McProject): KaptDependencies {
      val map = project
        .configurations
        .filterNot { it.key.value.startsWith("_") }
        .filter { it.key.value.contains("kapt", true) }
        .mapValues { (configName, _) ->

          val external = project.externalDependencies[configName].orEmpty()
          val internal = project
            .projectDependencies
            .all()

          val allDependencies = external + internal

          allDependencies
            .filterNot { it.name == "org.jetbrains.kotlin:kotlin-annotation-processing-gradle" }
            .filter { it.configurationName == configName }
            .map { KaptProcessor(it.name) }
            .toSet()
        }

      return KaptDependencies(ConcurrentHashMap(map))
    }
  }
}

suspend fun ProjectContext.kaptDependencies(): KaptDependencies = get(KaptDependencies)
suspend fun ProjectContext.kaptDependenciesForConfig(
  configurationName: ConfigurationName
): Set<KaptProcessor> = kaptDependencies()[configurationName].orEmpty()
