import java.nio.file.*
import kotlin.io.path.*

class Day(
    dayOfMonth: Int,
    private vararg val parts: Part,
) {
    private val dayString = "Day${dayOfMonth / 10}${dayOfMonth % 10}"
    private val dayStringTest = dayString + "_test"

    fun run() {
        val input = resourcePath(dayString)?.readLines()
        parts.forEachIndexed { index, part ->
            val partNo = index + 1
            testInput(partNo)
                ?.let(part.compute)
                ?.takeIf { it != part.testResult }
                ?.let { "part1 returned $it, expected ${part.testResult}" }

            if (input != null) {
                val result = part.compute(input)
                println("part $partNo: $result")
            }
        }
    }

    private fun testInput(partNo: Int): List<String>? =
        resourcePath(dayStringTest + partNo)?.readLines()
            ?: resourcePath(dayStringTest)?.readLines()

    private fun resourcePath(name: String): Path? =
        Path("src/main/resources/$name.txt").takeIf { it.exists() }
}

class Part(val compute: (List<String>) -> Int, val testResult: Int)

fun day(dayOfMonth: Int, part1: (List<String>) -> Int, testResult1: Int) {
    Day(dayOfMonth, Part(part1, testResult1)).run()
}

fun day(dayOfMonth: Int, part1: (List<String>) -> Int, testResult1: Int, part2: (List<String>) -> Int, testResult2: Int) {
    Day(dayOfMonth, Part(part1, testResult1), Part(part2, testResult2)).run()
}
