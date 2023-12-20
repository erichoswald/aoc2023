import Direction17.*
import strikt.api.*
import strikt.assertions.*

fun main() {
    val crucibleRange = 1..3
    val ultraCrucibleRange = 4..10

    val testInput = listOf(
        "2413432311323",
        "3215453535623",
        "3255245654254",
        "3446585845452",
        "4546657867536",
        "1438598798454",
        "4457876987766",
        "3637877979653",
        "4654967986887",
        "4564679986453",
        "1224686865563",
        "2546548887735",
        "4322674655533",
    )

    expectThat(minimalLoss(testInput.toHeatLossMap(), crucibleRange)).isEqualTo(102)

    val input = readInput("Day17")
    println("Part 1: ${minimalLoss(input.toHeatLossMap(), crucibleRange)}")

    expectThat(minimalLoss(testInput.toHeatLossMap(), ultraCrucibleRange)).isEqualTo(94)
    val testInput2 = listOf(
        "111111111111",
        "999999999991",
        "999999999991",
        "999999999991",
        "999999999991",
    )
    expectThat(minimalLoss(testInput2.toHeatLossMap(), ultraCrucibleRange)).isEqualTo(71)

    println("Part 2: ${minimalLoss(input.toHeatLossMap(), ultraCrucibleRange)}")
}

private enum class Direction17(val rowInc: Int, val columnInc: Int) {
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1),
    UP(-1, 0),
}

private data class CrucibleMove(val row: Int, val column: Int, val direction: Direction17, val length: Int)

private fun List<String>.toHeatLossMap(): List<List<Int>> =
    map { line -> line.map(Char::digitToInt) }

private fun minimalLoss(heatLossMap: List<List<Int>>, lengthRange: IntRange): Int {
    val endRow = heatLossMap.lastIndex
    val endColumn = heatLossMap.last().lastIndex
    val accumulatedLoss = mutableMapOf<CrucibleMove, Int>()
    val visited = mutableSetOf<CrucibleMove>()
    val queue = mutableListOf<CrucibleMove>()

    for (direction in setOf(DOWN, RIGHT)) {
        val start = CrucibleMove(0, 0, direction, lengthRange.last)
        accumulatedLoss[start] = 0
        queue += start
    }

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        visited += current
        val moves = current.possibleMoves(lengthRange)
            .filter { (row, column) -> row in 0..endRow && column in 0..endColumn }
            .filterNot { it in visited }
        for (move in moves) {
            var loss = accumulatedLoss.getValue(current)
            var (row, column) = current
            do {
                row += move.direction.rowInc
                column += move.direction.columnInc
                loss += heatLossMap[row][column]
            } while (row != move.row || column != move.column)
            val previousBest = accumulatedLoss[move]
            if (previousBest == null || loss < previousBest) {
                accumulatedLoss[move] = loss
            }
            if (move !in visited && move !in queue) {
                queue += move
            }
        }

        queue.sortBy(accumulatedLoss::getValue)
    }

    return accumulatedLoss
        .filter { (key, _) -> key.row == endRow && key.column == endColumn }
        .values
        .min()
}

private fun CrucibleMove.possibleMoves(lengthRange: IntRange): List<CrucibleMove> {
    val flows = mutableListOf<CrucibleMove>()
    if (length < lengthRange.last) {
        flows += CrucibleMove(row + direction.rowInc, column + direction.columnInc, direction, length + 1)
    }
    if (length >= lengthRange.first) {
        when (direction) {
            DOWN, UP -> {
                flows += CrucibleMove(row, column - lengthRange.first, LEFT, lengthRange.first)
                flows += CrucibleMove(row, column + lengthRange.first, RIGHT, lengthRange.first)
            }

            LEFT, RIGHT -> {
                flows += CrucibleMove(row - lengthRange.first, column, UP, lengthRange.first)
                flows += CrucibleMove(row + lengthRange.first, column, DOWN, lengthRange.first)
            }
        }
    }
    return flows
}
