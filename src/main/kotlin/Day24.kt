import strikt.api.*
import strikt.assertions.*
import java.math.*
import java.math.BigDecimal.*

fun main() {
    val testInput = listOf(
        "19, 13, 30 @ -2,  1, -2",
        "18, 19, 22 @ -1, -1, -2",
        "20, 25, 34 @ -2, -2, -4",
        "12, 31, 28 @ -1, -2, -1",
        "20, 19, 15 @  1, -5, -3",
    )
    val testTrajectories = listOf(
        Trajectory(19, 13, 30, -2, 1, -2),
        Trajectory(18, 19, 22, -1, -1, -2),
        Trajectory(20, 25, 34, -2, -2, -4),
        Trajectory(12, 31, 28, -1, -2, -1),
        Trajectory(20, 19, 15, 1, -5, -3),
    )
    expectThat(testInput.parseTrajectories()).isEqualTo(testTrajectories)

    expectThat(testTrajectories.boundaryCrossingCount(BigDecimal(7), BigDecimal(27))).isEqualTo(2)

    val trajectories = readInput("Day24").parseTrajectories()
    println("Part 1: ${trajectories.boundaryCrossingCount(BigDecimal(200000000000000L), BigDecimal(400000000000000L))}")
}

private data class Trajectory(
    val px: BigDecimal,
    val py: BigDecimal,
    val pz: BigDecimal,
    val vx: BigDecimal,
    val vy: BigDecimal,
    val vz: BigDecimal
) {
    constructor(px: Long, py: Long, pz: Long, vx: Long, vy: Long, vz: Long) : this(
        px.toBigDecimal(),
        py.toBigDecimal(),
        pz.toBigDecimal(),
        vx.toBigDecimal(),
        vy.toBigDecimal(),
        vz.toBigDecimal(),
    )
}

private data class TrajectoryPoint(val x: BigDecimal, val y: BigDecimal)

private fun List<String>.parseTrajectories(): List<Trajectory> =
    map { line ->
        val (p, v) = line.split('@')
        val (px, py, pz) = p.split(',').map { it.trim().toLong() }
        val (vx, vy, vz) = v.split(',').map { it.trim().toLong() }
        expect {
            that(vx).isNotEqualTo(0)
            that(vy).isNotEqualTo(0)
        }
        Trajectory(px, py, pz, vx, vy, vz)
    }

private fun List<Trajectory>.boundaryCrossingCount(b0: BigDecimal, b1: BigDecimal): Int {
    var count = 0
    for (i in indices) {
        for (j in (i + 1)..lastIndex) {
            val a = this[i]
            val b = this[j]
            val intersection = a.intersection(b)
            if (intersection != null) {
                if (intersection.isInRange(b0, b1) && a.isInFuture(intersection) && b.isInFuture(intersection)) {
                    count++
                }
            }
        }
    }
    return count
}

private fun Trajectory.intersection(other: Trajectory): TrajectoryPoint? {
    val d = vx * other.vy - vy * other.vx
    if (d == ZERO) return null // Trajectories are parallel or coincident.
    val pqx = px * (py + vy) - py * (px + vx)
    val opqx = other.px * (other.py + other.vy) - other.py * (other.px + other.vx)
    val nx = opqx * vx - pqx * other.vx
    val ny = opqx * vy - pqx * other.vy
    return TrajectoryPoint(nx / d, ny / d)
}

private fun Trajectory.isInFuture(p: TrajectoryPoint): Boolean =
    when {
        vx > ZERO && p.x < px -> false
        vx < ZERO && p.x > px -> false
        vy > ZERO && p.y < py -> false
        vy < ZERO && p.y > py -> false
        else -> true
    }

private fun TrajectoryPoint.isInRange(b0: BigDecimal, b1: BigDecimal): Boolean =
    x in b0..b1 && b0 <= y && y <= b1

private operator fun LongRange.times(scale: Long): LongRange =
    first * scale..last * scale
