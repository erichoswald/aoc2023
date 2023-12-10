import strikt.api.*
import strikt.assertions.*

fun main() {
    val test = listOf(
        "Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green",
        "Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue",
        "Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red",
        "Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red",
        "Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green",
    )
    expectThat(part1(test)).isEqualTo(8)

    val input = readInput("Day02")
    println("part 1: ${part1(input)}")

    expectThat(part2(test)).isEqualTo(2286)
    println("part 2: ${part2(input)}")
}

const val RED_CUBES = 12
const val GREEN_CUBES = 13
const val BLUE_CUBES = 14

data class Game(val id: Int, val selections: List<Selection>)
data class Selection(val red: Int, val green: Int, val blue: Int)

private fun part1(input: List<String>): Int =
    input
        .map(::parseGame)
        .filter { it.selections.all(Selection::isPossible) }
        .sumOf(Game::id)

private fun part2(input: List<String>): Int =
    input
        .map(::parseGame)
        .sumOf(Game::power)

fun Selection.isPossible(): Boolean =
    red <= RED_CUBES && green <= GREEN_CUBES && blue <= BLUE_CUBES

fun Selection.power(): Int =
    red * green * blue

fun Game.power(): Int =
    selections
        .reduce { a, b -> Selection(maxOf(a.red, b.red), maxOf(a.green, b.green), maxOf(a.blue, b.blue)) }
        .power()

fun parseGame(line: String): Game {
    val id = line.removePrefix("Game ").takeWhile(Char::isDigit).toInt()
    val selections = parseSelections(line)
    return Game(id, selections)
}

fun parseSelections(line: String): List<Selection> =
    line
        .substringAfter(':')
        .split(';')
        .map(::parseSelection)

fun parseSelection(input: String): Selection =
    input
        .split(',')
        .fold(Selection(0, 0, 0)) { selection, component ->
            val (quantity, color) = component.trim().split(' ')
            val count = quantity.toInt()
            when (color) {
                "red" -> selection.copy(red = selection.red + count)
                "green" -> selection.copy(green = selection.green + count)
                "blue" -> selection.copy(blue = selection.blue + count)
                else -> selection
            }
        }
