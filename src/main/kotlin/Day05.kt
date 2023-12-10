import strikt.api.*
import strikt.assertions.*

fun main() {
    val test = Almanac(readInput("Day05_test"))

    expectThat(test.findLowestLocation()).isEqualTo(35L)

    val almanac = Almanac(readInput("Day05"))
    println("part 1: ${almanac.findLowestLocation()}")

    expectThat(test.findLowestRangeLocation()).isEqualTo(46L)

    println("oart 2: ${almanac.findLowestRangeLocation()}")
}

private val MapRegex = """(.+)-to-(.+) map:""".toRegex()

private class Almanac(input: List<String>) {
    private val seeds = mutableListOf<Long>()
    private val maps = mutableMapOf<String, Mapping>()

    init {
        parse(input)
    }

    fun findLowestLocation(): Long =
        seeds.minOf(::findLocation)

    fun findLowestRangeLocation(): Long =
        seedRanges().minOf { findLocations(it).first().first }

    private fun seedRanges(): List<LongRange> =
        seeds
            .chunked(2)
            .map { (first, length) -> first..<(first + length) }

    private fun findLocation(seed: Long): Long {
        var mapping = maps["seed"]
        var sourceValue = seed
        while (mapping != null && !mapping.isLocation) {
            sourceValue = mapping.map(sourceValue)
            mapping = maps[mapping.destinationCategory]
        }
        return mapping?.map(sourceValue) ?: sourceValue
    }

    private fun findLocations(seedRange: LongRange): List<LongRange> {
        var mapping = maps["seed"]
        var ranges = listOf(seedRange)
        while (mapping != null && !mapping.isLocation) {
            ranges = mapping.map(ranges)
            mapping = maps[mapping.destinationCategory]
        }
        return mapping?.map(ranges) ?: ranges
    }

    private fun parse(input: List<String>) {
        require(input.first().startsWith("seeds: "))
        seeds += input.first().substringAfter("seeds: ").numbers()

        var lineNo = 1
        while (lineNo < input.size) {
            val line = input[lineNo]
            val match = MapRegex.find(line)
            if (match != null) {
                val source = match.groupValues[1]
                val destination = match.groupValues[2]
                val rangeMappings = mutableListOf<RangeMapping>()
                var nextLine = input.getOrNull(++lineNo)
                while (!nextLine.isNullOrBlank()) {
                    val (destinationStart, sourceStart, length) = nextLine.numbers()
                    rangeMappings += RangeMapping(sourceStart..(sourceStart + length), destinationStart)
                    nextLine = input.getOrNull(++lineNo)
                }
                maps += source to Mapping(destination, rangeMappings)
            }
            lineNo++
        }
    }

    private fun String.numbers(): List<Long> =
        split(' ').mapNotNull(String::toLongOrNull)
}

private class Mapping(val destinationCategory: String, rangeMappings: List<RangeMapping>) {
    val rangeMappings = completeRangeMappings(rangeMappings.sortedBy(RangeMapping::first))

    val isLocation: Boolean = destinationCategory == "location"

    private fun completeRangeMappings(rangeMappings: List<RangeMapping>): List<RangeMapping> {
        val completeRangeMappings = mutableListOf<RangeMapping>()
        var first = 0L
        for (rangeMapping in rangeMappings) {
            if (first < rangeMapping.first()) {
                completeRangeMappings += RangeMapping(first..<rangeMapping.first(), first)
            }
            completeRangeMappings += rangeMapping
            first = rangeMapping.last() + 1L
        }
        completeRangeMappings += RangeMapping(first..Long.MAX_VALUE, first)
        return completeRangeMappings
    }

    fun map(sourceValue: Long): Long =
        rangeMappings.find { sourceValue in it.sourceRange }?.map(sourceValue) ?: sourceValue

    fun map(sourceRanges: List<LongRange>): List<LongRange> {
        val destinationRanges = mutableListOf<LongRange>()
        for (sourceRange in sourceRanges) {
            for (mapping in rangeMappings) {
                mapping.addCommonRange(sourceRange, destinationRanges)
            }
        }
        return destinationRanges.sortedBy(LongRange::first)
    }
}

private class RangeMapping(val sourceRange: LongRange, destinationStart: Long) {
    val delta = destinationStart - sourceRange.first

    fun first() = sourceRange.first
    fun last() = sourceRange.last

    fun map(sourceValue: Long): Long =
        sourceValue + delta

    fun addCommonRange(range: LongRange, ranges: MutableList<LongRange>) {
        if (intersects(range)) {
            val first = maxOf(sourceRange.first, range.first) + delta
            val last = minOf(sourceRange.last, range.last) + delta
            ranges += first..last
        }
    }

    fun intersects(range: LongRange): Boolean =
        sourceRange.first <= range.last && range.first <= sourceRange.last
}
