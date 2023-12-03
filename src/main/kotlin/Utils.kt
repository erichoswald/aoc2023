import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.*

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String): List<String> =
    Path("src/main/resources/$name.txt")
        .takeIf { it.exists() }
        ?.readLines()
        ?: emptyList()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)
