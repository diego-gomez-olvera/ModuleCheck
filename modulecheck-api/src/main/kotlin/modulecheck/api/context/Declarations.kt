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

import kotlinx.coroutines.flow.toList
import modulecheck.parsing.gradle.SourceSetName
import modulecheck.parsing.source.DeclarationName
import modulecheck.parsing.source.asDeclarationName
import modulecheck.project.ConfiguredProjectDependency
import modulecheck.project.McProject
import modulecheck.project.ProjectContext
import modulecheck.project.isAndroid
import modulecheck.utils.LazySet
import modulecheck.utils.LazySet.DataSource
import modulecheck.utils.LazySet.DataSource.Priority.HIGH
import modulecheck.utils.SafeCache
import modulecheck.utils.dataSource
import modulecheck.utils.lazySet

data class Declarations(
  private val delegate: SafeCache<SourceSetName, LazySet<DeclarationName>>,
  private val project: McProject
) : ProjectContext.Element {

  override val key: ProjectContext.Key<Declarations>
    get() = Key

  suspend fun get(sourceSetName: SourceSetName): LazySet<DeclarationName> {
    return delegate.getOrPut(sourceSetName) {

      val sets = mutableListOf<LazySet<DeclarationName>>()
      val sources = mutableListOf<DataSource<DeclarationName>>()

      sourceSetName
        .withUpstream(project)
        .forEach { sourceSetOrUpstream ->

          val rNameOrNull = project.androidRFqNameForSourceSetName(sourceSetOrUpstream)

          project.jvmFilesForSourceSetName(sourceSetOrUpstream)
            .toList()
            .map { dataSource(HIGH) { it.declarations } }
            .let { sources.addAll(it) }

          if (rNameOrNull != null) {
            sources.add(dataSource { setOf(rNameOrNull.asDeclarationName()) })
          }

          if (project.isAndroid()) {
            sets.add(project.androidResourceDeclarationsForSourceSetName(sourceSetOrUpstream))

            sets.add(project.androidDataBindingDeclarationsForSourceSetName(sourceSetOrUpstream))
          }
        }

      lazySet(sets, sources)
    }
  }

  companion object Key : ProjectContext.Key<Declarations> {
    override suspend operator fun invoke(project: McProject): Declarations {

      return Declarations(SafeCache(), project)
    }
  }
}

suspend fun ProjectContext.declarations(): Declarations = get(Declarations)

suspend fun ConfiguredProjectDependency.declarations(): LazySet<DeclarationName> {
  return if (isTestFixture) {
    project.declarations().get(SourceSetName.TEST_FIXTURES)
  } else {
    project.declarations().get(SourceSetName.MAIN)
  }
}
