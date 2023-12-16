import BeamDirection.*
import strikt.api.*
import strikt.assertions.*

fun main() {
    val testGrid = listOf(
        """.|...\....""",
        """|.-.\.....""",
        """.....|-...""",
        """........|.""",
        """..........""",
        """.........\""",
        """..../.\\..""",
        """.-.-/..|..""",
        """.|....-|.\""",
        """..//.|....""",
    )
    testReflections()
    expectThat(traverse(testGrid).countEnergized()).isEqualTo(46)

    val grid = readInput("Day16")
    println("Part 1: ${traverse(grid).countEnergized()}")

    expectThat(maximumEnergization(testGrid)).isEqualTo(51)
    println("Part 2: ${maximumEnergization(grid)}")
}

private fun testReflections() {
    expectThat(reflections(DOWN, '.')).containsExactly(DOWN)
    expectThat(reflections(DOWN, '/')).containsExactly(LEFT)
    expectThat(reflections(DOWN, '\\')).containsExactly(RIGHT)
    expectThat(reflections(DOWN, '|')).containsExactly(DOWN)
    expectThat(reflections(DOWN, '-')).containsExactly(LEFT, RIGHT)

    expectThat(reflections(LEFT, '.')).containsExactly(LEFT)
    expectThat(reflections(LEFT, '/')).containsExactly(DOWN)
    expectThat(reflections(LEFT, '\\')).containsExactly(UP)
    expectThat(reflections(LEFT, '|')).containsExactly(DOWN, UP)
    expectThat(reflections(LEFT, '-')).containsExactly(LEFT)

    expectThat(reflections(RIGHT, '.')).containsExactly(RIGHT)
    expectThat(reflections(RIGHT, '/')).containsExactly(UP)
    expectThat(reflections(RIGHT, '\\')).containsExactly(DOWN)
    expectThat(reflections(RIGHT, '|')).containsExactly(DOWN, UP)
    expectThat(reflections(RIGHT, '-')).containsExactly(RIGHT)

    expectThat(reflections(UP, '.')).containsExactly(UP)
    expectThat(reflections(UP, '/')).containsExactly(RIGHT)
    expectThat(reflections(UP, '\\')).containsExactly(LEFT)
    expectThat(reflections(UP, '|')).containsExactly(UP)
    expectThat(reflections(UP, '-')).containsExactly(LEFT, RIGHT)
}

private enum class BeamDirection(val rowInc: Int, val columnInc: Int) {
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1),
    UP(-1, 0),
}

private data class Beam(val row: Int, val column: Int, val direction: BeamDirection)

private fun maximumEnergization(grid: List<String>): Int =
    sequence {
        for (row in grid.indices) {
            yield(Beam(row, 0, RIGHT))
            yield(Beam(row, grid.first().lastIndex, LEFT))
        }
        for (column in grid.first().indices) {
            yield(Beam(0, column, DOWN))
            yield(Beam(grid.lastIndex, column, UP))
        }
    }.maxOf { beam ->
        traverse(grid, beam.row, beam.column, beam.direction).countEnergized()
    }

private fun traverse(
    grid: List<String>,
    row: Int = 0,
    column: Int = 0,
    direction: BeamDirection = RIGHT
): List<List<Set<BeamDirection>>> {
    val visited = List(grid.size) {
        MutableList(grid.first().length) {
            mutableSetOf<BeamDirection>()
        }
    }
    traverse(grid, row, column, direction, visited)
    return visited
}

private fun traverse(
    grid: List<String>,
    row: Int,
    column: Int,
    direction: BeamDirection,
    visited: List<MutableList<MutableSet<BeamDirection>>>,
) {
    if (grid.contains(row, column) && direction !in visited[row][column]) {
        visited[row][column] += direction
        for (reflection in reflections(direction, grid[row][column])) {
            traverse(grid, row + reflection.rowInc, column + reflection.columnInc, reflection, visited)
        }
    }
}

private fun reflections(direction: BeamDirection, tile: Char): Set<BeamDirection> =
    when (tile) {
        '/' -> when (direction) {
            DOWN -> setOf(LEFT)
            LEFT -> setOf(DOWN)
            RIGHT -> setOf(UP)
            UP -> setOf(RIGHT)
        }
        '\\' -> when (direction) {
            DOWN -> setOf(RIGHT)
            LEFT -> setOf(UP)
            RIGHT -> setOf(DOWN)
            UP -> setOf(LEFT)
        }
        '|' -> when (direction) {
            DOWN, UP -> setOf(direction)
            LEFT, RIGHT -> setOf(DOWN, UP)
        }
        '-' -> when (direction) {
            DOWN, UP -> setOf(LEFT, RIGHT)
            LEFT, RIGHT -> setOf(direction)
        }
        else -> setOf(direction)
    }

private fun List<String>.contains(row: Int, column: Int): Boolean =
    row in indices && column in first().indices

private fun List<List<Set<BeamDirection>>>.countEnergized(): Int =
    sumOf { rows ->
        rows.sumOf { traversed ->
            val energized = if (traversed.isNotEmpty()) 1 else 0
            energized
        }
    }
