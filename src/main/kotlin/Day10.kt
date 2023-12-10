import strikt.api.*
import strikt.assertions.*

fun main() {
    val test1 = listOf(
        "-L|F7",
        "7S-7|",
        "L|7||",
        "-L-J|",
        "L|-JF",
    )
    val test2 = listOf(
        "7-F7-",
        ".FJ|7",
        "SJLL7",
        "|F--J",
        "LJ.LJ",
    )

    expectThat(findStart(test1)).isEqualTo(Coord(1, 1))
    expectThat(findStart(test2)).isEqualTo(Coord(2, 0))

    expectThat(findConnections(test1, Coord(1, 1))).containsExactlyInAnyOrder(Coord(1, 2), Coord(2, 1))
    expectThat(findConnections(test1, Coord(1, 2))).containsExactlyInAnyOrder(Coord(1, 1), Coord(1, 3))
    expectThat(findConnections(test1, Coord(1, 3))).containsExactlyInAnyOrder(Coord(1, 2), Coord(2, 3))
    expectThat(findConnections(test1, Coord(2, 3))).containsExactlyInAnyOrder(Coord(1, 3), Coord(3, 3))
    expectThat(findConnections(test1, Coord(3, 3))).containsExactlyInAnyOrder(Coord(2, 3), Coord(3, 2))
    expectThat(findConnections(test1, Coord(3, 2))).containsExactlyInAnyOrder(Coord(3, 3), Coord(3, 1))
    expectThat(findConnections(test1, Coord(3, 1))).containsExactlyInAnyOrder(Coord(3, 2), Coord(2, 1))
    expectThat(findConnections(test1, Coord(2, 1))).containsExactlyInAnyOrder(Coord(3, 1), Coord(1, 1))

    expectThat(maxDistance(test1)).isEqualTo(4)
    expectThat(maxDistance(test2)).isEqualTo(8)

    val input = readInput("Day10")
    println("part1: ${maxDistance(input)}")

    val test3 = listOf(
        "..........",
        ".S------7.",
        ".|F----7|.",
        ".||....||.",
        ".||....||.",
        ".|L-7F-J|.",
        ".|..||..|.",
        ".L--JL--J.",
        "..........",
    )
    expectThat(enclosureSize(test3)).isEqualTo(4)

    val test4 = listOf(
        ".F----7F7F7F7F-7....",
        ".|F--7||||||||FJ....",
        ".||.FJ||||||||L7....",
        "FJL7L7LJLJ||LJ.L-7..",
        "L--J.L7...LJS7F-7L7.",
        "....F-J..F7FJ|L7L7L7",
        "....L7.F7||L7|.L7L7|",
        ".....|FJLJ|FJ|F7|.LJ",
        "....FJL-7.||.||||...",
        "....L---J.LJ.LJLJ...",
    )
    expectThat(enclosureSize(test4)).isEqualTo(8)

    val test5 = listOf(
        "FF7FSF7F7F7F7F7F---7",
        "L|LJ||||||||||||F--J",
        "FL-7LJLJ||||||LJL-77",
        "F--JF--7||LJLJ7F7FJ-",
        "L---JF-JLJ.||-FJLJJ7",
        "|F|F-JF---7F7-L7L|7|",
        "|FFJF7L7F-JF7|JL---7",
        "7-L-JL7||F7|L7F-7F7|",
        "L.L7LFJ|||||FJL7||LJ",
        "L7JLJL-JLJLJL--JLJ.L",
    )
    expectThat(enclosureSize(test5)).isEqualTo(10)

    println("part2: ${enclosureSize(input)}")
}

private data class Coord(val row: Int, val column: Int)

private val North = Coord(-1, 0)
private val South = Coord(1, 0)
private val East = Coord(0, 1)
private val West = Coord(0, -1)

private val Neighbors = mapOf(
    '|' to listOf(North, South),
    '-' to listOf(East, West),
    'L' to listOf(North, East),
    'J' to listOf(North, West),
    'F' to listOf(South, East),
    '7' to listOf(South, West),
    '.' to listOf(),
    'S' to listOf(North, South, East, West),
)

private val Connections = mapOf(
    North to setOf('|', 'F', '7', 'S'),
    South to setOf('|', 'J', 'L', 'S'),
    East to setOf('-', 'J', '7', 'S'),
    West to setOf('-', 'F', 'L', 'S'),
)

private val StartReplacement = mapOf(
    setOf(North, South) to '|',
    setOf(East, West) to '-',
    setOf(North, East) to 'L',
    setOf(North, West) to 'J',
    setOf(South, East) to 'F',
    setOf(South, West) to '7',
)

private fun maxDistance(map: List<String>): Int =
    traverse(map).first

private fun enclosureSize(map: List<String>): Int {
    val loopCoords = traverse(map).second
    var size = 0
    for (row in map.indices) {
        size += insideCount(map, row, loopCoords)
    }
    return size
}

private fun insideCount(map: List<String>, row: Int, loopCoords: Set<Coord>): Int {
    val line = map[row]
    var isInside = false
    var n = 0
    var pending: Char? = null
    for (column in line.indices) {
        if (Coord(row, column) in loopCoords) {
            val ch = line[column]
                .takeIf { it != 'S' }
                ?: startReplacement(map, Coord(row, column))
            when (ch) {
                '|' -> isInside = !isInside
                'F', 'L' -> pending = ch
                'J' -> if (pending == 'F') isInside = !isInside
                '7' -> if (pending == 'L') isInside = !isInside
            }
        } else if (isInside) {
            n++
        }
    }
    return n
}

private fun traverse(map: List<String>): Pair<Int, Set<Coord>> {
    val start = findStart(map)
    var coords = setOf(start)
    val loopCoords = mutableSetOf<Coord>()
    var distance = 0
    do {
        distance += 1
        loopCoords += coords
        coords = coords.flatMap { findConnections(map, it) }.toSet() - loopCoords
    } while (coords.size > 1)
    loopCoords += coords
    return Pair(distance, loopCoords)
}

private fun findStart(map: List<String>): Coord {
    map.forEachIndexed { row, line ->
        val column = line.indexOf('S')
        if (column >= 0) {
            return Coord(row, column)
        }
    }
    error("No starting point found")
}

private fun startReplacement(map: List<String>, coord: Coord): Char {
    val directions = Neighbors.getValue('S')
        .filter { direction -> map.at(coord + direction) in Connections.getValue(direction) }
        .toSet()
    return StartReplacement.getValue(directions)
}

private fun findConnections(map: List<String>, coord: Coord): List<Coord> =
    Neighbors.getValue(map.at(coord)).mapNotNull { direction ->
        (coord + direction).takeIf { map.at(it) in Connections.getValue(direction) }
    }

private operator fun Coord.plus(other: Coord): Coord =
    Coord(row + other.row, column + other.column)

private fun List<String>.at(coord: Coord): Char =
    getOrNull(coord.row)?.getOrNull(coord.column) ?: '.'
