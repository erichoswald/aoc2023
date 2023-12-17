import FlowDirection.*
import strikt.api.*
import strikt.assertions.*

fun main() {
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

    expectThat(minimalLoss(testInput)).isEqualTo(102)

    val input = readInput("Day17")
    println("Part 1: ${minimalLoss(input)}")
}

typealias LossMap = Map<Pair<Int, Int>, Int>

enum class FlowDirection(val rowInc: Int, val columnInc: Int) {
    DOWN(1, 0),
    LEFT(0, -1),
    RIGHT(0, 1),
    UP(-1, 0),
}

data class LavaFlow(val row: Int, val column: Int, val directions: List<FlowDirection>)
data class LavaPath(val loss: Int, val path: List<FlowDirection>)

private fun List<String>.toLossMap(): LossMap =
    flatMapIndexed { row, line ->
        line.mapIndexed { column, char ->
            Pair(row, column) to char.digitToInt()
        }
    }.toMap()

private fun minimalLoss(input: List<String>): Int =
    minimalLoss(input.toLossMap(), Pair(0, 0), Pair(input.lastIndex, input.last().lastIndex))

private fun minimalLoss(lossMap: LossMap, start: Pair<Int, Int>, end: Pair<Int, Int>): Int {
    val knownLosses = mutableMapOf<LavaFlow, LavaPath>().withDefault { LavaPath(Int.MAX_VALUE, emptyList()) }
    val visited = mutableSetOf<LavaFlow>()
    val queue = mutableListOf<LavaFlow>()

    for (direction in FlowDirection.entries) {
        val flow = LavaFlow(end.first, end.second, listOf(direction))
        knownLosses[flow] = LavaPath(0, emptyList())
        queue += flow
    }

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        visited += current
        val currentPath = knownLosses.getValue(current)
        val sources = current.directions.sources()
            .map { LavaFlow(current.row - it.rowInc, current.column - it.columnInc, (listOf(it) + current.directions).take(3)) }
            .filter { it.pair() in lossMap.keys }
            .filterNot { it in visited }
        for (source in sources) {
            val loss = currentPath.loss + lossMap.getValue(current.pair())
            val sourcePath = knownLosses[source]
            if (sourcePath == null || loss < sourcePath.loss) {
                knownLosses[source] = LavaPath(loss, source.directions)
            }
            queue += source
        }
        queue.sortBy { knownLosses.getValue(it).loss }
        while (queue.firstOrNull() in visited) {
            queue.removeFirst()
        }
    }

    return knownLosses
        .filter { (key, _) -> key.row == start.first && key.column == start.second }
        .values
        .minOf(LavaPath::loss)
}

private fun LavaFlow.pair(): Pair<Int, Int> =
    Pair(row, column)

private fun List<FlowDirection>.sources(): Set<FlowDirection> {
    val sources = mutableSetOf<FlowDirection>()
    if (isEmpty()) {
        for (direction in FlowDirection.entries) {
            sources += direction
        }
    } else {
        val direction = first()
        if (size < 3 || this[1] != direction || this[2] != direction) {
            sources += direction
        }
        sources += when (direction) {
            DOWN, UP -> setOf(LEFT, RIGHT)
            LEFT, RIGHT -> setOf(DOWN, UP)
        }
    }
    return sources
}
