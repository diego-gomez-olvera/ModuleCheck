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

plugins {
  javaLibrary
  id("com.vanniktech.maven.publish")
}

dependencies {

  api(libs.kotlin.compiler)
  api(libs.semVer)

  api(projects.modulecheckParsing.java)
  api(projects.modulecheckParsing.api)
  api(projects.modulecheckParsing.psi)
  api(projects.modulecheckParsing.xml)

  implementation(libs.agp)
  implementation(libs.groovy)
  implementation(libs.groovyXml)
  implementation(libs.kotlin.reflect)

  testImplementation(libs.bundles.hermit)
  testImplementation(libs.bundles.jUnit)
  testImplementation(libs.bundles.kotest)

  testImplementation(projects.modulecheckInternalTesting)
  testImplementation(projects.modulecheckSpecs)
}
