import strikt.api.*
import strikt.assertions.*
import java.math.*
import java.math.BigInteger.*

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

    val magicRock = trajectories.findMagicRock()
    println("Part 2: ${magicRock.px + magicRock.py + magicRock.pz}")
}

private data class Trajectory(
    val px: BigInteger,
    val py: BigInteger,
    val pz: BigInteger,
    val vx: BigInteger,
    val vy: BigInteger,
    val vz: BigInteger
) {
    constructor(px: Long, py: Long, pz: Long, vx: Long, vy: Long, vz: Long) : this(
        px.toBigInteger(),
        py.toBigInteger(),
        pz.toBigInteger(),
        vx.toBigInteger(),
        vy.toBigInteger(),
        vz.toBigInteger(),
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

private fun List<Trajectory>.findMagicRock(): Trajectory {
    // We need 6 equations to solve for the components of the rock trajectory, which we get by combining two dimensions of two known trajectories six times.
    // tr.px + t * tr.vx = rock.px + t * rock.vx
    // t = (tr.px - rock.px) / (rock.vx - tr.vx) = (tr.py - rock.py) / (rock.vy - tr.vy) = (tr.pz - rock.pz) / (rock.pz - tr.vz)
    // (tr.px - rock.px) * (rock.vy - tr.vy) = (tr.py - rock.py) * (rock.vx - tr.vx)
    // tr.px * rock.vy - tr.px * tr.vy - rock.px * rock.vy + rock.px * tr.vy = tr.py * rock.vx - tr.py * tr.vx - rock.py * rock.vx + rock.py * tr.vx
    // tr.px * rock.vy - tr.px * tr.vy + rock.px * tr.vy - tr.py * rock.vx + tr.py * tr.vx - rock.py * tr.vx = rock.px * rock.vy - rock.py * rock.vx
    // t0.px * r.vy - t0.px * t0.vy + r.px * t0.vy - t0.py * r.vx + t0.py * t0.vx - r.py * t0.vx = t1.px * r.vy - t1.px * t1.vy + r.px * t1.vy - t1.py * r.vx + t1.py * t1.vx - r.py * t1.vx
    // a * r.px + b * r.vx + c * r.py + d * r.vy + e * r.pz + f * r.vz = g
    // a = t0.vy - t1.vy, b = t1.py - t0.py, c = t1.vx - t0.vx, d = t0.px - t1.px, g = t0.px * t0.vy - t0.py * t0.vx - t1.px * t1.vy + t1.py * t1.vx
    val matrix = Array(6) { Array(7) { ZERO } }
    val tr0 = this[0]
    val tr1 = this[1]
    val tr2 = this[2]

    // x/y of tr0/tr1
    matrix[0][0] = tr0.vy - tr1.vy
    matrix[0][1] = tr1.py - tr0.py
    matrix[0][2] = tr1.vx - tr0.vx
    matrix[0][3] = tr0.px - tr1.px
    matrix[0][6] = tr0.px * tr0.vy - tr0.py * tr0.vx - tr1.px * tr1.vy + tr1.py * tr1.vx

    // x/z of tr0/tr1
    matrix[1][0] = tr0.vz - tr1.vz
    matrix[1][1] = tr1.pz - tr0.pz
    matrix[1][4] = tr1.vx - tr0.vx
    matrix[1][5] = tr0.px - tr1.px
    matrix[1][6] = tr0.px * tr0.vz - tr0.pz * tr0.vx - tr1.px * tr1.vz + tr1.pz * tr1.vx

    // y/z of tr0/tr1
    matrix[2][2] = tr0.vz - tr1.vz
    matrix[2][3] = tr1.pz - tr0.pz
    matrix[2][4] = tr1.vy - tr0.vy
    matrix[2][5] = tr0.py - tr1.py
    matrix[2][6] = tr0.py * tr0.vz - tr0.pz * tr0.vy - tr1.py * tr1.vz + tr1.pz * tr1.vy

    // x/y of tr0/tr2
    matrix[3][0] = tr0.vy - tr2.vy
    matrix[3][1] = tr2.py - tr0.py
    matrix[3][2] = tr2.vx - tr0.vx
    matrix[3][3] = tr0.px - tr2.px
    matrix[3][6] = tr0.px * tr0.vy - tr0.py * tr0.vx - tr2.px * tr2.vy + tr2.py * tr2.vx

    // x/z of tr0/tr2
    matrix[4][0] = tr0.vz - tr2.vz
    matrix[4][1] = tr2.pz - tr0.pz
    matrix[4][4] = tr2.vx - tr0.vx
    matrix[4][5] = tr0.px - tr2.px
    matrix[4][6] = tr0.px * tr0.vz - tr0.pz * tr0.vx - tr2.px * tr2.vz + tr2.pz * tr2.vx

    // y/z of tr0/tr2
    matrix[5][2] = tr0.vz - tr2.vz
    matrix[5][3] = tr2.pz - tr0.pz
    matrix[5][4] = tr2.vy - tr0.vy
    matrix[5][5] = tr0.py - tr2.py
    matrix[5][6] = tr0.py * tr0.vz - tr0.pz * tr0.vy - tr2.py * tr2.vz + tr2.pz * tr2.vy

    matrix.reduceToRowEchelonForm()
    matrix.reduce()

    return Trajectory(
        px = matrix[0].last(),
        py = matrix[2].last(),
        pz = matrix[4].last(),
        vx = matrix[1].last(),
        vy = matrix[3].last(),
        vz = matrix[5].last(),
    )
}

private fun Array<Array<BigInteger>>.reduceToRowEchelonForm() {
    val rows = size
    val columns = first().size
    var row = 0
    for (column in 0..<columns - 1) {
        if (this[row][column] != ZERO) {
            for (r in (row + 1)..<rows) {
                val pivot = this[r][column]
                this[r][column] = ZERO
                for (c in (column + 1)..<columns) {
                    this[r][c] = this[r][c] * this[row][column] - this[row][c] * pivot
                }
            }
            row++
        }
    }
}

private fun Array<Array<BigInteger>>.reduce() {
    val resultColumn = first().lastIndex
    var column = resultColumn - 1
    while (column >= 0) {
        val v = this[column][column]
        expectThat(this[column][resultColumn] % v).isEqualTo(ZERO)
        this[column][resultColumn] /= v
        this[column][column] = ONE
        for (row in 0..<column) {
            this[row][resultColumn] -= this[row][column] * this[column][resultColumn]
            this[row][column] = ZERO
        }
        column--
    }
}

private fun Trajectory.intersection(other: Trajectory): TrajectoryPoint? {
    // https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection
    val d = vx * other.vy - vy * other.vx
    if (d == ZERO) return null // Trajectories are parallel or coincident.
    val pqx = px * (py + vy) - py * (px + vx)
    val opqx = other.px * (other.py + other.vy) - other.py * (other.px + other.vx)
    val nx = opqx * vx - pqx * other.vx
    val ny = opqx * vy - pqx * other.vy
    return TrajectoryPoint(nx.toBigDecimal() / d.toBigDecimal(), ny.toBigDecimal() / d.toBigDecimal())
}

private fun Trajectory.isInFuture(p: TrajectoryPoint): Boolean =
    when {
        vx > ZERO && p.x < px.toBigDecimal() -> false
        vx < ZERO && p.x > px.toBigDecimal() -> false
        vy > ZERO && p.y < py.toBigDecimal() -> false
        vy < ZERO && p.y > py.toBigDecimal() -> false
        else -> true
    }

private fun TrajectoryPoint.isInRange(b0: BigDecimal, b1: BigDecimal): Boolean =
    x in b0..b1 && b0 <= y && y <= b1

private operator fun LongRange.times(scale: Long): LongRange =
    first * scale..last * scale
