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

package modulecheck.api.finding

import com.squareup.anvil.annotations.ContributesBinding
import modulecheck.api.finding.Finding.FindingResult
import modulecheck.dagger.AppScope
import javax.inject.Inject

fun interface FindingResultFactory {

  suspend fun create(
    findings: List<Finding>,
    autoCorrect: Boolean,
    deleteUnused: Boolean
  ): List<FindingResult>
}

@ContributesBinding(AppScope::class)
class RealFindingResultFactory @Inject constructor() : FindingResultFactory {

  override suspend fun create(
    findings: List<Finding>,
    autoCorrect: Boolean,
    deleteUnused: Boolean
  ): List<FindingResult> {

    return findings.onEach { it.positionOrNull.await() }
      .map { finding ->

        val fixed = when {
          !autoCorrect -> false
          deleteUnused && finding is Deletable -> finding.delete()
          finding is Fixable -> finding.fix()
          else -> false
        }

        finding.toResult(fixed)
      }
  }
}
