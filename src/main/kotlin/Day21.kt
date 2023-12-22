import strikt.api.*
import strikt.assertions.*

/*
Part 2: With so many steps, it's not feasible to track individual locations with each step.
The number of reachable spots increases with each step (assuming the starting spot is not within an area bounded by rocks).

Observations from analyzing the input file:

The border rows and columns have no rocks.
The input has a clear path from the start in the middle to its borders and its corners.
The 65th step reaches the boundaries of the original plot square in the middle of the boundary, the 66th step crosses the border.
The 130th step reaches the corners of the original plot square, the 132nd step reaches the diagonally opposite corner.
Each horizontally or vertically adjacent tile is populated in the same way, starting in the middle of the boundary closest to the starting tile.
Each diagonally adjacent tile is also populated in the same way, starting at the corner closest to the starting tile.
Every (65+k*131+1)th step, a new E (N, S, W) tile starts the same cycle, so there are (steps-65)/131 tiles in each direction.
Every (k*131+1)th step, a new NE (NW, SE, SW) tile starts the same cycle, so there (steps/131) tiles in each diagonal.
26501365 = 202300 * 131 + 65
*/

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

//    input.parsePlot().simulate(65 + 131*4)
    /*
        Running the simulation yields:
        65: 3821
        65 + 131: 34234
        65 + 131*2: 94963
        65 + 131*3: 186008
        65 + 131*4: 307369

        Assumption: Number of reachable spots grows as a quadratic function (expanding in two dimensions),
        at least each time we reach the border of the next set of tiles as the step number implies.

        a*x^2 + b*x + c = n

        a*65*65 + b*65 + c = 3821
        c = 3821 - 65*b - 4225*a

        a*(65 + 131)*(65 + 131) + b*(65 + 131) + c = 34234
        a*196*196 + b*196 + 3821 - 65*b - 4225*a = 34234
        a*(196*196 - 4225) + b*(196 - 65) = 34234 - 3821
        a*34191 + b*131 = 30413
        b = (30413 - a*34191) / 131

        a*(65 + 131*2)(65 + 131*2) + b*(65 + 131*2) + c = 94963
        a*106929 + b*327 + c = 94963
        a*106929 + ((30413 - a*34191) / 131)*327 + 3821 - 65*((30413 - a*34191) / 131) - a*4225 = 94963
        a*106929 + 327*30413/131 - a*327*34191/131 + 3821 - 65*30413/131 + a*65*34191/131 - a*4225 = 94963
        a*(106929 - 327*34191/131 + 65*34191/131 - 4225) = 94963 - 327*30413/131 - 3821 + 65*30413/131
        a = (91142 - 327*30413/131 + 65*30413/131) / (106929 - 327*34191/131 + 65*34191/131 - 4225)
        a = (91142 - 262*30413/131) / (102704 - 262*34191/131)
        a = (91142 - 2*30413) / (102704 - 2*34191) = (91142 - 60826) / (102704 - 68382) = 30316 / 34322
     */
    for (tiles in 0L..4L) {
        println("$tiles: ${compute(tiles)}")
    }

    println("Part 2: ${compute(202300)}")
}

private const val A = 30316.0 / 34322.0
private const val B = (30413.0 - A*34191.0) / 131.0
private const val C = 3821.0 - 65.0*B - 4225.0*A

private fun compute(tiles: Long): Long {
    val steps = (65L + 131L * tiles).toDouble()
    return Math.round(steps * steps * A + steps * B + C)
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

private fun List<List<Char>>.simulate(steps: Int) {
    var plot = explode(steps)
    for (step in 1..steps) {
        plot = plot.step()
    }
    println("$steps: ${plot.countOccupied()}")
}

private fun List<List<Char>>.explode(steps: Int): List<List<Char>> {
    expectThat(size).isEqualTo(131)
    val tiles = (steps - 65) / 131
    val allocations = (2 * tiles + 1) * 131
    val plot = List(allocations) { row ->
        MutableList(allocations) { column ->
            this[row % 131][column % 131].takeIf { it == ROCK } ?: GARDEN
        }
    }
    val center = tiles * 131 + 65
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
