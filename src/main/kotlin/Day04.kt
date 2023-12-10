import strikt.api.*
import strikt.assertions.*

fun main() {
    val test = listOf(
        "Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53",
        "Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19",
        "Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1",
        "Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83",
        "Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36",
        "Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11",
    )
    expectThat(part1(test)).isEqualTo(13)

    val input = readInput("Day04")
    println("part 1: ${part1(input)}")

    expectThat(part2(test)).isEqualTo(30)

    println("part 2: ${part2(input)}")
}

private fun part1(input: List<String>): Int =
    input
        .map(::Card)
        .sumOf(Card::worth)

private fun part2(input: List<String>): Int {
    val cards = input.map(::Card)
    for (index in cards.indices) {
        val card = cards[index]
        for (offset in 1..card.matchCount()) {
            cards[index + offset].addCopies(card.copies())
        }
    }
    return cards.sumOf(Card::copies)
}

private class Card(input: String) {
    private val winningNumbers: Set<Int>
    private val ownNumbers: Set<Int>
    private var copies = 1

    init {
        val sets = input
            .substringAfter(':')
            .split('|')
            .map { group ->
                group.trim()
                    .split("\\s+".toRegex())
                    .map(String::toInt)
                    .toSet()
            }
        winningNumbers = sets[0]
        ownNumbers = sets[1]
    }

    fun addCopies(count: Int) {
        copies += count
    }

    fun copies(): Int =
        copies

    fun worth(): Int =
        when (val count = matchCount()) {
            0 -> 0
            else -> 1 shl (count - 1)
        }

    fun matchCount(): Int =
        winningNumbers.intersect(ownNumbers).size
}
