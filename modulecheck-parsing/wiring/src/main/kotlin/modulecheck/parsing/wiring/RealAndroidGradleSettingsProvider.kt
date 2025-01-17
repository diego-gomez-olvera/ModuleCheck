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

package modulecheck.parsing.wiring

import com.squareup.anvil.annotations.ContributesBinding
import modulecheck.dagger.AppScope
import modulecheck.parsing.gradle.AndroidGradleSettings
import modulecheck.parsing.gradle.AndroidGradleSettingsProvider
import modulecheck.parsing.groovy.antlr.GroovyAndroidGradleParser
import modulecheck.parsing.psi.KotlinAndroidGradleParser
import org.jetbrains.kotlin.incremental.isKotlinFile
import java.io.File
import javax.inject.Inject
import javax.inject.Provider

class RealAndroidGradleSettingsProvider(
  private val groovyParser: GroovyAndroidGradleParser,
  private val kotlinParser: KotlinAndroidGradleParser,
  private val buildFile: File
) : AndroidGradleSettingsProvider {

  override fun get(): AndroidGradleSettings {
    return when {
      buildFile.isKotlinFile(listOf("kts")) -> kotlinParser.parse(buildFile)
      buildFile.extension == "gradle" -> groovyParser.parse(buildFile)
      else -> throw IllegalArgumentException(
        "The file argument must be either a `*.gradle.kts` file or `*.gradle`.  " +
          "The supplied argument was `${buildFile.name}`"
      )
    }
  }

  @ContributesBinding(AppScope::class)
  class Factory @Inject constructor(
    private val groovyParserProvider: Provider<GroovyAndroidGradleParser>,
    private val kotlinParserProvider: Provider<KotlinAndroidGradleParser>
  ) : AndroidGradleSettingsProvider.Factory {
    override fun create(buildFile: File): RealAndroidGradleSettingsProvider {
      return RealAndroidGradleSettingsProvider(
        groovyParser = groovyParserProvider.get(),
        kotlinParser = kotlinParserProvider.get(),
        buildFile = buildFile
      )
    }
  }
}
