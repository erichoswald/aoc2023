import strikt.api.*
import strikt.assertions.*

fun main() {
    expectThat(testIsland.longestHike(testIsland.findStartCoords(), emptySet(), 0)).isEqualTo(94)

    val input = readInput("Day23")
    println("Part 1: ${input.longestHike(input.findStartCoords(), emptySet(), 0)}")
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

private data class IslandCoords(val row: Int, val column: Int)

private fun Island.findStartCoords(): IslandCoords =
    IslandCoords(0, first().indexOf("."))

private fun Island.isAtGoal(coords: IslandCoords): Boolean =
    coords.row == lastIndex && this[coords] == PATH

private fun Island.longestHike(from: IslandCoords, visited: Set<IslandCoords>, length: Int): Int? =
    if (isAtGoal(from)) {
        length
    } else {
        when (this[from]) {
            FOREST -> emptyList()
            PATH -> listOfNotNull(north(from), east(from), south(from), west(from))
            NORTH -> listOfNotNull(north(from))
            EAST -> listOfNotNull(east(from))
            SOUTH -> listOfNotNull(south(from))
            WEST -> listOfNotNull(west(from))
            else -> error("Unexpected island terrain at $from: '${this[from]}'")
        }
            .filterNot { it in visited }
            .mapNotNull { to -> longestHike(to, visited + from, length + 1) }
            .maxOrNull()
    }

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
