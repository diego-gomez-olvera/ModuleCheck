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

package modulecheck.testing

import hermit.test.junit.HermitJUnit5
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import java.io.File
import kotlin.properties.Delegates
import io.kotest.matchers.shouldBe as kotestShouldBe

abstract class BaseTest : HermitJUnit5() {

  val testProjectDir by resets {
    val className = testInfo.testClass.get()
      // "simpleName" for a nested class is just the nested class name,
      // so use the FqName and trim the package name to get qualified nested names
      .let { it.canonicalName.removePrefix(it.packageName + ".") }
      .replace("[^a-zA-Z0-9]".toRegex(), "_")

    val testName = testInfo.displayName
      .replace("[^a-zA-Z0-9]".toRegex(), "_")
      .replace("_{2,}".toRegex(), "_")
      .removeSuffix("_")

    File("build/tests/$className/$testName")
  }

  fun File.relativePath() = path.removePrefix(testProjectDir.path)

  /** Replace CRLF and CR line endings with Unix LF endings.*/
  fun String.normaliseLineSeparators(): String = replace("\r\n|\r".toRegex(), "\n")

  /** Replace Windows file separators with Unix ones, just for string comparison in tests */
  fun String.fixFileSeparators(): String = replace(File.separator, "/")

  fun String.clean(): String {
    return normaliseLineSeparators()
      .fixFileSeparators()
      .useRelativePaths()
      .remove("in [\\d\\.]+ seconds\\.".toRegex())
      .trim()
  }

  /** replace `ModuleCheck found 2 issues in 1.866 seconds.` with `ModuleCheck found 2 issues` */
  fun String.removeDuration(): String {
    return replace(durationSuffixRegex) { it.destructured.component1() }
  }

  /** replace absolute paths with relative ones */
  fun String.useRelativePaths(): String {
    return fixFileSeparators()
      .remove(
        // order matters here!!  absolute must go first
        testProjectDir.absolutePath.fixFileSeparators(),
        testProjectDir.path.fixFileSeparators()
      )
  }

  fun String.remove(vararg strings: String): String = strings.fold(this) { acc, string ->
    acc.replace(string, "")
  }

  fun String.remove(vararg patterns: Regex): String = patterns.fold(this) { acc, regex ->
    acc.replace(regex, "")
  }

  private var testInfo: TestInfo by Delegates.notNull()

  // This is automatically injected by JUnit5
  @BeforeEach
  internal fun injectTestInfo(testInfo: TestInfo) {
    this.testInfo = testInfo
    testProjectDir.delete()
  }

  @Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE", "MagicNumber")
  infix fun <T, U : T> T.shouldBe(expected: U?) {
    /*
    Any AssertionError generated by this function will have this function at the top of its stacktrace.

    The actual call site for the assertion is always the _second_ line.

    So, we can catch the assertion error, remove this function from the stacktrace, and rethrow.
     */
    try {
      kotestShouldBe(expected)
    } catch (assertionError: AssertionError) {
      // remove this function from the stacktrace and rethrow
      assertionError.stackTrace = assertionError
        .stackTrace
        .drop(1)
        .take(5)
        .toTypedArray()
      throw assertionError
    }
  }

  companion object {
    protected val durationSuffixRegex =
      "(ModuleCheck found [\\d]+ issues?) in [\\d\\.]+ seconds\\.[\\s\\S]*".toRegex()
  }
}
