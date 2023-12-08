fun main() {
    Day<Int>(7)
        .part1(6440) { input ->
            parseHandsWithBids(input)
                .regularRank()
                .score()
        }
        .part2(5905) { input ->
            parseHandsWithBids(input)
                .jokerRank()
                .score()
        }
}

private typealias Hand = List<Char>

private data class HandWithBid(val hand: Hand, val bid: Int)
private data class Group(val card: Char, val count: Int)
private enum class Type { HighCard, OnePair, TwoPair, ThreeOfAKind, FullHouse, FourOfAKind, FiveOfAKind }

private val RegularOrder = listOf('2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A')
private val JokerOrder = listOf('J', '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'Q', 'K', 'A')

private fun parseHandsWithBids(input: List<String>): List<HandWithBid> =
    input.map { line ->
        val cards = line.take(5).toList()
        val bid = line.drop(6).toInt()
        HandWithBid(cards, bid)
    }

private fun List<HandWithBid>.regularRank(): List<HandWithBid> =
    rank(::regularType, RegularOrder)

private fun List<HandWithBid>.jokerRank(): List<HandWithBid> =
    rank(::jokerType, JokerOrder)

private fun List<HandWithBid>.score(): Int =
    foldIndexed(0) { index, score, handWithBid ->
        val rank = index + 1
        score + rank * handWithBid.bid
    }

private fun List<HandWithBid>.rank(type: (Hand) -> Type, order: List<Char>): List<HandWithBid> =
    sortedWith { a, b ->
        val compareByType = type(a.hand).ordinal - type(b.hand).ordinal
        if (compareByType != 0) {
            compareByType
        } else {
            compareByCardValue(a.hand, b.hand, order)
        }
    }

private fun regularType(hand: Hand): Type =
    type(groups(hand))

private fun jokerType(hand: Hand): Type =
    type(replaceJokers(groups(hand)))

private fun groups(hand: Hand): List<Group> =
    hand
        .groupBy { it }
        .mapValues { (_, values) -> values.size }
        .map { (card, count) -> Group(card, count) }
        .sortedByDescending(Group::count)

private fun replaceJokers(groups: List<Group>): List<Group> {
    val jokers = groups.find { it.card.isJoker() }
    if (jokers != null) {
        val withoutJokers = groups.filterNot { it.card.isJoker() }
        return if (withoutJokers.isEmpty()) {
            listOf(Group('A', 5))
        } else {
            val firstWithoutJokers = withoutJokers.first()
            val boostedGroup = Group(firstWithoutJokers.card, firstWithoutJokers.count + jokers.count)
            listOf(boostedGroup) + withoutJokers.drop(1)
        }
    }
    return groups
}

private fun Char.isJoker(): Boolean =
    this == 'J'

private fun type(groups: List<Group>) =
    when (groups.size) {
        1 -> Type.FiveOfAKind
        2 -> if (groups.first().count == 4) Type.FourOfAKind else Type.FullHouse
        3 -> if (groups.first().count == 3) Type.ThreeOfAKind else Type.TwoPair
        4 -> Type.OnePair
        else -> Type.HighCard
    }

private fun compareByCardValue(a: List<Char>, b: List<Char>, order: List<Char>): Int {
    if (a.isEmpty()) {
        return 0
    }
    val d = cardValue(a.first(), order) - cardValue(b.first(), order)
    return if (d != 0) d else compareByCardValue(a.drop(1), b.drop(1), order)
}

private fun cardValue(card: Char, order: List<Char>): Int =
    order.indexOf(card)
