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

package modulecheck.builds

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

open class MyTask @Inject constructor(
  @get:Input
  val thing: Thing
) : DefaultTask() {

  private val p: Set<String> by lazy {
    project.configurations
      .flatMap { it.dependencies }
      .filterIsInstance<ProjectDependency>()
      .map { it.dependencyProject.path }
      .toSet()
  }

  @TaskAction
  fun run() {
    println("thing --> ${p}")
  }
}

class Thing(val name: String)
