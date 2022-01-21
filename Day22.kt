import java.io.File

const val red   = "\u001b[31m"
const val bold  = "\u001b[1m"
const val reset = "\u001b[0m"
const val green = "\u001b[32m"

fun readTxtFile(name: String): List<String> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines
}

data class XY(val x: Int, val y: Int) {
    private val dirx = listOf(0, 1, 0, -1)
    private val diry = listOf(-1, 0, 1, 0)
    fun mv(dir: Int) = XY(x+dirx[dir], y+diry[dir])
}

fun parse(inp: List<String>): MutableMap<XY, Int> {
    val dl = inp.size / 2
    val mp = mutableMapOf<XY, Int>()
    for ((lx, ln) in inp.withIndex()) ln.forEachIndexed { i, c -> if (c == '#') mp[XY(i-dl, lx-dl)] = 2 }
    return mp
}

data class Carr(var loc: XY, var dir: Int, var infCnt: Int) {
    fun mv(mp: MutableMap<XY, Int>, dl: Int) {
        val tmp = mp.getOrDefault(loc, 0)
        mp[loc] = (tmp + dl) % 4
        dir = (dir + 3 + tmp) % 4
        infCnt += if (mp[loc]!! == 2) 1 else 0
        loc = loc.mv(dir)
    }
}

fun main() {

    val start = System.nanoTime()
    val omp = parse(readTxtFile("d22.input.txt"))

    var mp = omp.toMutableMap()
    var car = Carr(XY(0,0), 0, 0)
    repeat(10000) { car.mv(mp, 2) }
    println("\nThe virus infects $red$bold${car.infCnt}$reset nodes during 10,000 bursts")

    mp = omp.toMutableMap()
    car = Carr(XY(0,0), 0, 0)
    repeat(10_000_000) { car.mv(mp, 1)}
    println("The refined virus infects $red$bold${car.infCnt}$reset nodes during 10,000,000 bursts")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")

}
