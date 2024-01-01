import strikt.api.*
import strikt.assertions.*

fun main() {
    val testInput = listOf(
        "jqt: rhn xhk nvd",
        "rsh: frs pzl lsr",
        "xhk: hfx",
        "cmg: qnr nvd lhk bvb",
        "rhn: xhk bvb hfx",
        "bvb: xhk hfx",
        "pzl: lsr hfx nvd",
        "qnr: nvd",
        "ntq: jqt hfx bvb xhk",
        "nvd: lhk",
        "lsr: lhk",
        "rzs: qnr cmg lsr rsh",
        "frs: qnr lhk lsr",
    )
    val testGraph = testInput.parseGraph()
    expectThat(testGraph["jqt"]).isNotNull().containsExactlyInAnyOrder("rhn", "xhk", "nvd", "ntq")
    expectThat(testGraph["xhk"]).isNotNull().containsExactlyInAnyOrder("jqt", "hfx", "rhn", "bvb", "ntq")
    expectThat(testGraph.split()).containsExactlyInAnyOrder(
        setOf("cmg", "frs", "lhk", "lsr", "nvd", "pzl", "qnr", "rsh", "rzs"),
        setOf("bvb", "hfx", "jqt", "ntq", "rhn", "xhk"),
    )

    val input = readInput("Day25")
    val wires = input.parseGraph()
    println("Part 1: ${wires.split().toList().let { (group1, group2) -> group1.size * group2.size }}")
}

private typealias WireGraph = Map<String, Set<String>>

private fun List<String>.parseGraph(): WireGraph {
    val graph = mutableMapOf<String, MutableSet<String>>()
    forEach { line ->
        val (from, components) = line.split(": ")
        components.split(" ").forEach { to ->
            graph.getOrPut(from, ::mutableSetOf) += to
            graph.getOrPut(to, ::mutableSetOf) += from
        }
    }
    return graph
}

private fun WireGraph.split(): Set<Set<String>> =
    wires()
        .filter { (from, to) -> countUniquePaths(from, to) == 3 }
        .fold(toMutableMap()) { graph, (from, to) ->
            graph.apply { remove(from, to) }
        }.groups()

private fun WireGraph.wires(): Sequence<Pair<String, String>> =
    sequence {
        for (from in keys) {
            for (to in keys) {
                if (to in connections(from)) {
                    yield(from to to)
                }
            }
        }
    }

private tailrec fun WireGraph.countUniquePaths(from: String, to: String, count: Int = 0): Int {
    val path = findShortestPath(from, to)
    if (path.isEmpty()) {
        return count // No further path found.
    }
    val availableEdges = toMutableMap()
    path.zipWithNext { a, b ->
        availableEdges.remove(a, b)
    }
    return availableEdges.countUniquePaths(from, to, count + 1)
}

private fun MutableMap<String, Set<String>>.remove(from: String, to: String) {
    compute(from) { _, values -> values?.minus(to) }
    compute(to) { _, values -> values?.minus(from) }
}

private fun WireGraph.findShortestPath(from: String, to: String): List<String> {
    val visited = mutableSetOf<String>()
    val queue = mutableListOf(listOf(from))
    while (queue.isNotEmpty()) {
        val path = queue.removeFirst()
        val last = path.last()
        if (last == to) {
            return path
        } else {
            visited += last
            val unvisited = connections(last) - visited
            for (next in unvisited) {
                queue += path + next
            }
        }
    }
    return emptyList()
}

private fun WireGraph.connections(node: String): Set<String> =
    getOrElse(node, ::emptySet)

private fun WireGraph.groups(): Set<Set<String>> {
    val groups = mutableMapOf<String, MutableSet<String>>()
    forEach { (from, wires) ->
        val fromGroup = groups[from]
        wires.forEach { to ->
            val toGroup = groups[to]
            when {
                fromGroup == null && toGroup == null -> {
                    val newGroup = mutableSetOf(from, to)
                    groups[from] = newGroup
                    groups[to] = newGroup
                }

                fromGroup != null && toGroup == null -> {
                    fromGroup += to
                    groups[to] = fromGroup
                }

                fromGroup == null && toGroup != null -> {
                    toGroup += from
                    groups[from] = toGroup
                }

                fromGroup === toGroup -> Unit
                fromGroup != null && toGroup != null -> {
                    fromGroup += toGroup
                    toGroup.forEach {
                        groups[it] = fromGroup
                    }
                }
            }
        }
    }
    return groups.values.toSet()
}
