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

package modulecheck.core

import modulecheck.parsing.DependencyBlockParser
import modulecheck.parsing.PluginBlockParser
import modulecheck.parsing.gradle.DependenciesBlock
import modulecheck.parsing.gradle.PluginsBlock
import modulecheck.parsing.groovy.antlr.GroovyDependencyBlockParser
import modulecheck.parsing.groovy.antlr.GroovyPluginsBlockParser
import modulecheck.parsing.psi.KotlinDependencyBlockParser
import modulecheck.parsing.psi.KotlinPluginsBlockParser
import modulecheck.parsing.psi.internal.asKtFile
import org.jetbrains.kotlin.incremental.isKotlinFile
import java.io.File

fun DependencyBlockParser.Companion.parse(file: File): List<DependenciesBlock> {
  return when {
    file.isKotlinFile(listOf("kts")) -> KotlinDependencyBlockParser().parse(file.asKtFile())
    file.extension == "gradle" -> GroovyDependencyBlockParser().parse(file.readText())
    else -> throw IllegalArgumentException(
      "The file argument must be either a `*.gradle.kts` file or `*.gradle`.  " +
        "The supplied argument was `${file.name}`"
    )
  }
}

fun PluginBlockParser.Companion.parse(file: File): PluginsBlock? {
  return when {
    file.isKotlinFile(listOf("kts")) -> KotlinPluginsBlockParser().parse(file.asKtFile())
    file.extension == "gradle" -> GroovyPluginsBlockParser().parse(file.readText())
    else -> throw IllegalArgumentException(
      "The file argument must be either a `*.gradle.kts` file or `*.gradle`.  " +
        "The supplied argument was `${file.name}`"
    )
  }
}
