import Pulse.*
import strikt.api.*
import strikt.assertions.*

fun main() {
    val test1 = listOf(
        "broadcaster -> a, b, c",
        "%a -> b",
        "%b -> c",
        "%c -> inv",
        "&inv -> a",
    )
    expectThat(test1.parseModules().values).containsExactly(
        Broadcaster(listOf("a", "b", "c")),
        FlipFlop("a", listOf("broadcaster", "inv"), listOf("b")),
        FlipFlop("b", listOf("broadcaster", "a"), listOf("c")),
        FlipFlop("c", listOf("broadcaster", "b"), listOf("inv")),
        Conjunction("inv", listOf("c"), listOf("a")),
    )

    expectThat(test1.parseModules().countEmittedPulses()).isEqualTo(32000000)

    val test2 = listOf(
        "broadcaster -> a",
        "%a -> inv, con",
        "&inv -> b",
        "%b -> con",
        "&con -> output",
    )
    expectThat(test2.parseModules().values).containsExactly(
        Broadcaster(listOf("a")),
        FlipFlop("a", listOf("broadcaster"), listOf("inv", "con")),
        Conjunction("inv", listOf("a"), listOf("b")),
        FlipFlop("b", listOf("inv"), listOf("con")),
        Conjunction("con", listOf("a", "b"), listOf("output")),
        Sink("output", listOf("con")),
    )
    expectThat(test2.parseModules().countEmittedPulses()).isEqualTo(11687500)

    val input = readInput("Day20")
    println("Part 1: ${input.parseModules().countEmittedPulses()}")

    println("Part 2: ${input.parseModules().countToRxLow()}")
}

private const val BROADCASTER = "broadcaster"
private const val BUTTON = "button"

private enum class Pulse {
    LOW, HIGH
}

private data class Emitted(
    val pulse: Pulse,
    val from: String,
    val to: String,
)

private sealed interface Module {
    val name: String
    val sources: List<String>
    val destinations: List<String>

    fun process(pulse: Pulse, from: String): Pulse?
}

private data class Broadcaster(
    override val destinations: List<String>,
) : Module {
    override val sources: List<String> = emptyList()
    override val name: String = BROADCASTER

    override fun process(pulse: Pulse, from: String): Pulse =
        pulse
}

private data class FlipFlop(
    override val name: String,
    override val sources: List<String>,
    override val destinations: List<String>,
) : Module {
    private var isOn = false

    override fun process(pulse: Pulse, from: String): Pulse? =
        when {
            pulse == HIGH -> null
            !isOn -> {
                isOn = true
                HIGH
            }
            isOn -> {
                isOn = false
                LOW
            }
            else -> error("Impossible")
        }
}

private data class Conjunction(
    override val name: String,
    override val sources: List<String>,
    override val destinations: List<String>,
) : Module {
    private val memory = sources.associateWith { LOW }.toMutableMap()

    override fun process(pulse: Pulse, from: String): Pulse {
        memory[from] = pulse
        return if (memory.values.all { it == HIGH }) {
            LOW
        } else {
            HIGH
        }
    }
}

private data class Sink(
    override val name: String,
    override val sources: List<String>,
) : Module {
    override val destinations: List<String> = emptyList()

    override fun process(pulse: Pulse, from: String): Pulse? =
        null
}

private fun List<String>.parseModules(): Map<String, Module> {
    val destinations = map { line ->
        val (name, dest) = line.split(" -> ")
        name to dest.split(", ")
    }
    val sources = destinations.fold(emptyList<Pair<String, String>>()) { pairs, (name, destinations) ->
        val unprefixed = name.split('&', '%').single(String::isNotEmpty)
        pairs + destinations.map { it to unprefixed }
    }.groupBy { it.first }.mapValues { (_, pairs) -> pairs.map { it.second } }
    val modules = destinations.map { (name, dest) ->
        when {
            name == BROADCASTER -> Broadcaster(dest)
            name.startsWith('%') -> FlipFlop(name.drop(1), sources.getOrElse(name.drop(1), ::emptyList), dest)
            name.startsWith('&') -> Conjunction(name.drop(1), sources.getOrElse(name.drop(1), ::emptyList), dest)
            else -> error("Cannot parse name '$name'")
        }
    }.associateBy(Module::name)
    val sinks = (sources - modules.keys).map { (name, sources) -> name to Sink(name, sources) }.toMap()
    return modules + sinks
}

private fun Map<String, Module>.countEmittedPulses(cycles: Int = 1000): Long {
    var lowPulses = 0L
    var highPulses = 0L
    repeat(cycles) {
        val queue = mutableListOf(Emitted(LOW, BUTTON, BROADCASTER))
        do {
            val (pulse, from, to) = queue.removeFirst()
            when (pulse) {
                LOW -> lowPulses++
                HIGH -> highPulses++
            }
            get(to)?.process(pulse, from)?.let { emittedPulse ->
                queue += getValue(to).destinations.map { name -> Emitted(emittedPulse, to, name) }
            }
        } while (queue.isNotEmpty())
    }
    return lowPulses * highPulses
}

private fun Map<String, Module>.countToRxLow(): Long {
    // Assume that there is a single conjunction that feeds "rx" whose inputs are also conjunctions whose state repeats in cycles.
    val rxFeeder = getValue("rx").sources
    expectThat(rxFeeder).hasSize(1)
    val rxFeederModule = getValue(rxFeeder.single())
    expectThat(rxFeederModule).isA<Conjunction>()
    val sources = rxFeederModule.sources
    expectThat(sources.map(::getValue)).all { isA<Conjunction>() }
    val sourceHighCount = mutableMapOf<String, Long>()

    var pressCount = 0L
    while (sourceHighCount.size != sources.size) {
        pressCount++
        val queue = mutableListOf(Emitted(LOW, BUTTON, BROADCASTER))
        do {
            val (pulse, from, to) = queue.removeFirst()
            get(to)?.process(pulse, from)?.let { emittedPulse ->
                queue += getValue(to).destinations.map { name ->
                    if (name == rxFeederModule.name) {
                        if (emittedPulse == HIGH && to !in sourceHighCount) {
                            sourceHighCount[to] = pressCount
                        }
                    }
                    Emitted(emittedPulse, to, name)
                }
            }
        } while (queue.isNotEmpty())
    }
    return lcm(sourceHighCount.values.toList())
}

private fun lcm(numbers: List<Long>): Long =
    numbers.reduce(::lcm)

private fun lcm(a: Long, b: Long): Long =
    a * b / gcd(a, b)

private tailrec fun gcd(a: Long, b: Long): Long =
    if (b == 0L) a else gcd(b, a % b)
