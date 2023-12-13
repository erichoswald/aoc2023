import strikt.api.*
import strikt.assertions.*

fun main() {
    val test1 = listOf(
        "#.##..##.",
        "..#.##.#.",
        "##......#",
        "##......#",
        "..#.##.#.",
        "..##..##.",
        "#.#.##.#.",
    )
    val test2 = listOf(
        "#...##..#",
        "#....#..#",
        "..##..###",
        "#####.##.",
        "#####.##.",
        "..##..###",
        "#....#..#",
    )

    expectThat(listOf("#.#", "##.").transpose()).isEqualTo(listOf("##", ".#", "#."))

    expectThat(mirrorColumns(test1, smudgeCorrections = 0)).isEqualTo(5)
    expectThat(mirrorRows(test2, smudgeCorrections = 0)).isEqualTo(4)

    expectThat(parsePatterns(test1 + listOf("") + test2)).isEqualTo(listOf(test1, test2))
    expectThat(sumOfPatternValues(listOf(test1, test2), smudgeCorrections = 0)).isEqualTo(405)

    val patterns = parsePatterns(readInput("Day13"))
    println("Part 1: ${sumOfPatternValues(patterns, smudgeCorrections = 0)}")

    expectThat(mirrorRows(test1, smudgeCorrections = 1)).isEqualTo(3)
    expectThat(mirrorRows(test2, smudgeCorrections = 1)).isEqualTo(1)
    expectThat(sumOfPatternValues(listOf(test1, test2), smudgeCorrections = 1)).isEqualTo(400)

    println("Part 2: ${sumOfPatternValues(patterns, smudgeCorrections = 1)}")
}

private fun sumOfPatternValues(patterns: List<List<String>>, smudgeCorrections: Int): Int =
    patterns.sumOf { pattern -> patternValue(pattern, smudgeCorrections) }

private fun patternValue(pattern: List<String>, smudgeCorrections: Int): Int =
    mirrorRows(pattern, smudgeCorrections)?.times(100) ?: mirrorColumns(pattern, smudgeCorrections) ?: 0

private fun parsePatterns(input: List<String>): List<List<String>> {
    val patterns = mutableListOf<List<String>>()
    var start = 0
    while (start < input.size) {
        var end = start
        while (!input.getOrNull(end).isNullOrBlank()) {
            end++
        }
        patterns += input.subList(start, end)
        start = end + 1
    }
    return patterns
}

private fun mirrorRows(pattern: List<String>, smudgeCorrections: Int): Int? {
    for (gap in 1..pattern.lastIndex) {
        var smudgeCount = 0
        for (offset in 1..pattern.size) {
            val top = pattern.getOrNull(gap - offset)
            val bottom = pattern.getOrNull(gap + offset - 1)
            if (top != null && bottom != null && top != bottom) {
                smudgeCount += errorDistance(top, bottom)
            }
        }
        if (smudgeCount == smudgeCorrections) {
            return gap
        }
    }
    return null
}

private fun errorDistance(a: String, b: String): Int =
    a.zip(b) { ac, bc -> if (ac == bc) 0 else 1 }.sum()

fun mirrorColumns(pattern: List<String>, smudgeCorrections: Int): Int? =
    mirrorRows(pattern.transpose(), smudgeCorrections)

private fun List<String>.transpose(): List<String> {
    val length = minOf(String::length)
    val transposed = mutableListOf<String>()
    for (column in 0..<length) {
        val sb = StringBuilder()
        for (row in indices) {
            sb.append(this[row][column])
        }
        transposed += sb.toString()
    }
    return transposed
}
