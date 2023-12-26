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
    val plot = input.parsePlot()
    println("Part 1: ${plot.run(64).countOccupied()}")

    /*
        Part 2: With so many steps, it's not feasible to track individual locations with each step.

        Observations from analyzing the input file:
        - The start is in the center.
        - The border rows and columns have no rocks.
        - The input has a clear path from the start in the middle to its borders and its corners.
    */
    val center = input.size / 2
    expect {
        that(plot.size).isEqualTo(plot.first().size) // Plot is square.
        that(plot[center][center]).isEqualTo(START)
        that(plot[center].none { it == ROCK }) // There are no rocks in the center row.
        that(plot.map { it[center] }.none { it == ROCK }) // There are no rocks in the center column.
        that(plot.first().none { it == ROCK }) // There are no rocks in the borders.
        that(plot.last().none { it == ROCK })
        that(plot.map(List<Char>::first).none { it == ROCK })
        that(plot.map(List<Char>::last).none { it == ROCK })
    }

    /*
        The 65th step reaches the boundaries of the original plot square in the middle of the boundary, the 66th step crosses the border.
        The 130th step reaches the corners of the original plot square, the 132nd step reaches the diagonally opposite corner.
        Each horizontally or vertically adjacent tile is populated in the same way, starting in the middle of the boundary closest to the starting tile.
        Each diagonally adjacent tile is also populated in the same way, starting at the corner closest to the starting tile.
        Every (65+k*131+1)th step, a new E (N, S, W) tile starts the same cycle, so there are (steps-65)/131 tiles in each direction.
        Every (k*131+1)th step, a new NE (NW, SE, SW) tile starts the same cycle, so there (steps/131) tiles in each diagonal.

        26501365 = 202300 * 131 + 65, i.e. the required number of steps fills 202300 tiles in horizontal and vertical directions.
        Instead of repeating _s_ steps, we can fill _t_ full tiles so that _s = 131t + 65_
    */
    val steps = 26501365
    expectThat((steps - center) % plot.size).isEqualTo(0) // Chosen Steps fill full tiles in all directions.

    /*
        Assumption: the number of reachable spots whenever the "horizon" reaches the outer border of a tile is a function of the number of tiles.
        Because the horizon spreads in two dimensions, assume it is a quadratic function.

        By running the algorithm for the first few number of tiles, we can get known results, which we can then use to fit
        the quadratic function to those points and check if the function works for other data points as well.
    */
    val simulated = (0..4).map(plot::simulate)
    expect {
        that(simulated[0]).isEqualTo(3821)
        that(simulated[1]).isEqualTo(34234)
        that(simulated[2]).isEqualTo(94963)
        that(simulated[3]).isEqualTo(186008)
        that(simulated[4]).isEqualTo(307369)
    }

    /*
        Determine coefficients a, b, and c for a*x^2 + b*x + c = spots where x is the number of tiles and spots the simulated number of reachable spots.
        a * 0^2 + b * 0 + c = simulated[0]
        a * 1^2 + b * 1 + c = simulated[1]
        b = simulated[1] - c - a
        a * 2^2 + b * 2 + c = simulated[2]
        a * 4 + (simulated[1] - c - a) * 2 = simulated[2] - c
        a * (4 - 2) = simulated[2] - c - simulated[1] * 2 + c * 2
        a = (simulated[2] - simulated[1] * 2 + c) / 2
     */
    val c = simulated[0]
    val a = (simulated[2] - simulated[1] * 2 + c) / 2
    val b = simulated[1] - c - a

    fun f(tiles: Long): Long =
        a * tiles * tiles + b * tiles + c

    expect {
        that(f(3)).isEqualTo(simulated[3])
        that(f(4)).isEqualTo(simulated[4])
    }

    val tiles = (steps - center) / plot.size
    println("Part 2: ${f(tiles.toLong())}")
}

private const val GARDEN = '.'
private const val ROCK = '#'
private const val OCCUPIED = 'O'
private const val START = 'S'

private fun List<String>.parsePlot(): List<List<Char>> =
    map { line ->
        line.map { char -> char }
    }

private fun List<List<Char>>.run(steps: Int): List<List<Char>> =
    (1..steps).fold(this) { plot, _ -> plot.step() }

private fun List<List<Char>>.step(): List<List<Char>> =
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

private fun List<List<Char>>.simulate(tiles: Int): Long {
    var plot = expand(tiles)
    val steps = size / 2 + tiles * size
    for (step in 1..steps) {
        plot = plot.step()
    }
    return plot.countOccupied().toLong()
}

private fun List<List<Char>>.expand(tiles: Int): List<List<Char>> {
    val allocations = (2 * tiles + 1) * size
    val plot = List(allocations) { row ->
        MutableList(allocations) { column ->
            this[row % size][column % size].takeIf { it == ROCK } ?: GARDEN
        }
    }
    val center = tiles * size + size / 2
    plot[center][center] = 'O'
    return plot
}

private fun List<List<Char>>.isReachable(row: Int, column: Int): Boolean =
    isOccupied(row - 1, column) || isOccupied(row + 1, column) || isOccupied(row, column - 1) || isOccupied(row,column + 1)

private fun List<List<Char>>.isOccupied(row: Int, column: Int): Boolean =
    getOrNull(row)?.getOrNull(column) in setOf(START, OCCUPIED)

private fun List<List<Char>>.countOccupied(): Int =
    sumOf { row ->
        row.count { it == OCCUPIED }
    }
