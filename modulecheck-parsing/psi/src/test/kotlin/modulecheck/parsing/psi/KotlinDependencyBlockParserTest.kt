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

package modulecheck.parsing.psi

import io.kotest.common.runBlocking
import modulecheck.parsing.gradle.ConfigurationName
import modulecheck.parsing.gradle.ExternalDependencyDeclaration
import modulecheck.parsing.gradle.ModuleDependencyDeclaration
import modulecheck.parsing.gradle.ModuleRef.StringRef
import modulecheck.parsing.gradle.ModuleRef.TypeSafeRef
import modulecheck.parsing.gradle.UnknownDependencyDeclaration
import modulecheck.project.McProject
import modulecheck.project.test.ProjectTest
import org.junit.jupiter.api.Test

internal class KotlinDependencyBlockParserTest : ProjectTest() {

  @Test
  fun `external declaration`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api("com.foo:bar:1.2.3.4")
       }
        """.trimIndent()
      ).single()

    block.settings shouldBe listOf(
      ExternalDependencyDeclaration(
        configName = ConfigurationName.api,
        declarationText = """api("com.foo:bar:1.2.3.4")""",
        statementWithSurroundingText = """   api("com.foo:bar:1.2.3.4")""",
        group = "com.foo",
        moduleName = "bar",
        version = "1.2.3.4"
      )
    )
  }

  @Test
  fun `string extension configuration functions declaration`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
         "api"(project(path = ":core:jvm"))
       }
        """.trimIndent()
      ).single()

    block.settings shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(path = ":core:jvm")""",
        configName = ConfigurationName.api,
        declarationText = """"api"(project(path = ":core:jvm"))""",
        statementWithSurroundingText = """  "api"(project(path = ":core:jvm"))"""
      )
    )
  }

  @Test
  fun `declaration's original string should include trailing comment`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api(project(":core:jvm")) // trailing comment
          api(project(":core:jvm"))
       }
        """.trimIndent()
      ).single()

    block.getOrEmpty(":core:jvm", ConfigurationName.api) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(":core:jvm")""",
        configName = ConfigurationName.api,
        declarationText = """api(project(":core:jvm"))""",
        statementWithSurroundingText = """   api(project(":core:jvm")) // trailing comment"""
      ),
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(":core:jvm")""",
        configName = ConfigurationName.api,
        declarationText = """api(project(":core:jvm"))""",
        statementWithSurroundingText = """   api(project(":core:jvm"))"""
      )
    )
  }

  @Test
  fun `declaration with annotation should include annotation with argument`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api(project(":core:android"))
          @Suppress("Unused")
          api(project(":core:jvm"))
          testImplementation(project(":core:test"))
       }
        """.trimIndent()
      ).single()

    block.settings shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:android"),
        moduleAccess = """project(":core:android")""",
        configName = ConfigurationName.api,
        declarationText = """api(project(":core:android"))""",
        statementWithSurroundingText = "   api(project(\":core:android\"))",
        suppressed = listOf()
      ),
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(":core:jvm")""",
        configName = ConfigurationName.api,
        declarationText = """api(project(":core:jvm"))""",
        statementWithSurroundingText = "   @Suppress(\"Unused\")\n   api(project(\":core:jvm\"))",
        suppressed = listOf("Unused")
      ),
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:test"),
        moduleAccess = """project(":core:test")""",
        configName = ConfigurationName.testImplementation,
        declarationText = """testImplementation(project(":core:test"))""",
        statementWithSurroundingText = "   testImplementation(project(\":core:test\"))",
        suppressed = listOf()
      )
    )
  }

  @Test
  fun `dependency block with Suppress annotation should include annotation with argument`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       @Suppress("Unused")
       dependencies {
          api(project(":core:android"))
          @Suppress("InheritedDependency")
          api(project(":core:jvm"))
          testImplementation(project(":core:test"))
       }
        """.trimIndent()
      ).single()

    block.settings shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:android"),
        moduleAccess = """project(":core:android")""",
        configName = ConfigurationName.api,
        declarationText = """api(project(":core:android"))""",
        statementWithSurroundingText = "   api(project(\":core:android\"))",
        suppressed = listOf("Unused")
      ),
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(":core:jvm")""",
        configName = ConfigurationName.api,
        declarationText = """api(project(":core:jvm"))""",
        statementWithSurroundingText = "   @Suppress(\"InheritedDependency\")\n   api(project(\":core:jvm\"))",
        suppressed = listOf("InheritedDependency", "Unused")
      ),
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:test"),
        moduleAccess = """project(":core:test")""",
        configName = ConfigurationName.testImplementation,
        declarationText = """testImplementation(project(":core:test"))""",
        statementWithSurroundingText = "   testImplementation(project(\":core:test\"))",
        suppressed = listOf("Unused")
      )
    )
  }

  @Test
  fun `string module dependency declaration with testFixtures should be parsed`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api(testFixtures(project(":core:jvm")))
       }
        """.trimIndent()
      ).single()

    block.settings shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(":core:jvm")""",
        configName = ConfigurationName.api,
        declarationText = """api(testFixtures(project(":core:jvm")))""",
        statementWithSurroundingText = """   api(testFixtures(project(":core:jvm")))"""
      )
    )
  }

  @Test
  fun `type-safe module dependency declaration with testFixtures should be parsed`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api(testFixtures(projects.core.jvm))
       }
        """.trimIndent()
      ).single()

    block.settings shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = TypeSafeRef("core.jvm"),
        moduleAccess = "projects.core.jvm",
        configName = ConfigurationName.api,
        declarationText = """api(testFixtures(projects.core.jvm))""",
        statementWithSurroundingText = """   api(testFixtures(projects.core.jvm))"""
      )
    )
  }

  @Test
  fun `module dependency with config block should split declarations properly`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api(project(":core:test")) {
            exclude(group = "androidx.appcompat")
          }

          api(project(":core:jvm"))
       }
        """.trimIndent()
      ).single()

    block.getOrEmpty(":core:test", ConfigurationName.api) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:test"),
        moduleAccess = """project(":core:test")""",
        configName = ConfigurationName.api,
        declarationText = """api(project(":core:test")) {
          |     exclude(group = "androidx.appcompat")
          |   }
        """.trimMargin(),
        statementWithSurroundingText = """   api(project(":core:test")) {
          |     exclude(group = "androidx.appcompat")
          |   }
        """.trimMargin()
      )
    )

    block.getOrEmpty(":core:jvm", ConfigurationName.api) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(":core:jvm")""",
        configName = ConfigurationName.api,
        declarationText = "api(project(\":core:jvm\"))",
        statementWithSurroundingText = "\n   api(project(\":core:jvm\"))"
      )
    )
  }

  @Test
  fun `module dependency with config block and preceding declaration should split declarations properly`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api(project(":core:jvm"))

          api(project(":core:test")) {
            exclude(group = "androidx.appcompat")
          }
       }
        """.trimIndent()
      ).single()

    block.getOrEmpty(":core:test", ConfigurationName.api) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:test"),
        moduleAccess = """project(":core:test")""",
        configName = ConfigurationName.api,
        declarationText = """api(project(":core:test")) {
          |     exclude(group = "androidx.appcompat")
          |   }
        """.trimMargin(),
        statementWithSurroundingText = """
          |
          |   api(project(":core:test")) {
          |     exclude(group = "androidx.appcompat")
          |   }
        """.trimMargin()
      )
    )

    block.getOrEmpty(":core:jvm", ConfigurationName.api) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(":core:jvm")""",
        configName = ConfigurationName.api,
        declarationText = "api(project(\":core:jvm\"))",
        statementWithSurroundingText = "   api(project(\":core:jvm\"))"
      )
    )
  }

  @Test
  fun `module dependency with preceding blank line should preserve the blank line`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api(project(":core:test"))

          api(project(":core:jvm"))
       }
        """.trimIndent()
      ).single()

    block.getOrEmpty(":core:jvm", ConfigurationName.api) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(":core:jvm")""",
        configName = ConfigurationName.api,
        declarationText = "api(project(\":core:jvm\"))",
        statementWithSurroundingText = "\n   api(project(\":core:jvm\"))"
      )
    )
  }

  @Test
  fun `module dependency with two different configs should be recorded twice`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          implementation(project(":core:jvm"))
          api(project(":core:jvm"))
       }
        """.trimIndent()
      ).single()

    block.getOrEmpty(":core:jvm", ConfigurationName.api) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(":core:jvm")""",
        configName = ConfigurationName.api,
        declarationText = """api(project(":core:jvm"))""",
        statementWithSurroundingText = """   api(project(":core:jvm"))"""
      )
    )

    block.getOrEmpty(":core:jvm", ConfigurationName.implementation) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(":core:jvm")""",
        configName = ConfigurationName.implementation,
        declarationText = """implementation(project(":core:jvm"))""",
        statementWithSurroundingText = """   implementation(project(":core:jvm"))"""
      )
    )
  }

  @Test
  fun `declaration's original string should include preceding single-line comment`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api("com.foo:bar:1.2.3.4") // inline comment

          // single-line comment
          implementation(project(":core:android"))
       }
        """.trimIndent()
      ).single()

    block.getOrEmpty(":core:android", ConfigurationName.implementation) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:android"),
        moduleAccess = """project(":core:android")""",
        configName = ConfigurationName.implementation,
        declarationText = """implementation(project(":core:android"))""",
        statementWithSurroundingText = """
   // single-line comment
   implementation(project(":core:android"))"""
      )
    )
  }

  @Test
  fun `declaration's original string should include preceding block comment`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api("com.foo:bar:1.2.3.4") // inline comment

          /*
          block comment
          */
          implementation(project(":core:android"))
       }
        """.trimIndent()
      ).single()

    block.getOrEmpty(":core:android", ConfigurationName.implementation) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:android"),
        moduleAccess = """project(":core:android")""",
        configName = ConfigurationName.implementation,
        declarationText = """implementation(project(":core:android"))""",
        statementWithSurroundingText = """
   /*
   block comment
   */
   implementation(project(":core:android"))"""
      )
    )
  }

  @Test
  fun `declaration's original string should include preceding in-line block comment`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api("com.foo:bar:1.2.3.4") // inline comment
          /* single-line block comment */ implementation(project(":core:android"))
       }
        """.trimIndent()
      ).single()

    block.getOrEmpty(":core:android", ConfigurationName.implementation) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:android"),
        moduleAccess = """project(":core:android")""",
        configName = ConfigurationName.implementation,
        declarationText = """implementation(project(":core:android"))""",
        statementWithSurroundingText = """   /* single-line block comment */ implementation(project(":core:android"))"""
      )
    )
  }

  @Test
  fun `duplicate module dependency with same config should be recorded twice`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api(project(":core:jvm"))
          api (   project(":core:jvm"))
       }
        """.trimIndent()
      ).single()

    block.getOrEmpty(":core:jvm", ConfigurationName.api) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(":core:jvm")""",
        configName = ConfigurationName.api,
        declarationText = """api(project(":core:jvm"))""",
        statementWithSurroundingText = """   api(project(":core:jvm"))"""
      ),
      ModuleDependencyDeclaration(
        moduleRef = StringRef(":core:jvm"),
        moduleAccess = """project(":core:jvm")""",
        configName = ConfigurationName.api,
        declarationText = """api (   project(":core:jvm"))""",
        statementWithSurroundingText = """   api (   project(":core:jvm"))"""
      )
    )
  }

  @Test
  fun `modules declared using type-safe accessors can be looked up using their path`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
       dependencies {
          api(projects.core.test)
          implementation(projects.httpLogging)
       }
        """.trimIndent()
      ).single()

    block.getOrEmpty(":core:test", ConfigurationName.api) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = TypeSafeRef("core.test"),
        moduleAccess = "projects.core.test",
        configName = ConfigurationName.api,
        declarationText = """api(projects.core.test)""",
        statementWithSurroundingText = """   api(projects.core.test)"""
      )
    )

    block.getOrEmpty(":http-logging", ConfigurationName.implementation) shouldBe listOf(
      ModuleDependencyDeclaration(
        moduleRef = TypeSafeRef("httpLogging"),
        moduleAccess = "projects.httpLogging",
        configName = ConfigurationName.implementation,
        declarationText = """implementation(projects.httpLogging)""",
        statementWithSurroundingText = """   implementation(projects.httpLogging)"""
      )
    )
  }

  @Test
  fun `buildscript dependencies should not be parsed`() {
    val block = KotlinDependencyBlockParser()
      .parse(
        """
        |buildscript {
        |  repositories {
        |    mavenCentral()
        |    google()
        |    jcenter()
        |    maven("https://plugins.gradle.org/m2/")
        |    maven("https://oss.sonatype.org/content/repositories/snapshots")
        |  }
        |  dependencies {
        |    classpath("com.android.tools.build:gradle:7.0.2")
        |    classpath("com.squareup.anvil:gradle-plugin:2.3.4")
        |    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.30")
        |  }
        |}
        |dependencies {
        |  api(libs.ktlint)
        |}
        |""".trimMargin()
      ).single()

    block.settings shouldBe listOf(
      UnknownDependencyDeclaration(
        argument = "libs.ktlint",
        configName = ConfigurationName.api,
        declarationText = "api(libs.ktlint)",
        statementWithSurroundingText = "  api(libs.ktlint)"
      )
    )
  }

  fun KotlinDependencyBlockParser.parse(
    string: String,
    project: McProject = simpleProject(buildFileText = string)
  ): List<KotlinDependenciesBlock> = runBlocking { parse(project) }
}
