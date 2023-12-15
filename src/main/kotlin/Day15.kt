import strikt.api.*
import strikt.assertions.*

fun main() {
    expectThat("HASH".hash()).isEqualTo(52)
    expectThat("rn=1".hash()).isEqualTo(30)
    expectThat("cm-".hash()).isEqualTo(253)
    val testInstructions = parseInitializationSequence("rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7")
    expectThat(testInstructions.sumHashes()).isEqualTo(1320)

    val input = readInput("Day15")
    expectThat(input).hasSize(1)

    val instructions = parseInitializationSequence(input.first())
    println("Part 1: ${instructions.sumHashes()}")

    val testBoxes = LensBoxes()
    testBoxes.apply(testInstructions)
    expectThat(testBoxes.entries(0)).isEqualTo(listOf("rn" to 1, "cm" to 2))
    expectThat(testBoxes.entries(1)).isEmpty()
    expectThat(testBoxes.entries(3)).isEqualTo(listOf("ot" to 7, "ab" to 5, "pc" to 6))
    expectThat(testBoxes.totalFocusingPower()).isEqualTo(145)

    val boxes = LensBoxes()
    boxes.apply(instructions)
    println("Part 2: ${boxes.totalFocusingPower()}")
}

private class LensBoxes {
    private val boxes: List<MutableMap<String, Int>> = List(256) { mutableMapOf() }

    fun entries(box: Int): List<Pair<String, Int>> =
        boxes[box].toList()

    fun totalFocusingPower(): Int =
        boxes.flatMapIndexed { box, entries ->
            entries.toList().mapIndexed { slot, (_, focalLength) ->
                (box + 1) * (slot + 1) * focalLength
            }
        }.sum()

    fun apply(instruction: String) {
        if (instruction.endsWith('-')) {
            val label = instruction.dropLast(1)
            boxes[label.hash()].remove(label)
        } else {
            val (label, lens) = instruction.split('=')
            boxes[label.hash()] += label to lens.toInt()
        }
    }

    fun apply(instructions: Iterable<String>) {
        instructions.forEach(::apply)
    }
}

private fun Iterable<CharSequence>.sumHashes(): Int =
    sumOf(CharSequence::hash)

private fun CharSequence.hash(): Int =
    fold(0) { current, char ->
        ((current + char.code) * 17) % 256
    }

private fun parseInitializationSequence(input: String): List<String> =
    input.split(',')
