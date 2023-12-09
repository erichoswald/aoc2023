import Direction.*
import strikt.api.*
import strikt.assertions.*

fun main() {
    test()

    Day<Long>(8)
        .part1(6) { lines ->
            val instructions = parseInstructions(lines.first())
            val map = parseMap(lines.drop(1))
            map.countSteps(instructions)
        }
        .part2(6) { lines ->
            val instructions = parseInstructions(lines.first())
            val map = parseMap(lines.drop(1))
            map.countSimultaneousSteps(instructions)
        }
}

private fun test() {
    expectThat(parseMapNode("AAA = (BBB, CCC)"))
        .isEqualTo("AAA" to MapNode("BBB", "CCC"))

    expectThat(listOf(LEFT, RIGHT).at(3))
        .isEqualTo(RIGHT)
}

private const val START = "AAA"
private const val END = "ZZZ"

private val MapNodeRegex = """(\w{3}) = \((\w{3}), (\w{3})\)""".toRegex()

private enum class Direction { LEFT, RIGHT }
private data class MapNode(val left: String, var right: String)

private fun Map<String, MapNode>.countSteps(instructions: List<Direction>): Long =
    follow(START, instructions) { it == END }

private fun Map<String, MapNode>.countSimultaneousSteps(instructions: List<Direction>): Long {
    val steps = findStartingLabels().map {
        follow(it, instructions, String::isFinishingLabel)
    }
    // NB: This only holds if the next step from the finishing labels leads back to the first node after the start node!
    return lcm(steps)
}

private fun Map<String, MapNode>.follow(
    start: String,
    instructions: List<Direction>,
    isAtEnd: (String) -> Boolean
): Long {
    var steps = 0L
    var label = start
    do {
        label = follow(label, instructions.at(steps++))
    } while (!isAtEnd(label))
    return steps
}

private fun Map<String, MapNode>.follow(label: String, direction: Direction): String =
    getValue(label).follow(direction)

private fun MapNode.follow(direction: Direction): String =
    when (direction) {
        LEFT -> left
        RIGHT -> right
    }

private fun lcm(numbers: List<Long>): Long =
    numbers.reduce(::lcm)

private fun lcm(a: Long, b: Long): Long =
    a * b / gcd(a, b)

private tailrec fun gcd(a: Long, b: Long): Long =
    if (b == 0L) a else gcd(b, a % b)

private fun Map<String, MapNode>.findStartingLabels(): List<String> =
    keys.filter(String::isStartingLabel)

private fun String.isStartingLabel(): Boolean =
    last() == 'A'

private fun String.isFinishingLabel(): Boolean =
    last() == 'Z'

private fun List<Direction>.at(steps: Long): Direction =
    get((steps % size.toLong()).toInt())

private fun parseInstructions(line: String): List<Direction> =
    line.map { ch ->
        when (ch) {
            'L' -> LEFT
            'R' -> RIGHT
            else -> error("Unknown direction '$ch'")
        }
    }

private fun parseMap(lines: List<String>): Map<String, MapNode> =
    lines
        .dropWhile(String::isBlank)
        .associate(::parseMapNode)

private fun parseMapNode(line: String): Pair<String, MapNode> {
    val match = MapNodeRegex.find(line) ?: error("Unexpected map node input: $line")
    val (_, label, left, right) = match.groupValues
    return label to MapNode(left, right)
}
