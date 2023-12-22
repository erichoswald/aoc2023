import strikt.api.*
import strikt.assertions.*

fun main() {
    val testInput = listOf(
        "1,0,1~1,2,1", // A
        "0,0,2~2,0,2", // B
        "0,2,3~2,2,3", // C
        "0,0,4~0,2,4", // D
        "2,0,5~2,2,5", // E
        "0,1,6~2,1,6", // F
        "1,1,8~1,1,9", // G
    )
    val testBlocks = testInput.parseBlocks()
    val testSettled = testBlocks.settled()
    expectThat(testSettled).containsExactly(
        SandBlock(1..1, 0..2, 1..1), // A
        SandBlock(0..2, 0..0, 2..2), // B
        SandBlock(0..2, 2..2, 2..2), // C
        SandBlock(0..0, 0..2, 3..3), // D
        SandBlock(2..2, 0..2, 3..3), // E
        SandBlock(0..2, 1..1, 4..4), // F
        SandBlock(1..1, 1..1, 5..6), // G
    )
    expectThat(testSettled.supportedBlocks(testSettled[0])).containsExactly(testSettled[1], testSettled[2])
    expectThat(testSettled.canDisintegrate(testSettled[0])).isFalse()
    expectThat(testSettled.canDisintegrate(testSettled[1])).isTrue()
    expectThat(testSettled.countCanDisintegrate()).isEqualTo(5)

    val input = readInput("Day22")
    println("Part 1: ${input.parseBlocks().settled().countCanDisintegrate()}")
}

private data class SandBlock(
    val x: IntRange,
    val y: IntRange,
    val z: IntRange,
)

private fun List<String>.parseBlocks(): List<SandBlock> =
    map { line ->
        val (p0, p1) = line.split('~')
        val (x0, y0, z0) = p0.split(',').map(String::toInt)
        val (x1, y1, z1) = p1.split(',').map(String::toInt)
        expect {
            that(x0).isLessThanOrEqualTo(x1)
            that(y0).isLessThanOrEqualTo(y1)
            that(z0).isLessThanOrEqualTo(z1)
        }
        SandBlock(x0..x1, y0..y1, z0..z1)
    }

private fun List<SandBlock>.settled(): List<SandBlock> {
    val support = List(maxOf { it.x.last } + 1) {
        MutableList(maxOf { it.y.last } + 1) {
            1 // Start right above ground level.
        }
    }
    val settled = mutableListOf<SandBlock>()
    sortedBy { it.z.first }.forEach { block ->
        val bottom = support.subList(block.x.first, block.x.last + 1).maxOf { beam ->
            beam.subList(block.y.first, block.y.last + 1).max()
        }
        expectThat(bottom <= block.z.first)
        val top = bottom + block.z.last - block.z.first
        settled += block.copy(z = bottom..top)
        for (x in block.x) {
            for (y in block.y) {
                support[x][y] = top + 1
            }
        }
    }
    return settled
}

private fun List<SandBlock>.countCanDisintegrate(): Int =
    count(::canDisintegrate)

private fun List<SandBlock>.canDisintegrate(block: SandBlock): Boolean =
    supportedBlocks(block).all { b -> supportingBlocks(b).size > 1 }

private fun List<SandBlock>.supportedBlocks(block: SandBlock): List<SandBlock> =
    filter { supported ->
        supported.z.first == block.z.last + 1 && supported.x.overlaps(block.x) && supported.y.overlaps(block.y)
    }

private fun List<SandBlock>.supportingBlocks(block: SandBlock): List<SandBlock> =
    filter { supporting ->
        supporting.z.last + 1 == block.z.first && supporting.x.overlaps(block.x) && supporting.y.overlaps(block.y)
    }

private fun IntRange.overlaps(other: IntRange): Boolean =
    first <= other.last && last >= other.first
