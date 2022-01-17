import java.io.File

const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"
const val green = "\u001b[32m"


fun readTxtFile(name: String): List<String> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines
}

data class Score (val grp: Int, val jnk: Int)
fun score(inp: String): Score {
    var grpScr = 0
    var jnkCnt = 0
    var lvl = 0
    var jnk = false
    var esc = false
    inp.forEach { when (it) {
        '{' -> if (!jnk && !esc) lvl++ else if (esc) esc = false else jnkCnt++
        '<' -> if (!jnk && !esc) jnk = true else if (esc) esc = false else jnkCnt++
        '!' -> esc = !esc
        '>' -> if (jnk && !esc) jnk = false else esc = false
        '}' -> if (!jnk && !esc) grpScr += lvl-- else if (esc) esc = false else jnkCnt++
        else -> if (esc) esc = false else if (jnk) jnkCnt++
    } }
    return Score(grpScr, jnkCnt)
}

fun main() {
    val start  = System.nanoTime()
    val lines  = readTxtFile("d9.input.txt")

    val result = score(lines[0])
    println("\nThe group scoring is $red$bold${result.grp}")
    println("${result.jnk}$reset garbage encountered.")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")
}