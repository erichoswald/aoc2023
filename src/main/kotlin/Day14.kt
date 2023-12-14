import Allocation.*
import strikt.api.*
import strikt.assertions.*

private const val CYCLES = 1_000_000_000L

fun main() {
    val test = listOf(
        "O....#....",
        "O.OO#....#",
        ".....##...",
        "OO.#O....O",
        ".O.....O#.",
        "O.#..O.#.#",
        "..O..#O..O",
        ".......O..",
        "#....###..",
        "#OO..#....",
    )

    val testPlatform = parsePlatform(test)
    expectThat(testPlatform.tilt().render())
        .isEqualTo(
            listOf(
                "OOOO.#.O..",
                "OO..#....#",
                "OO..O##..O",
                "O..#.OO...",
                "........#.",
                "..#....#.#",
                "..O..#.O.O",
                "..O.......",
                "#....###..",
                "#....#....",
            ),
        )
    expectThat(testPlatform.tilt().load()).isEqualTo(136)

    val input = readInput("Day14")
    val platform = parsePlatform(input)
    println("Part 1: ${platform.tilt().load()}")

    expectThat(parsePlatform(listOf("#.", ".O")).rotate().render())
        .isEqualTo(listOf(".#", "O."))

    expectThat(testPlatform.tilt().rotate().render())
        .isEqualTo(
            listOf(
                "##....OOOO",
                ".......OOO",
                "..OO#....O",
                "......#..O",
                ".......O#.",
                "##.#..O#.#",
                ".#....O#..",
                ".#.O#....O",
                ".....#....",
                "...O#..O#.",
            ),
        )
    val cycledTestPlatform = testPlatform.tilt().rotate().tilt().rotate().tilt().rotate().tilt().rotate()
    expectThat(cycledTestPlatform.render())
        .isEqualTo(
            listOf(
                ".....#....",
                "....#...O#",
                "...OO##...",
                ".OO#......",
                ".....OOO#.",
                ".O#...O#.#",
                "....O#....",
                "......OOOO",
                "#...O###..",
                "#..OO#....",
            ),
        )

    expectThat(testPlatform.cycle(CYCLES).load()).isEqualTo(64)

    println("Part 2: ${platform.cycle(CYCLES).load()}")
}

private enum class Allocation(val char: Char) {
    ROUNDED('O'), CUBED('#'), FREE('.');

    companion object {
        fun from(char: Char): Allocation =
            when (char) {
                'O' -> ROUNDED
                '#' -> CUBED
                '.' -> FREE
                else -> error("Unexpected allocation '$char'")
            }
    }
}

private data class Platform(private val allocations: List<MutableList<Allocation>>) {

    val rows = allocations.size
    val columns = allocations.first().size

    fun copy(): Platform =
        Platform(allocations.map { it.toList().toMutableList() })

    operator fun get(row: Int, column: Int): Allocation =
        allocations[row][column]

    operator fun set(row: Int, column: Int, allocation: Allocation) {
        allocations[row][column] = allocation
    }

    private fun swap(row0: Int, column: Int) {
        val t = this[row0, column]
        this[row0, column] = this[row0 + 1, column]
        this[row0 + 1, column] = t
    }

    fun cycle(times: Long = 1L): Platform {
        val lastSeen = mutableMapOf<Platform, Long>()
        var platform = this
        var count = 0L
        while (count < times) {
            val seen = lastSeen[platform]
            if (seen != null) {
                val cycle = count - seen
                val rounds = (times - count) / cycle
                count += rounds * cycle
            }
            lastSeen[platform] = count++
            platform = platform
                .tilt().rotate()
                .tilt().rotate()
                .tilt().rotate()
                .tilt().rotate()
        }
        return platform
    }

    fun tilt(): Platform =
        copy().apply {
            for (row in 1..<rows) {
                for (column in 0..<columns) {
                    if (this[row, column] == ROUNDED) {
                        var r = row - 1
                        while (r >= 0 && get(r, column) == FREE) {
                            swap(r--, column)
                        }
                    }
                }
            }
        }

    fun rotate(): Platform =
        copy().also { rotated ->
            for (row in 0..<rows) {
                for (column in 0..<columns) {
                    rotated[column, rows - 1 - row] = this[row, column]
                }
            }
        }

    fun load(): Int {
        var totalWeight = 0
        for (row in 0..<rows) {
            val rowWeight = rows - row
            for (column in 0..<columns) {
                if (this[row, column] == ROUNDED) {
                    totalWeight += rowWeight
                }
            }
        }
        return totalWeight
    }

    fun render(): List<String> =
        allocations.map { row ->
            val sb = StringBuilder()
            row.forEach { sb.append(it.char) }
            sb.toString()
        }
}

private fun parsePlatform(input: List<String>): Platform =
    Platform(
        input.map { line ->
            line.map { char ->
                Companion.from(char)
            }.toMutableList()
        },
    )
