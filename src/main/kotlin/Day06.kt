import strikt.api.*
import strikt.assertions.*

private data class Record(val time: Long, val distance: Long)

fun main() {
    val testInput = listOf("Time:      7  15   30", "Distance:  9  40  200")
    val testRecords = listOf(Record(7, 9), Record(15, 40), Record(30, 200))

    expectThat(parseRecords(testInput)).containsExactly(testRecords)
    expectThat(Record(7, 9).winningCombinations()).isEqualTo((2L..5L).count().toLong())
    expectThat(Record(15, 40).winningCombinations()).isEqualTo((4L..11L).count().toLong())

    expectThat(part1(testInput)).isEqualTo(288)

    val input = readInput("Day06")
    println("part 1: ${part1(input)}")

    println("part 2: ${part2(input)}")
}

private fun part1(input: List<String>): Long =
    parseRecords(input)
        .map(Record::winningCombinations)
        .reduce(Long::times)

private fun part2(input: List<String>): Long =
    input
        .map { it.replace(" ", "") }
        .let(::part1)

private fun parseRecords(input: List<String>): List<Record> {
    val times = input[0].substringAfter("Time:").trim().split(' ').mapNotNull(String::toLongOrNull)
    val distances = input[1].substringAfter("Distance:").trim().split(' ').mapNotNull(String::toLongOrNull)
    return times.zip(distances, ::Record)
}

private fun Record.winningCombinations(): Long {
    var count = 0L
    for (n in 1..<time) {
        if ((time - n) * n > distance) count++
    }
    return count
}
