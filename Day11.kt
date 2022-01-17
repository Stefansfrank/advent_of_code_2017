import java.io.File
import kotlin.math.abs

const val redBold = "\u001b[31m\u001B[1m"
const val reset = "\u001b[0m"
const val green = "\u001b[32m"

fun readTxtFile(name: String): List<String> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines
}

// I represent each location with x,y so that diagonal steps are (1,1), (-1,1) etc.
// and up or down steps are (0,2) / (0,-2)
data class XY(val x: Int, val y: Int) {

    // computes the distance in steps from 0,0
    // the idea is that we need as many steps (n) as the horizontal (x) coordinate requires
    // we can pick them so that we can cover a vertical (y) range of [-x..x]
    // any additional vertical steps need to be added if y is not in this range
    fun dist(): Int = abs(x) + if (abs(x) < abs(y)) (abs(y) - abs(x))/2 else 0
}

// computes the coordinates of the next location in direction 'dir' from 'loc'
fun next(dir: String, loc: XY): XY {
    return when(dir) {
        "n" -> XY(loc.x, loc.y + 2)
        "ne" -> XY(loc.x + 1, loc.y + 1)
        "nw" -> XY(loc.x - 1, loc.y + 1)
        "s" -> XY(loc.x, loc.y - 2)
        "se" -> XY(loc.x + 1, loc.y - 1)
        "sw" -> XY(loc.x - 1, loc.y - 1)
        else -> XY(0, 0)
    }
}

// walks a list of directions starting from 0,0
fun walk(dirs: List<String>) {
    var loc = XY(0, 0)
    var max = 0
    dirs.forEach {
        loc = next(it, loc)
        max = maxOf(max, loc.dist())
    }
    println("\nHis current location is $redBold${loc.dist()}$reset away")
    println("On his travel the furthest distance was $redBold$max$reset")
}

fun main() {
    val start = System.nanoTime()
    val dirs  = readTxtFile("d11.input.txt")[0].split(",")
    walk(dirs)

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")

}