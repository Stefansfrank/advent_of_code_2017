import java.io.File

const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"

fun readTxtFile(name: String): List<String> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines
}

fun cntValid(lines: List<String>, ana: Boolean): Int {
    var sum = 0
    for (ln in lines) {
        val mp = mutableMapOf<String, Boolean>()
        var dupl = false
        ln.split(' ').forEach{ ss ->
            val ss2 = if (ana) { ss.toCharArray().sorted().joinToString("") } else { ss }
            dupl = dupl || (mp.getOrDefault(ss2, false))
            mp[ss2] = true
        }
        sum += if (dupl) { 0 } else { 1 }
    }
    return sum
}

fun main() {
    val lines = readTxtFile("d4.input.txt")
    println("\nValid pass phrases (Part 1): $bold$red${cntValid(lines, false)}$reset")
    println("Valid pass phrases (Part 2): $bold$red${cntValid(lines, true)}$reset")
}