import strikt.api.*
import strikt.assertions.*

fun main() {
    expectThat(testIsland.graph().longestHike(testIsland.startCoords(), testIsland.goalCoords())).isEqualTo(94)

    val input = readInput("Day23")
    println("Part 1: ${input.graph().longestHike(input.startCoords(), input.goalCoords())}")

    expectThat(testIsland.withoutSlopes().graph().longestHike(testIsland.startCoords(), testIsland.goalCoords())).isEqualTo(154)

    println("Part 2: ${input.withoutSlopes().graph().longestHike(input.startCoords(), input.goalCoords())}")
}

private const val FOREST = '#'
private const val PATH = '.'
private const val NORTH = '^'
private const val EAST = '>'
private const val SOUTH = 'v'
private const val WEST = '<'

private val testIsland = listOf(
    "#.#####################",
    "#.......#########...###",
    "#######.#########.#.###",
    "###.....#.>.>.###.#.###",
    "###v#####.#v#.###.#.###",
    "###.>...#.#.#.....#...#",
    "###v###.#.#.#########.#",
    "###...#.#.#.......#...#",
    "#####.#.#.#######.#.###",
    "#.....#.#.#.......#...#",
    "#.#####.#.#.#########v#",
    "#.#...#...#...###...>.#",
    "#.#.#v#######v###.###v#",
    "#...#.>.#...>.>.#.###.#",
    "#####v#.#.###v#.#.###.#",
    "#.....#...#...#.#.#...#",
    "#.#########.###.#.#.###",
    "#...###...#...#...#.###",
    "###.###.#.###v#####v###",
    "#...#...#.#.>.>.#.>.###",
    "#.###.###.#.###.#.#v###",
    "#.....###...###...#...#",
    "#####################.#",
)

private typealias Island = List<String>
private typealias IslandGraph = Map<IslandCoords, List<IslandPath>>

private data class IslandCoords(val row: Int, val column: Int)
private data class IslandPath(val from: IslandCoords, val to: IslandCoords, val length: Int)

private fun Island.graph(): IslandGraph {
    val graph = mutableMapOf<IslandCoords, MutableList<IslandPath>>()
    addToGraph(startCoords(), graph)
    return graph
}

private fun Island.addToGraph(from: IslandCoords, graph: MutableMap<IslandCoords, MutableList<IslandPath>>) {
    if (from !in graph.keys) {
        for (to in neighbors(from)) {
            val path = pathToNextCrossing(to, setOf(from))
            val crossing = path.last()
            graph.getOrPut(from, ::mutableListOf) += IslandPath(from, crossing, path.lastIndex)
            addToGraph(crossing, graph)
        }
    }
}

private tailrec fun Island.pathToNextCrossing(last: IslandCoords, path: Set<IslandCoords>): List<IslandCoords> {
    val singleUnvisitedNeighbor = (neighbors(last) - path).singleOrNull()
    return if (singleUnvisitedNeighbor != null) {
        pathToNextCrossing(singleUnvisitedNeighbor, path + last)
    } else {
        (path + last).toList()
    }
}

private fun IslandGraph.longestHike(from: IslandCoords, goal: IslandCoords): Int? =
    longestHike(from, goal, emptySet(), 0)

private fun IslandGraph.longestHike(from: IslandCoords, goal: IslandCoords, visited: Set<IslandCoords>, length: Int): Int? =
    if (from == goal) {
        length
    } else {
        val paths = this[from].orEmpty()
        val unvisited = paths.filterNot { it.to in visited }
        unvisited.mapNotNull { path -> longestHike(path.to, goal, visited + path.from, length + path.length) }
            .maxOrNull()
    }

private fun Island.startCoords(): IslandCoords =
    IslandCoords(0, first().indexOf(PATH))

private fun Island.goalCoords(): IslandCoords =
    IslandCoords(lastIndex, last().indexOf(PATH))

private fun Island.withoutSlopes(): Island =
    map { row ->
        row.map { terrain -> terrain.takeIf { it == FOREST } ?: PATH }.joinToString("")
    }

private fun Island.neighbors(from: IslandCoords): List<IslandCoords> =
    when (this[from]) {
        FOREST -> emptyList()
        PATH -> listOfNotNull(north(from), east(from), south(from), west(from))
        NORTH -> listOfNotNull(north(from))
        EAST -> listOfNotNull(east(from))
        SOUTH -> listOfNotNull(south(from))
        WEST -> listOfNotNull(west(from))
        else -> error("Unexpected island terrain at $from: '${this[from]}'")
    }.filter { this[it] != FOREST }

private operator fun Island.get(coords: IslandCoords): Char =
    this[coords.row][coords.column]

private fun Island.north(coords: IslandCoords): IslandCoords? =
    coords.copy(row = coords.row - 1).takeIf { coords.row > 0 }

private fun Island.east(coords: IslandCoords): IslandCoords? =
    coords.copy(column = coords.column + 1).takeIf { coords.column < get(coords.row).lastIndex }

private fun Island.south(coords: IslandCoords): IslandCoords? =
    coords.copy(row = coords.row + 1).takeIf { coords.row < lastIndex }

private fun Island.west(coords: IslandCoords): IslandCoords? =
    coords.copy(column = coords.column - 1).takeIf { coords.column > 0 }
