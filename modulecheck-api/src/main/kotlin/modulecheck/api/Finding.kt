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

package modulecheck.api

import modulecheck.api.Finding.Position
import java.io.File

interface Finding {

  val problemName: String
  val dependentPath: String
  val buildFile: File

  val statementTextOrNull: String? get() = null
  val positionOrNull: Position?

  fun logString(): String {
    return "${buildFile.path}: ${positionString()} $problemName"
  }

  fun positionString() = positionOrNull?.logString() ?: ""

  data class Position(
    val row: Int,
    val column: Int
  ) {
    fun logString(): String = "($row, $column): "
  }
}

fun String.positionOfStatement(statement: String): Position {

  val lines = lines()
  val trimmedLastStatementLine = statement.trimEnd()
    .lines()
    .last()
    .trimStart()

  var index = indexOf(trimmedLastStatementLine)

  var row = 0

  while (lines[row].length < index) {
    // if the current row's string isn't long enough, subtract its length from the total index
    // and move on to the next row.  Subtract an additional 1 because the newline character
    // in the full string isn't included in the line's string.
    index -= (lines[row].length + 1)
    row++
  }
  return Position(row + 1, index + 1)
}
