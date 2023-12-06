const val RED_CUBES = 12
const val GREEN_CUBES = 13
const val BLUE_CUBES = 14

data class Game(val id: Int, val selections: List<Selection>)
data class Selection(val red: Int, val green: Int, val blue: Int)

fun main() {
    Day<Int>(2)
        .part1(8, ::part1)
        .part2(2286, ::part2)
}

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
