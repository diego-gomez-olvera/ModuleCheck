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

// AGP Variant API's are deprecated
// Gradle's Convention API's are deprecated, but only available in 7.2+
@file:Suppress("DEPRECATION")

package modulecheck.gradle

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.api.TestedVariant
import com.squareup.anvil.plugin.AnvilExtension
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import modulecheck.api.settings.ModuleCheckSettings
import modulecheck.core.rule.KAPT_PLUGIN_ID
import modulecheck.gradle.internal.androidManifests
import modulecheck.gradle.internal.existingFiles
import modulecheck.gradle.task.GradleLogger
import modulecheck.parsing.gradle.Config
import modulecheck.parsing.gradle.Configurations
import modulecheck.parsing.gradle.SourceSet
import modulecheck.parsing.gradle.SourceSets
import modulecheck.parsing.gradle.asConfigurationName
import modulecheck.parsing.gradle.toSourceSetName
import modulecheck.parsing.source.AnvilGradlePlugin
import modulecheck.parsing.source.JavaVersion
import modulecheck.parsing.wiring.RealJvmFileProvider
import modulecheck.project.BuildFileParser
import modulecheck.project.ConfiguredProjectDependency
import modulecheck.project.ExternalDependencies
import modulecheck.project.ExternalDependency
import modulecheck.project.McProject
import modulecheck.project.ProjectCache
import modulecheck.project.ProjectDependencies
import modulecheck.project.ProjectProvider
import modulecheck.project.impl.RealAndroidMcProject
import modulecheck.project.impl.RealMcProject
import modulecheck.utils.mapToSet
import net.swiftzer.semver.SemVer
import org.gradle.api.DomainObjectSet
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.internal.HasConvention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.internal.component.external.model.ProjectDerivedCapability
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import kotlin.LazyThreadSafetyMode.NONE

class GradleProjectProvider @AssistedInject constructor(
  @Assisted
  private val rootGradleProject: GradleProject,
  private val settings: ModuleCheckSettings,
  override val projectCache: ProjectCache,
  private val gradleLogger: GradleLogger,
  private val buildFileParserFactory: BuildFileParser.Factory,
  private val jvmFileProviderFactory: RealJvmFileProvider.Factory
) : ProjectProvider {

  private val gradleProjects = rootGradleProject.allprojects
    .associateBy { it.path }

  override fun get(path: String): McProject {
    return projectCache.getOrPut(path) {
      createProject(path)
    }
  }

  override fun getAll(): List<McProject> {
    return rootGradleProject.allprojects
      .filter { it.buildFile.exists() }
      .filterNot { it.path in settings.doNotCheck }
      .map { get(it.path) }
  }

  override fun clearCaches() {
    projectCache.clearContexts()
  }

  @Suppress("UnstableApiUsage")
  private fun createProject(path: String): McProject {
    val gradleProject = gradleProjects.getValue(path)

    val configurations = gradleProject.configurations()

    val projectDependencies = gradleProject.projectDependencies()
    val externalDependencies = gradleProject.externalDependencies()

    val hasKapt = gradleProject
      .plugins
      .hasPlugin(KAPT_PLUGIN_ID)
    val hasTestFixturesPlugin = gradleProject
      .pluginManager
      .hasPlugin(TEST_FIXTURES_PLUGIN_ID)

    val testedExtension = gradleProject
      .extensions
      .findByType(LibraryExtension::class.java)
      ?: gradleProject
        .extensions
        .findByType(AppExtension::class.java)

    val isAndroid = testedExtension != null

    val libraryExtension by lazy(NONE) {
      gradleProject
        .extensions
        .findByType(LibraryExtension::class.java)
    }

    return if (isAndroid) {
      RealAndroidMcProject(
        path = path,
        projectDir = gradleProject.projectDir,
        buildFile = gradleProject.buildFile,
        configurations = configurations,
        hasKapt = hasKapt,
        hasTestFixturesPlugin = hasTestFixturesPlugin,
        sourceSets = gradleProject.androidSourceSets(),
        projectCache = projectCache,
        anvilGradlePlugin = gradleProject.anvilGradlePluginOrNull(),
        androidResourcesEnabled = libraryExtension?.buildFeatures?.androidResources != false,
        viewBindingEnabled = testedExtension?.buildFeatures?.viewBinding == true,
        manifests = gradleProject.androidManifests().orEmpty(),
        logger = gradleLogger,
        jvmFileProviderFactory = jvmFileProviderFactory,
        javaSourceVersion = gradleProject.javaVersion(),
        projectDependencies = projectDependencies,
        externalDependencies = externalDependencies,
        buildFileParserFactory = buildFileParserFactory
      )
    } else {
      RealMcProject(
        path = path,
        projectDir = gradleProject.projectDir,
        buildFile = gradleProject.buildFile,
        configurations = configurations,
        hasKapt = hasKapt,
        hasTestFixturesPlugin = hasTestFixturesPlugin,
        sourceSets = gradleProject.jvmSourceSets(),
        projectCache = projectCache,
        anvilGradlePlugin = gradleProject.anvilGradlePluginOrNull(),
        logger = gradleLogger,
        jvmFileProviderFactory = jvmFileProviderFactory,
        javaSourceVersion = gradleProject.javaVersion(),
        projectDependencies = projectDependencies,
        externalDependencies = externalDependencies,
        buildFileParserFactory = buildFileParserFactory
      )
    }
  }

  private fun GradleProject.configurations(): Configurations {

    fun Configuration.allInherited(): Set<Configuration> {
      return generateSequence(extendsFrom.asSequence()) { extended ->
        extended.flatMap { it.extendsFrom.asSequence() }
          .takeIf { it.iterator().hasNext() }
      }.flatten()
        .toSet()
    }

    fun Configuration.toConfig(): Config {

      return Config(
        name = name.asConfigurationName(),
        inherited = allInherited().mapToSet { it.toConfig() }
      )
    }

    val map = configurations
      .filterNot { it.name == ScriptHandler.CLASSPATH_CONFIGURATION }
      .associate { configuration ->

        val config = configuration.toConfig()

        configuration.name.asConfigurationName() to config
      }
    return Configurations(map)
  }

  private fun GradleProject.externalDependencies(): Lazy<ExternalDependencies> = lazy {
    val map = configurations
      .associate { configuration ->

        val externalDependencies = configuration.dependencies
          .filterIsInstance<ExternalModuleDependency>()
          .map { dep ->

            ExternalDependency(
              configurationName = configuration.name.asConfigurationName(),
              group = dep.group,
              moduleName = dep.name,
              version = dep.version
            )
          }

        configuration.name.asConfigurationName() to externalDependencies
      }
      .toMutableMap()

    ExternalDependencies(map)
  }

  private fun GradleProject.projectDependencies(): Lazy<ProjectDependencies> =
    lazy {
      val map = configurations
        .filterNot { it.name == "ktlintRuleset" }
        .associate { config ->
          config.name.asConfigurationName() to config.dependencies
            .withType(ProjectDependency::class.java)
            .map {

              val isTestFixture = it.requestedCapabilities
                .filterIsInstance<ProjectDerivedCapability>()
                .any { capability -> capability.capabilityId.endsWith(TEST_FIXTURES_SUFFIX) }

              ConfiguredProjectDependency(
                configurationName = config.name.asConfigurationName(),
                project = get(it.dependencyProject.path),
                isTestFixture = isTestFixture
              )
            }
        }
        .toMutableMap()
      ProjectDependencies(map)
    }

  private fun GradleProject.javaVersion(): JavaVersion {
    return convention
      .findPlugin(JavaPluginConvention::class.java)
      ?.sourceCompatibility
      ?.toJavaVersion()
      ?: JavaVersion.VERSION_1_8
  }

  private fun GradleProject.jvmSourceSets(): SourceSets {
    val map = convention
      .findPlugin(JavaPluginConvention::class.java)
      ?.sourceSets
      ?.map { gradleSourceSet ->
        val jvmFiles = (
          (gradleSourceSet as? HasConvention)
            ?.convention
            ?.plugins
            ?.get("kotlin") as? KotlinSourceSet
          )
          ?.kotlin
          ?.sourceDirectories
          ?.files
          ?: gradleSourceSet.allJava.files

        SourceSet(
          name = gradleSourceSet.name.toSourceSetName(),
          classpathFiles = gradleSourceSet.compileClasspath.existingFiles().files,
          outputFiles = gradleSourceSet.output.classesDirs.existingFiles().files,
          jvmFiles = jvmFiles,
          resourceFiles = gradleSourceSet.resources.sourceDirectories.files
        )
      }
      ?.associateBy { it.name }
      .orEmpty()

    return SourceSets(map)
  }

  private fun GradleProject.anvilGradlePluginOrNull(): AnvilGradlePlugin? {
    /*
    Before Kotlin 1.5.0, Anvil was applied to the `kotlinCompilerPluginClasspath` config.

    In 1.5.0+, it's applied to individual source sets, such as
    `kotlinCompilerPluginClasspathMain`, `kotlinCompilerPluginClasspathTest`, etc.
     */
    val version = configurations
      .filter { it.name.startsWith("kotlinCompilerPluginClasspath") }
      .asSequence()
      .flatMap { it.dependencies }
      .firstOrNull { it.group == "com.squareup.anvil" }
      ?.version
      ?.let { versionString -> SemVer.parse(versionString) }
      ?: return null

    val enabled = extensions
      .findByType(AnvilExtension::class.java)
      ?.generateDaggerFactories
      ?.get() == true

    return AnvilGradlePlugin(version, enabled)
  }

  private val BaseExtension.variants: DomainObjectSet<out BaseVariant>?
    get() = when (this) {
      is AppExtension -> applicationVariants
      is LibraryExtension -> libraryVariants
      is TestExtension -> applicationVariants
      else -> null
    }

  private val BaseVariant.testVariants: List<BaseVariant>
    get() = when (this) {
      is TestedVariant -> listOfNotNull(testVariant, unitTestVariant)
      else -> emptyList()
    }

  private fun GradleProject.androidSourceSets(): SourceSets {
    val map = extensions
      .findByType(BaseExtension::class.java)
      ?.variants
      ?.flatMap { variant ->

        val testSourceSets = variant
          .testVariants
          .flatMap { it.sourceSets }

        val mainSourceSets = variant.sourceSets

        (testSourceSets + mainSourceSets)
          .distinctBy { it.name }
          .map { sourceProvider ->

            val jvmFiles = with(sourceProvider) {
              javaDirectories + kotlinDirectories
            }
              .flatMap { it.listFiles().orEmpty().toList() }
              .toSet()

            val resourceFiles = sourceProvider
              .resDirectories
              .flatMap { it.listFiles().orEmpty().toList() }
              .flatMap { it.listFiles().orEmpty().toList() }
              .toSet()

            val layoutFiles = resourceFiles
              .filter {
                it.isFile && it.path
                  .replace(
                    File.separator,
                    "/"
                  ) // replaceDestructured `\` from Windows paths with `/`.
                  .contains("""/res/layout.*/.*.xml""".toRegex())
              }
              .toSet()

            SourceSet(
              name = sourceProvider.name.toSourceSetName(),
              classpathFiles = emptySet(),
              outputFiles = setOf(), // TODO
              jvmFiles = jvmFiles,
              resourceFiles = resourceFiles,
              layoutFiles = layoutFiles
            )
          }
      }
      ?.associateBy { it.name }
      .orEmpty()

    return SourceSets(map)
  }

  companion object {
    private const val TEST_FIXTURES_SUFFIX = "-test-fixtures"
    private const val TEST_FIXTURES_PLUGIN_ID = "java-test-fixtures"
  }

  @AssistedFactory
  interface Factory {
    fun create(rootGradleProject: GradleProject): GradleProjectProvider
  }
}
