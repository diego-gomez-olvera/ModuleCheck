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

package modulecheck.parsing.groovy.antlr

import modulecheck.parsing.PluginsBlock

class GroovyPluginsBlock(
  contentString: String
) : PluginsBlock(contentString) {

  override fun findOriginalStringIndex(parsedString: String) = originalLines
    .indexOfFirst { originalLine ->
      originalLine.collapseBlockComments()
        .trimEachLineStart()
        .trimLinesLikeAntlr()
        .lines()
        .any { it.startsWith(parsedString) }
    }

  override fun toString(): String {
    return "GroovyPluginsBlock(\n" +
      "\tallDeclarations=${
      allDeclarations.toList()
        .joinToString(",\n\t\t", "\n\t\t")
      },\n" +
      ")"
  }
}
