import DigDirection.*
import Terrain.*
import strikt.api.*
import strikt.assertions.*

fun main() {
    val testInput = listOf(
        "R 6 (#70c710)",
        "D 5 (#0dc571)",
        "L 2 (#5713f0)",
        "D 2 (#d2c081)",
        "R 2 (#59c680)",
        "D 2 (#411b91)",
        "L 5 (#8ceee2)",
        "U 2 (#caa173)",
        "L 1 (#1b58a2)",
        "U 2 (#caa171)",
        "R 2 (#7807d2)",
        "U 3 (#a77fa3)",
        "L 2 (#015232)",
        "U 2 (#7a21e3)",
    )
    val testDigPlan = testInput.parseDigPlan()
    val testTrenchCoords = testDigPlan.trenchCoords()
    val testLagoon = testTrenchCoords.trench()
    testLagoon.fillFromOutside()
    val testVolume = testLagoon.volume()
    expectThat(testVolume).isEqualTo(62)

    val input = readInput("Day18")
    val lagoon = input.parseDigPlan().trenchCoords().trench()
    lagoon.fillFromOutside()
    println("Part 1: ${lagoon.volume()}")
}

private enum class DigDirection {
    L, R, U, D
}

private enum class Terrain {
    TRENCH, OUTSIDE, INSIDE
}

private data class DigPlanNode(val direction: DigDirection, val length: Int)

private data class TrenchCoord(val row: Int, val column: Int)

private fun List<String>.parseDigPlan(): List<DigPlanNode> =
    map { line ->
        val parts = line.split(' ')
        DigPlanNode(DigDirection.valueOf(parts[0]), parts[1].toInt())
    }

private fun List<DigPlanNode>.trenchCoords(): Set<TrenchCoord> {
    val coords = mutableSetOf<TrenchCoord>()
    var current = TrenchCoord(0, 0)
    forEach { node ->
        for (move in 1..node.length) {
            current = current.move(node.direction)
            coords += current
        }
    }
    expectThat(current).isEqualTo(TrenchCoord(0, 0))
    return coords
}

private fun Set<TrenchCoord>.trench(): List<MutableList<Terrain>> {
    val minRow = minOf(TrenchCoord::row)
    val maxRow = maxOf(TrenchCoord::row)
    val minColumn = minOf(TrenchCoord::column)
    val maxColumn = maxOf(TrenchCoord::column)
    return (minRow..maxRow).map { row ->
        (minColumn..maxColumn).map { column ->
            if (contains(TrenchCoord(row, column))) {
                TRENCH
            } else {
                INSIDE
            }
        }.toMutableList()
    }
}

private fun List<MutableList<Terrain>>.fillFromOutside() {
    do {
        var flippedCount = 0
        for (row in indices) {
            for (column in this[row].indices) {
                val here = this[row][column]
                if (here == INSIDE && isAnyNeighborOutside(row, column)) {
                    this[row][column] = OUTSIDE
                    flippedCount++
                }
            }
        }
    } while (flippedCount != 0)
}

private fun List<List<Terrain>>.volume(): Int =
    sumOf { row ->
        row.count { it != OUTSIDE }
    }

private fun List<List<Terrain>>.isAnyNeighborOutside(row: Int, column: Int): Boolean =
    isOutside(row -1, column) || isOutside(row + 1, column) || isOutside(row, column - 1) || isOutside(row, column + 1)

private fun List<List<Terrain>>.isOutside(row: Int, column: Int): Boolean =
    (getOrNull(row)?.getOrNull(column) ?: OUTSIDE) == OUTSIDE

private fun TrenchCoord.move(direction: DigDirection): TrenchCoord =
    when (direction) {
        L -> copy(column = column - 1)
        R -> copy(column = column + 1)
        U -> copy(row = row - 1)
        D -> copy(row = row + 1)
    }
