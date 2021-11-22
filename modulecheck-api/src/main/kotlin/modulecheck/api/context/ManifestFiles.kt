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

import modulecheck.parsing.xml.XmlFile
import modulecheck.project.AndroidMcProject
import modulecheck.project.McProject
import modulecheck.project.ProjectContext
import modulecheck.project.SourceSetName
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

data class ManifestFiles(
  internal val delegate: ConcurrentMap<SourceSetName, XmlFile.ManifestFile>
) : ConcurrentMap<SourceSetName, XmlFile.ManifestFile> by delegate,
  ProjectContext.Element {

  override val key: ProjectContext.Key<ManifestFiles>
    get() = Key

  companion object Key : ProjectContext.Key<ManifestFiles> {
    override suspend operator fun invoke(project: McProject): ManifestFiles {

      if (project !is AndroidMcProject) return ManifestFiles(ConcurrentHashMap())

      val map = project
        .manifests
        .mapValues { (_, file) ->
          XmlFile.ManifestFile(file)
        }

      return ManifestFiles(ConcurrentHashMap(map))
    }
  }
}

suspend fun ProjectContext.manifestFiles(): ManifestFiles = get(ManifestFiles)

suspend fun ProjectContext.manifestFilesForSourceSetName(
  sourceSetName: SourceSetName
): XmlFile.ManifestFile? = manifestFiles()[sourceSetName]
  ?.takeIf { it.file.exists() }
  ?: manifestFiles()[SourceSetName.MAIN]
    ?.takeIf { it.file.exists() }
