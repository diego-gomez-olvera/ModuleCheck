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

package com.rickbusarow.modulecheck.specs

import java.nio.file.Path

public data class ProjectBuildSpec(
  public val plugins: MutableList<String>,
  public val imports: MutableList<String>,
  public val blocks: MutableList<String>,
  public val repositories: MutableList<String>,
  public val dependencies: MutableList<String>,
  public var isAndroid: Boolean,
  public var buildScript: Boolean
) {

  public fun toBuilder(): ProjectBuildSpecBuilder = ProjectBuildSpecBuilder(
    plugins = plugins,
    imports = imports,
    blocks = blocks,
    repositories = repositories,
    dependencies = dependencies,
    android = isAndroid,
    buildScript = buildScript
  )

  public inline fun edit(
    init: ProjectBuildSpecBuilder.() -> Unit
  ): ProjectBuildSpec = toBuilder().apply { init() }.build()

  public fun writeIn(path: Path) {
    path.toFile().mkdirs()
    path.newFile("build.gradle.kts")
      .writeText(
        imports() +
          buildScriptBlock() +
          pluginsBlock() +
          repositoriesBlock() +
          androidBlock() +
          dependenciesBlock() +
          blocksBlock()
      )
  }

  private fun pluginsBlock() = if (plugins.isEmpty()) "" else buildString {
    appendLine("plugins {")
    plugins.forEach { appendLine("  $it") }
    appendLine("}\n")
  }

  private fun imports() = if (imports.isEmpty()) "" else buildString {
    imports.forEach { appendLine(it) }
    appendLine()
  }

  private fun blocksBlock() = if (blocks.isEmpty()) "" else buildString {
    blocks.forEach { appendLine("$it\n") }
  }

  private fun repositoriesBlock() = if (repositories.isEmpty()) "" else buildString {
    appendLine("repositories {")
    repositories.forEach { appendLine("  $it") }
    appendLine("}\n")
  }

  private fun buildScriptBlock() = if (!buildScript) "" else """buildscript {
      |  repositories {
      |    mavenCentral()
      |    google()
      |    jcenter()
      |    maven("https://plugins.gradle.org/m2/")
      |    maven("https://oss.sonatype.org/content/repositories/snapshots")
      |  }
      |  dependencies {
      |    classpath("com.android.tools.build:gradle:4.1.1")
      |    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.30")
      |  }
      |}
      |
      |allprojects {
      |
      |  repositories {
      |    mavenCentral()
      |    google()
      |    jcenter()
      |    maven("https://plugins.gradle.org/m2/")
      |    maven("https://oss.sonatype.org/content/repositories/snapshots")
      |  }
      |
      |}
      |""".trimMargin()

  private fun androidBlock() = if (!isAndroid) "" else """android {
      |  compileSdkVersion(30)
      |
      |  defaultConfig {
      |    minSdkVersion(23)
      |    targetSdkVersion(30)
      |    versionCode = 1
      |    versionName = "1.0"
      |  }
      |
      |  buildTypes {
      |    getByName("release") {
      |      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      |    }
      |  }
      |}
      |
      |""".trimMargin()

  private fun dependenciesBlock() = if (dependencies.isEmpty()) "" else buildString {
    appendLine("dependencies {")
    dependencies.forEach { appendLine("  $it") }
    appendLine("}")
  }

  public companion object {

    public operator fun invoke(
      init: ProjectBuildSpecBuilder.() -> Unit
    ): ProjectBuildSpec = ProjectBuildSpecBuilder(init = init).build()

    public fun builder(): ProjectBuildSpecBuilder = ProjectBuildSpecBuilder()
  }
}

@Suppress("LongParameterList", "TooManyFunctions")
public class ProjectBuildSpecBuilder(
  public val plugins: MutableList<String> = mutableListOf(),
  public val imports: MutableList<String> = mutableListOf(),
  public val blocks: MutableList<String> = mutableListOf(),
  public val repositories: MutableList<String> = mutableListOf(),
  public val dependencies: MutableList<String> = mutableListOf(),
  public var android: Boolean = false,
  public var buildScript: Boolean = false,
  init: ProjectBuildSpecBuilder.() -> Unit = {}
) : Builder<ProjectBuildSpec> {

  init {
    init()
  }

  public fun buildScript(): ProjectBuildSpecBuilder = apply {
    buildScript = true
  }

  public fun android(): ProjectBuildSpecBuilder = apply {
    android = true
  }

  public fun addImport(import: String): ProjectBuildSpecBuilder = apply {
    imports.add(import)
  }

  public fun addBlock(codeBlock: String): ProjectBuildSpecBuilder = apply {
    blocks.add(codeBlock)
  }

  public fun addPlugin(
    plugin: String,
    comment: String? = null,
    inlineComment: String? = null
  ): ProjectBuildSpecBuilder = apply {
    val prev = comment?.let { "$it\n  " } ?: ""
    val after = inlineComment?.let { " $it" } ?: ""

    plugins.add("$prev$plugin$after")
  }

  public fun addRepository(
    repository: String,
    comment: String? = null,
    inlineComment: String? = null
  ): ProjectBuildSpecBuilder = apply {
    val prev = comment?.let { "$it\n  " } ?: ""
    val after = inlineComment?.let { " $it" } ?: ""

    repositories.add("$prev$repository$after")
  }

  public fun addRawDependency(
    configuration: String
  ): ProjectBuildSpecBuilder = apply {
    dependencies.add(configuration)
  }

  public fun addExternalDependency(
    configuration: String,
    dependencyPath: String,
    comment: String? = null,
    inlineComment: String? = null
  ): ProjectBuildSpecBuilder = apply {
    val prev = comment?.let { "$it\n  " } ?: ""
    val after = inlineComment?.let { " $it" } ?: ""

    dependencies.add("$prev$configuration(\":$dependencyPath\")$after")
  }

  public fun addProjectDependency(
    configuration: String,
    dependencyProjectSpec: ProjectSpec,
    comment: String? = null,
    inlineComment: String? = null
  ): ProjectBuildSpecBuilder = apply {
    val prev = comment?.let { "$it\n  " } ?: ""
    val after = inlineComment?.let { " $it" } ?: ""

    dependencies.add("$prev$configuration(project(path = \":${dependencyProjectSpec.gradlePath}\"))$after")
  }

  public fun addProjectDependency(
    configuration: String,
    dependencyPath: String,
    comment: String? = null,
    inlineComment: String? = null
  ): ProjectBuildSpecBuilder = apply {
    val prev = comment?.let { "$it\n  " } ?: ""
    val after = inlineComment?.let { " $it" } ?: ""

    dependencies.add("$prev$configuration(project(path = \":$dependencyPath\"))$after")
  }

  public fun addProjectDependency2(
    configuration: String,
    dependencyPath: String
  ) {
    addRawDependency("$configuration(project(path = \":$dependencyPath\"))")
  }

  override fun build(): ProjectBuildSpec = ProjectBuildSpec(
    plugins = plugins,
    imports = imports,
    blocks = blocks,
    repositories = repositories,
    dependencies = dependencies,
    isAndroid = android,
    buildScript = buildScript
  )
}
