fun main() {
    Day<Int>(4)
        .part1(13, ::part1)
        .part2(30, ::part2)
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
