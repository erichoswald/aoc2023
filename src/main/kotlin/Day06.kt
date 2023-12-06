import strikt.api.*
import strikt.assertions.*

private data class Record(val time: Long, val distance: Long)

fun main() {
    tests()
    day(6, ::part1, 288, ::part2, 71503)
}

private fun part1(input: List<String>): Int =
    parseRecords(input)
        .map(Record::winningCombinations)
        .reduce(Long::times)
        .toInt()

private fun part2(input: List<String>): Int =
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

private fun tests() {
    expectThat(Record(7, 9).winningCombinations()).isEqualTo((2L..5L).count().toLong())
    expectThat(Record(15, 40).winningCombinations()).isEqualTo((4L..11L).count().toLong())
}
