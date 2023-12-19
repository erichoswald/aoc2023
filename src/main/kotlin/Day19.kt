import Condition.*
import Rating.*
import strikt.api.*
import strikt.assertions.*

fun main() {
    val testWorkflowInput = listOf(
        "px{a<2006:qkq,m>2090:A,rfg}",
        "pv{a>1716:R,A}",
        "lnx{m>1548:A,A}",
        "rfg{s<537:gd,x>2440:R,A}",
        "qs{s>3448:A,lnx}",
        "qkq{x<1416:A,crn}",
        "crn{x>2662:A,R}",
        "in{s<1351:px,qqz}",
        "qqz{s>2770:qs,m<1801:hdj,R}",
        "gd{a>3333:R,R}",
        "hdj{m>838:A,pv}",
    )
    val testPartInput = listOf(
        "{x=787,m=2655,a=1222,s=2876}",
        "{x=1679,m=44,a=2067,s=496}",
        "{x=2036,m=264,a=79,s=2244}",
        "{x=2461,m=1339,a=466,s=291}",
        "{x=2127,m=1623,a=2188,s=1013}",
    )

    expectThat(testWorkflowInput[0].parseWorkflow()).isEqualTo(
        Workflow(
            "px",
            listOf(
                ConditionStep(A, LT, 2006, "qkq"),
                ConditionStep(M, GT, 2090, "A"),
                UnconditionalStep("rfg"),
            ),
        ),
    )
    expectThat(testWorkflowInput[1].parseWorkflow()).isEqualTo(
        Workflow(
            "pv",
            listOf(
                ConditionStep(A, GT, 1716, "R"),
                UnconditionalStep("A"),
            ),
        ),
    )

    expectThat(testPartInput[0].parsePart()).isEqualTo(Part(787, 2655, 1222, 2876))

    val testWorkflows = testWorkflowInput.parseWorkflows()
    val testParts = testPartInput.map(String::parsePart)
    expectThat(testWorkflows.isAccepted(testParts[0])).isTrue()
    expectThat(testWorkflows.isAccepted(testParts[1])).isFalse()

    expectThat(testParts.sumAccepted(testWorkflows)).isEqualTo(19114)

    val input = readInput("Day19")
    val separator = input.indexOf("")
    val workflows = input.subList(0, separator).parseWorkflows()
    val parts = input.subList(separator + 1, input.size).map(String::parsePart)
    println("Part 1: ${parts.sumAccepted(workflows)}")

    expectThat(intersect(emptyList(), emptyList())).isEqualTo(emptyList())
    expectThat(intersect(emptyList(), listOf(2..6))).isEqualTo(emptyList())
    expectThat(intersect(listOf(1..1), listOf(2..6))).isEqualTo(emptyList())
    expectThat(intersect(listOf(2..6), listOf(4..9))).isEqualTo(listOf(4..6))
    expectThat(intersect(listOf(2..5, 7..8), listOf(4..9))).isEqualTo(listOf(4..5, 7..8))

    expectThat(testWorkflows.combinations("pv", FullRanges)).isEqualTo(1716L * MAX_RATING * MAX_RATING * MAX_RATING)
    expectThat(testWorkflows.combinations("crn", FullRanges)).isEqualTo(1338L * MAX_RATING * MAX_RATING * MAX_RATING)
    expectThat(testWorkflows.combinations("qkq", FullRanges)).isEqualTo((1415L + 1338L) * MAX_RATING * MAX_RATING * MAX_RATING)
    expectThat(testWorkflows.combinations("gd", FullRanges)).isEqualTo(0L)
    expectThat(testWorkflows.combinations("rfg", FullRanges)).isEqualTo(3464L * 2440L * MAX_RATING * MAX_RATING)

    expectThat(testWorkflows.combinations("in", FullRanges)).isEqualTo(167409079868000)

    println("Part 2: ${workflows.combinations("in", FullRanges)}")
}

private const val MAX_RATING = 4000
private val FullRange = 1..MAX_RATING
private val FullRanges = Ranges(listOf(FullRange), listOf(FullRange), listOf(FullRange), listOf(FullRange))

private enum class Rating {
    X, M, A, S
}

private enum class Condition {
    LT, GT
}

private data class Workflow(val name: String, val steps: List<Step>)

private sealed interface Step {
    val then: String
}

private data class ConditionStep(
    val rating: Rating,
    val condition: Condition,
    val value: Int,
    override val then: String
) : Step

private data class UnconditionalStep(override val then: String) : Step

private data class Part(val x: Int, val m: Int, val a: Int, val s: Int)

private data class Ranges(val x: List<IntRange>, val m: List<IntRange>, val a: List<IntRange>, val s: List<IntRange>)

private fun List<Part>.sumAccepted(workflows: Map<String, Workflow>): Int =
    filter(workflows::isAccepted).sumOf(Part::sum)

private fun Map<String, Workflow>.isAccepted(part: Part): Boolean {
    var name = "in"
    while (name != "A" && name != "R") {
        name = getValue(name).steps.then(part)
    }
    return name == "A"
}

private fun Map<String, Workflow>.combinations(name: String, ranges: Ranges): Long =
    when (name) {
        "A" -> ranges.combinations()
        "R" -> 0
        else -> combinations(getValue(name).steps, ranges)
    }

private fun Map<String, Workflow>.combinations(steps: List<Step>, ranges: Ranges): Long =
    when (val step = steps.first()) {
        is ConditionStep -> combinations(step.then, step.includedRanges(ranges)) + combinations(steps.drop(1), step.excludedRanges(ranges))
        is UnconditionalStep -> combinations(step.then, ranges)
    }

private fun List<Step>.then(part: Part): String =
    dropWhile { step -> step is ConditionStep && !step.matches(part) }.first().then

private fun ConditionStep.matches(part: Part): Boolean =
    when (condition) {
        LT -> part[rating] < value
        GT -> part[rating] > value
    }

private fun ConditionStep.includedRanges(ranges: Ranges): Ranges =
    ranges.reduce(
        rating,
        when (condition) {
            LT -> 1..<value
            GT -> (value + 1)..MAX_RATING
        },
    )

private fun ConditionStep.excludedRanges(ranges: Ranges): Ranges =
    ranges.reduce(
        rating,
        when (condition) {
            LT -> value..MAX_RATING
            GT -> 1..value
        }
    )

private operator fun Part.get(rating: Rating): Int =
    when (rating) {
        X -> x
        M -> m
        A -> a
        S -> s
    }

private fun Part.sum(): Int =
    x + m + a + s

private fun Ranges.combinations(): Long {
    val x = x.sumOf(IntRange::combinations)
    val m = m.sumOf(IntRange::combinations)
    val a = a.sumOf(IntRange::combinations)
    val s = s.sumOf(IntRange::combinations)
    return x * m * a * s
}

private fun IntRange.combinations(): Long =
    (last - first + 1).toLong()

private fun Ranges.reduce(rating: Rating, range: IntRange): Ranges =
    when (rating) {
        X -> copy(x = intersect(x, listOf(range)))
        M -> copy(m = intersect(m, listOf(range)))
        A -> copy(a = intersect(a, listOf(range)))
        S -> copy(s = intersect(s, listOf(range)))
    }

private fun intersect(a: List<IntRange>, b: List<IntRange>): List<IntRange> {
    val result = mutableListOf<IntRange>()
    val am = a.sortedBy(IntRange::first).toMutableList()
    val bm = b.sortedBy(IntRange::first).toMutableList()
    while (am.isNotEmpty() && bm.isNotEmpty()) {
        if (am.first().last < bm.first().first) {
            am.removeFirst()
        } else if (bm.first().last < am.first().first) {
            bm.removeFirst()
        } else if (am.first().first < bm.first().first) {
            am[0] = bm.first().first..am.first().last
        } else if (bm.first().first < am.first().first) {
            bm[0] = am.first().first..bm.first().last
        } else if (am.first().last < bm.first().last) {
            val aRange = am.removeFirst()
            result += aRange
            bm[0] = aRange.last..bm.first().last
        } else if (bm.first().last < am.first().last) {
            val bRange = bm.removeFirst()
            result += bRange
            am[0] = bRange.last..am.first().last
        } else {
            result += am.removeFirst()
            bm.removeFirst()
        }
    }
    return result
}

private fun List<String>.parseWorkflows(): Map<String, Workflow> =
    map(String::parseWorkflow).associateBy(Workflow::name)

private fun String.parseWorkflow(): Workflow {
    val name = takeWhile(Char::isLetter)
    val steps = drop(name.length)
        .removeSurrounding("{", "}")
        .parseSteps()
    return Workflow(name, steps)
}

private fun String.parseSteps(): List<Step> =
    split(',').map(String::parseStep)

private fun String.parseStep(): Step {
    val parts = split(':')
    return if (parts.size == 1) {
        UnconditionalStep(parts[0])
    } else {
        val rating = Rating.valueOf(parts[0].first().uppercase())
        val condition = when (parts[0][1]) {
            '<' -> LT
            '>' -> GT
            else -> error("Unknown condition in '$this'")
        }
        val value = parts[0].drop(2).toInt()
        ConditionStep(rating, condition, value, parts[1])
    }
}

private val PartRegex = """\{x=(\d+),m=(\d+),a=(\d+),s=(\d+)}""".toRegex()

private fun String.parsePart(): Part {
    val values = PartRegex.matchEntire(this)?.groupValues?.drop(1)?.map(String::toInt)
        ?: error("Not a part: '$this'")
    return Part(values[0], values[1], values[2], values[3])
}
