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
    val testLagoon = testInput.parseDigPlan().trenchAreas().normalize()
    testLagoon.fillFromOutside()
    expectThat(testLagoon.volume()).isEqualTo(62)

    val input = readInput("Day18")
    val lagoon = input.parseDigPlan().trenchAreas().normalize()
    lagoon.fillFromOutside()
    println("Part 1: ${lagoon.volume()}")

    expectThat(testInput.take(1).parseCorrectedPlan()).containsExactly(DigPlanNode(R, 461937))
    val correctedTestLagoon = testInput.parseCorrectedPlan().trenchAreas().normalize()
    correctedTestLagoon.fillFromOutside()
    expectThat(correctedTestLagoon.volume()).isEqualTo(952408144115)

    val correctedLagoon = input.parseCorrectedPlan().trenchAreas().normalize()
    correctedLagoon.fillFromOutside()
    println("Part 2: ${correctedLagoon.volume()}")
}

private val ColorRegex = """.*\(#(.{5})(.)\)""".toRegex()

private enum class DigDirection {
    R, D, L, U
}

private enum class Terrain {
    TRENCH, OUTSIDE, INSIDE
}

private data class DigPlanNode(val direction: DigDirection, val length: Long)
private data class Patch(val xMin: Long, val xMax: Long, val yMin: Long, val yMax: Long, var terrain: Terrain)

private fun List<String>.parseDigPlan(): List<DigPlanNode> =
    map { line ->
        val parts = line.split(' ')
        DigPlanNode(DigDirection.valueOf(parts[0]), parts[1].toLong())
    }

private fun List<String>.parseCorrectedPlan(): List<DigPlanNode> =
    map { line ->
        val (_, distance, direction) = ColorRegex.matchEntire(line)?.groupValues ?: error("Cannot match '$line'")
        DigPlanNode(DigDirection.entries[direction.toInt()], distance.toLong(16))
    }

private fun List<DigPlanNode>.trenchAreas(): List<Patch> {
    val patches = mutableListOf<Patch>()
    var x = 0L
    var y = 0L
    forEach { node ->
        when (node.direction) {
            U -> {
                patches += Patch(x, x + 1, y - node.length, y, TRENCH)
                y -= node.length
            }
            D -> {
                patches += Patch(x, x + 1, y + 1, y + node.length + 1, TRENCH)
                y += node.length
            }
            L -> {
                patches += Patch(x - node.length, x, y, y + 1, TRENCH)
                x -= node.length
            }
            R -> {
                patches += Patch(x + 1, x + 1 + node.length, y, y + 1, TRENCH)
                x += node.length
            }
        }
    }
    expect {
        that(x).isEqualTo(0)
        that(y).isEqualTo(0)
    }
    return patches
}

private fun List<Patch>.normalize(): List<List<Patch>> {
    val ys = (map(Patch::yMin) + map(Patch::yMax)).sorted().distinct()
    val xs = (map(Patch::xMin) + map(Patch::xMax)).sorted().distinct()
    val normalizedPatches = ys.zipWithNext { y0, y1 ->
        val row = filter { it.yMin <= y0 && y1 <= it.yMax }
        xs.zipWithNext { x0, x1 ->
            val column = row.filter { it.xMin <= x0 && x1 <= it.xMax }
            if (column.isEmpty()) {
                Patch(x0, x1, y0, y1, INSIDE)
            } else {
                expectThat(column).hasSize(1)
                Patch(x0, x1, y0, y1, TRENCH)
            }
        }
    }
    return normalizedPatches
}

private fun List<List<Patch>>.fillFromOutside() {
    do {
        var changed = false
        for (row in indices) {
            for (column in this[row].indices) {
                val area = this[row][column]
                if (area.terrain == INSIDE && isAnyNeighborOutside(row, column)) {
                    area.terrain = OUTSIDE
                    changed = true
                }
            }
        }
    } while (changed)
}

private fun List<List<Patch>>.volume(): Long =
    sumOf { row ->
        row.filter { it.terrain != OUTSIDE }.sumOf(Patch::volume)
    }

private fun Patch.volume(): Long =
    (xMax - xMin) * (yMax - yMin)

private fun List<List<Patch>>.isAnyNeighborOutside(row: Int, column: Int): Boolean =
    isOutside(row -1, column) || isOutside(row + 1, column) || isOutside(row, column - 1) || isOutside(row, column + 1)

private fun List<List<Patch>>.isOutside(row: Int, column: Int): Boolean =
    (getOrNull(row)?.getOrNull(column)?.terrain ?: OUTSIDE) == OUTSIDE
