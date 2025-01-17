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

package modulecheck.api.context

import kotlinx.coroutines.flow.toSet
import modulecheck.project.McProject
import modulecheck.project.ProjectContext
import modulecheck.utils.filterAsync

data class DependentProjects(
  private val delegate: Set<McProject>
) : Set<McProject> by delegate,
  ProjectContext.Element {

  override val key: ProjectContext.Key<DependentProjects>
    get() = Key

  companion object Key : ProjectContext.Key<DependentProjects> {
    override suspend operator fun invoke(project: McProject): DependentProjects {
      val others = project.projectCache
        .values
        .filterAsync { otherProject ->
          project.path in otherProject
            .classpathDependencies()
            .all()
            .map { it.contributed.project.path }
        }
        .toSet()

      return DependentProjects(others)
    }
  }
}

/**
 * All projects which are downstream of the receiver project, including those which only inherit via
 * another dependency's `api` configuration without declaring the dependency directly.
 */
suspend fun ProjectContext.dependents(): DependentProjects = get(DependentProjects)
