fun main() {
    Day<Int>(1)
        .part1(142, ::part1)
        .part2(281, ::part2)
}

private fun part1(input: List<String>): Int =
    input.sumOf { line ->
        val digits = line.filter(Char::isDigit)
        val firstDigit = digits.first().digitToInt()
        val lastDigit = digits.last().digitToInt()
        10 * firstDigit + lastDigit
    }

private fun part2(input: List<String>): Int =
    part1(input.map(::inlineDigitWords))

private fun inlineDigitWords(line: String): String {
    val sb = StringBuffer()
    var rest = line
    while (rest.isNotEmpty()) {
        val word = DigitWords.keys.firstOrNull(rest::startsWith)
        sb.append(DigitWords[word] ?: rest.first().toString())
        rest = rest.drop(1)
    }
    return sb.toString()
}

private val DigitWords = mapOf(
    "one" to "1",
    "two" to "2",
    "three" to "3",
    "four" to "4",
    "five" to "5",
    "six" to "6",
    "seven" to "7",
    "eight" to "8",
    "nine" to "9",
)
