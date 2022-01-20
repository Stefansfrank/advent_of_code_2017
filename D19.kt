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

// padding the map so tests for continuations don't throw exceptions
fun pad(mz: List<String>): List<String> {
    val nmz = mutableListOf<String>()
    nmz.add(" ".repeat(mz[0].length + 2))
    mz.forEach { nmz.add(" $it ") }
    nmz.add(" ".repeat(mz.last().length + 2))
    return nmz
}

data class XY(val x:Int, val y:Int) {
    private val dirX = listOf(0, 1, 0, -1)
    private val dirY = listOf(-1, 0, 1, 0)
    fun go(dir:Int) = XY(x + dirX[dir], y + dirY[dir])
}

// result class (Kotlin has no tuples)
data class Trace(var steps:Int, var id:String)

// traverses the maze returning the result
fun travel(mz: List<String>):Trace {

    val vld = listOf('|', '-', '|', '-') // valid continuators per direction

    var loc = XY(mz[1].indexOf('|'), 1)
    var nloc:XY
    var nn:XY
    var dir = 2
    val trc = Trace(1, "")

    // nothing difficult here, I always go only one step but might evaluate
    // the subsequent step and adapt the direction if on a '+'
    // collecting letters and step counts on the way
    while (true) {
        nloc = loc.go(dir)
        nloc = when (mz[nloc.y][nloc.x]) {
            vld[dir] -> nloc
            in ('A'..'Z') -> { trc.id += mz[nloc.y][nloc.x]; nloc }
            vld[(dir + 1) % 4] -> { nn = nloc.go(dir)
                if (mz[nn.y][nn.x] == vld[dir] || mz[nn.y][nn.x] in ('A'..'Z')) nloc else loc }
            '+' -> { nn = nloc.go((dir + 1) % 4)
                dir = if (mz[nn.y][nn.x] == vld[(dir + 1) % 4] || mz[nn.y][nn.x] in ('A'..'Z'))
                    (dir + 1) % 4 else (dir + 3) % 4
                nloc }
            else -> loc
        }
        if (nloc == loc) break
        loc = nloc
        trc.steps += 1
    }
    return trc
}

fun main() {
    val start = System.nanoTime()
    val mz = readTxtFile("d19.input.txt")
    val trc = travel(pad(mz))
    println("\nThe packet traveled $red$bold${trc.steps}$reset steps collecting letters " +
        "$red$bold${trc.id}$reset")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")
}