import strikt.api.*
import strikt.assertions.*

fun main() {
    expectThat(extendSequenceForward(0, 3, 6, 9, 12, 15)).isEqualTo(18)
    expectThat(extendSequenceForward(1, 3, 6, 10, 15, 21)).isEqualTo(28)
    expectThat(extendSequenceForward(10, 13, 16, 21, 30, 45)).isEqualTo(68)

    val sequences = readInput("Day09").map(::parseSequence)
    val part1 = sequences.sumOf(::extendSequenceForward)
    println("part1: $part1")

    expectThat(extendSequenceBackward(0, 3, 6, 9, 12, 15)).isEqualTo(-3)
    expectThat(extendSequenceBackward(1, 3, 6, 10, 15, 21)).isEqualTo(0)
    expectThat(extendSequenceBackward(10, 13, 16, 21, 30, 45)).isEqualTo(5)

    val part2 = sequences.sumOf(::extendSequenceBackward)
    println("part2: $part2")
}

private fun extendSequenceForward(vararg numbers: Int): Int =
    extendSequenceForward(numbers.toList())

private fun extendSequenceForward(numbers: List<Int>): Int =
    extendSequence(numbers) { diffs, delta -> diffs.last() + delta }

private fun extendSequenceBackward(vararg numbers: Int): Int =
    extendSequenceBackward(numbers.toList())

private fun extendSequenceBackward(numbers: List<Int>): Int =
    extendSequence(numbers) { diffs, delta -> diffs.first() - delta }

private fun extendSequence(numbers: List<Int>, extend: (List<Int>, Int) -> Int): Int =
    if (numbers.all { it == 0 }) {
        0
    } else {
        val diffs = numbers.zipWithNext { a, b -> b - a }
        val delta = extendSequence(diffs, extend)
        extend(numbers, delta)
    }

private fun parseSequence(input: String): List<Int> =
    input.split(' ').map(String::toInt)
