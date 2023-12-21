import PlotAllocation.*
import strikt.api.*
import strikt.assertions.*

fun main() {
    val testInput = listOf(
        "...........",
        ".....###.#.",
        ".###.##..#.",
        "..#.#...#..",
        "....#.#....",
        ".##..S####.",
        ".##..#...#.",
        ".......##..",
        ".##.#.####.",
        ".##..##.##.",
        "...........",
    )
    val testPlot = testInput.parsePlot()

    expectThat(testPlot.run(1).countOccupied()).isEqualTo(2)
    expectThat(testPlot.run(2).countOccupied()).isEqualTo(4)
    expectThat(testPlot.run(3).countOccupied()).isEqualTo(6)
    expectThat(testPlot.run(6).countOccupied()).isEqualTo(16)

    val input = readInput("Day21")
    println("Part 1: ${input.parsePlot().run(64).countOccupied()}")
}

private enum class PlotAllocation {
    GARDEN, ROCK, START, OCCUPIED
}

private fun List<String>.parsePlot(): List<List<PlotAllocation>> =
    map { line ->
        line.map { char ->
            when (char) {
                '.' -> GARDEN
                '#' -> ROCK
                'S' -> START
                else -> error("Cannot parse plot allocation: '$char'")
            }
        }
    }

private fun List<List<PlotAllocation>>.run(steps: Int): List<List<PlotAllocation>> =
    (1..steps).fold(this) { plot, _ -> plot.step() }

private fun List<List<PlotAllocation>>.step(): List<List<PlotAllocation>> =
    mapIndexed { row, line ->
        line.mapIndexed { column, allocation ->
            if (allocation == ROCK) {
                ROCK
            } else if (isReachable(row, column)) {
                OCCUPIED
            } else {
                GARDEN
            }
        }
    }

private fun List<List<PlotAllocation>>.isReachable(row: Int, column: Int): Boolean =
    isOccupied(row - 1, column) || isOccupied(row + 1, column) || isOccupied(row, column - 1) || isOccupied(row,column + 1)

private fun List<List<PlotAllocation>>.isOccupied(row: Int, column: Int): Boolean =
    getOrNull(row)?.getOrNull(column) in setOf(START, OCCUPIED)

private fun List<List<PlotAllocation>>.countOccupied(): Int =
    sumOf { row ->
        row.count { it == OCCUPIED }
    }
