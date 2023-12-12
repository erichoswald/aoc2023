import strikt.api.*
import strikt.assertions.*

fun main() {
    expectThat(parseInput(listOf("???.### 1,1,3"))).containsExactly(Pair("???.###", listOf(1, 1, 3)))

    expectThat(countPossibleArrangements("#.#.###", listOf(1, 1, 3))).isEqualTo(1)
    expectThat(countPossibleArrangements("???.###", listOf(1, 1, 3))).isEqualTo(1)
    expectThat(countPossibleArrangements(".??..??...?##.", listOf(1, 1, 3))).isEqualTo(4)
    expectThat(countPossibleArrangements("?#?#?#?#?#?#?#?", listOf(1, 3, 1, 6))).isEqualTo(1)
    expectThat(countPossibleArrangements("????.#...#...", listOf(4, 1, 1))).isEqualTo(1)
    expectThat(countPossibleArrangements("????.######..#####.", listOf(1, 6, 5))).isEqualTo(4)
    expectThat(countPossibleArrangements("?###????????", listOf(3, 2, 1))).isEqualTo(10)

    val test = parseInput(readInput("Day12_test"))
    expectThat(test.countPossibleArrangements()).isEqualTo(21)

    val input = parseInput(readInput("Day12"))
    println("Part 1: ${input.countPossibleArrangements()}")

    expectThat(unfold(".#", listOf(1))).isEqualTo(".#?.#?.#?.#?.#" to List(5) { 1 })
    expectThat(listOf(unfold(".??..??...?##.", listOf(1, 1, 3))).countPossibleArrangements()).isEqualTo(16384)

    expectThat(test.unfold().countPossibleArrangements()).isEqualTo(525152)

    println("Part 2: ${input.unfold().countPossibleArrangements()}")
}

private data class CounterKey(val condition: String, val damages: List<Int>)

private fun parseInput(input: List<String>): List<Pair<String, List<Int>>> =
    input.map { line ->
        val (conditions, arrangements) = line.split(' ')
        Pair(conditions, arrangements.split(',').map(String::toInt))
    }

private fun List<Pair<String, List<Int>>>.unfold(): List<Pair<String, List<Int>>> =
    map { (condition, damages) -> unfold(condition, damages) }

private fun unfold(condition: String, arrangements: List<Int>): Pair<String, List<Int>> =
    Pair(
        List(5) { condition }.joinToString("?"),
        List(5) { arrangements }.flatten(),
    )

private fun List<Pair<String, List<Int>>>.countPossibleArrangements(): Long {
    val memoized = mutableMapOf<CounterKey, Long>()
    return sumOf { (condition, arrangements) -> memoized.countPossibleArrangements(condition, arrangements) }
}

private fun countPossibleArrangements(condition: String, arrangements: List<Int>): Long =
    mutableMapOf<CounterKey, Long>().countPossibleArrangements(condition, arrangements)

private fun MutableMap<CounterKey, Long>.countPossibleArrangements(condition: String, damages: List<Int>): Long {
    val key = CounterKey(condition, damages)
    if (!containsKey(key)) {
        val value = when {
            condition == "." -> damages.isEmpty().countIfTrue()
            condition == "#" -> (damages.singleOrNull() == 1).countIfTrue()
            condition.startsWith('.') -> countPossibleArrangements(condition.drop(1), damages)
            condition.startsWith('?') -> {
                val tail = condition.drop(1)
                countPossibleArrangements(".$tail", damages) + countPossibleArrangements("#$tail", damages)
            }
            condition.startsWith("#") && damages.isEmpty() -> 0
            condition.startsWith("#.") && damages.first() == 1 ->
                countPossibleArrangements(condition.drop(1), damages.drop(1))
            condition.startsWith("##") && damages.first() == 1 -> 0
            condition.startsWith("##") ->
                countPossibleArrangements(condition.drop(1), listOf(damages.first() - 1) + damages.drop(1))
            condition.startsWith("#?") -> {
                val tail = condition.drop(2)
                countPossibleArrangements("#.$tail", damages) + countPossibleArrangements("##$tail", damages)
            }
            else -> 0
        }
        this[key] = value
    }
    return getValue(key)
}

private fun Boolean.countIfTrue(): Long =
    if (this) 1 else 0
