import strikt.api.*
import strikt.assertions.*

fun main() {
    val test1 = listOf(
        "..#.",
        ".....",
        "#..#",
    )
    expectThat(findGalaxies(test1)).containsExactlyInAnyOrder(Galaxy(0, 2), Galaxy(2, 0), Galaxy(2, 3))

    val test2 = listOf(
        "...#......",
        ".......#..",
        "#.........",
        "..........",
        "......#...",
        ".#........",
        ".........#",
        "..........",
        ".......#..",
        "#...#.....",
    )

    expectThat(rowWeights(test2)).containsExactly(1, 1, 1, 2, 1, 1, 1, 2, 1, 1)
    expectThat(columnWeights(test2)).containsExactly(1, 1, 2, 1, 1, 2, 1, 1, 2, 1)
    expectThat(shortestDistance(Galaxy(5, 1), Galaxy(9, 4), rowWeights(test2), columnWeights(test2))).isEqualTo(9)
    expectThat(shortestDistance(Galaxy(0, 3), Galaxy(8, 7), rowWeights(test2), columnWeights(test2))).isEqualTo(15)
    expectThat(shortestDistance(Galaxy(2, 0), Galaxy(6, 9), rowWeights(test2), columnWeights(test2))).isEqualTo(17)
    expectThat(shortestDistance(Galaxy(6, 9), Galaxy(2, 0), rowWeights(test2), columnWeights(test2))).isEqualTo(17)
    expectThat(shortestDistance(Galaxy(0, 0), Galaxy(0, 0), listOf(1), listOf(1))).isEqualTo(0)

    expectThat(sumShortestDistances(test2, emptySpaceWeight = 2)).isEqualTo(374)

    val input = readInput("Day11")
    println("part 1: ${sumShortestDistances(input, emptySpaceWeight = 2)}")

    expectThat(sumShortestDistances(test2, emptySpaceWeight = 10)).isEqualTo(1030)
    expectThat(sumShortestDistances(test2, emptySpaceWeight = 100)).isEqualTo(8410)

    println("part 2: ${sumShortestDistances(input, emptySpaceWeight = 1_000_000)}")
}

private data class Galaxy(val row: Int, val column: Int)

private fun rowWeights(input: List<String>, emptySpaceWeight: Long = 2L): List<Long> =
    input.map { row ->
        if (row.any(Char::isGalaxy)) 1L else emptySpaceWeight
    }

private fun columnWeights(input: List<String>, emptySpaceWeight: Long = 2L): List<Long> =
    (0..<input.maxOf(String::length)).map { column ->
        if (input.column(column).any(Char::isGalaxy)) 1L else emptySpaceWeight
    }

private fun sumShortestDistances(input: List<String>, emptySpaceWeight: Long): Long {
    val rowWeights = rowWeights(input, emptySpaceWeight)
    val columnWeights = columnWeights(input, emptySpaceWeight)
    val galaxies = findGalaxies(input)
    val twice = galaxies.sumOf { from ->
        galaxies.sumOf { to ->
            shortestDistance(from, to, rowWeights, columnWeights)
        }
    }
    return twice / 2L // Each distance was counted twice per galaxy pair.
}

private fun findGalaxies(input: List<String>): List<Galaxy> {
    val coords = mutableListOf<Galaxy>()
    input.forEachIndexed { rowIndex, row ->
        row.forEachIndexed { columnIndex, ch ->
            if (ch.isGalaxy()) {
                coords += Galaxy(rowIndex, columnIndex)
            }
        }
    }
    return coords
}

private fun shortestDistance(from: Galaxy, to: Galaxy, rowWeights: List<Long>, columnWeights: List<Long>): Long {
    val rowMin = minOf(from.row, to.row)
    val rowMax = maxOf(from.row, to.row)
    val columnMin = minOf(from.column, to.column)
    val columnMax = maxOf(from.column, to.column)
    val rowDistance = (rowMin..<rowMax).sumOf(rowWeights::get)
    val columnDistance = (columnMin..<columnMax).sumOf(columnWeights::get)
    return rowDistance + columnDistance
}

private fun List<String>.column(n: Int): List<Char> =
    map { it.getOrNull(n) ?: '.' }

private fun Char.isGalaxy(): Boolean =
    this == '#'
