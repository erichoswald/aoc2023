fun main() {
    day(3, ::part1, 4361, ::part2, 467835)
}

private fun part1(input: List<String>): Int =
    Scanner(input).partNumbers().sum()

private fun part2(input: List<String>): Int =
    Scanner(input).gearRatios().sum()

private data class Digits(val range: IntRange, val value: Int)

private class Scanner(val lines: List<String>) {
    private val digits = """\d+""".toRegex()
    private val gears = """\*""".toRegex()

    fun partNumbers(): List<Int> =
        lines.indices.flatMap(::findPartNumbers)

    fun gearRatios(): List<Int> =
        lines.indices.flatMap(::findGearRatios)

    private fun findPartNumbers(lineNo: Int): List<Int> =
        findDigits(lines[lineNo])
            .filter { it.isAdjacentToSymbol(lineNo) }
            .map { it.value }

    private fun findGearRatios(lineNo: Int): List<Int> {
        val digits = adjacentLines(lineNo).flatMap(::findDigits)
        return findGears(lines[lineNo])
            .map { column ->
                digits.filter { column in it.adjacentRange() }
                    .takeIf { it.size == 2 }
                    ?.let { (first, second) -> first.value * second.value }
                    ?: 0
            }
    }

    private fun findDigits(line: String): List<Digits> =
        digits.findAll(line).map { Digits(it.range, it.value.toInt()) }.toList()

    private fun findGears(line: String): List<Int> =
        gears.findAll(line).map { it.range.first }.toList()

    private fun adjacentLines(lineNo: Int): List<String> =
        ((lineNo - 1)..(lineNo + 1))
            .mapNotNull { lines.getOrElse(it) { "" } }

    private fun Digits.isAdjacentToSymbol(lineNo: Int): Boolean =
        adjacentLines(lineNo)
            .any { line -> symbolColumns(line).intersect(adjacentColumns()).isNotEmpty() }

    private fun Digits.adjacentColumns(): Set<Int> =
        adjacentRange().asIterable().toSet()

    private fun Digits.adjacentRange(): IntRange =
        ((range.first - 1)..(range.last + 1))

    private fun symbolColumns(input: String): Set<Int> {
        val columns = mutableSetOf<Int>()
        input.forEachIndexed { column, ch ->
            if (ch.isSymbol()) {
                columns += column
            }
        }
        return columns
    }

    private fun Char.isSymbol(): Boolean =
        this != '.' && !isDigit()
}
