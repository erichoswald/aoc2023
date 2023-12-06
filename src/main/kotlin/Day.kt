import java.nio.file.*
import kotlin.io.path.*

class Day<R>(dayOfMonth: Int) {
    private val dayString = "Day${dayOfMonth / 10}${dayOfMonth % 10}"
    private val dayStringTest = dayString + "_test"
    private val input = resourcePath(dayString)?.readLines()

    fun part1(testResult: R, compute: (List<String>) -> R): Day<R> {
        test(1, testResult, compute)
        run(1, compute)
        return this
    }

    fun part2(testResult: R, compute: (List<String>) -> R): Day<R> {
        test(2, testResult, compute)
        run(2, compute)
        return this
    }

    private fun test(partNo: Int, testResult: R, compute: (List<String>) -> R) {
        testInput(partNo)
            ?.let(compute)
            ?.takeIf { it != testResult }
            ?.let { println("part $partNo test returned $it, expected ${testResult}") }
    }

    private fun run(partNo: Int, compute: (List<String>) -> R) {
        if (input != null) {
            val result = compute(input)
            println("part $partNo: $result")
        }
    }

    private fun testInput(partNo: Int): List<String>? =
        resourcePath(dayStringTest + partNo)?.readLines()
            ?: resourcePath(dayStringTest)?.readLines()

    private fun resourcePath(name: String): Path? =
        Path("src/main/resources/$name.txt").takeIf { it.exists() }
}
