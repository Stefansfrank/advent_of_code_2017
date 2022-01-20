import java.io.File
import kotlin.math.abs

const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"
const val green = "\u001b[32m"

fun readTxtFile(name: String): List<String> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines
}

// parses the input into the particle class
fun parse(inp: List<String>): List<Part> {
    val parts = mutableListOf<Part>()
    for ((ix, ln) in inp.withIndex()) {
        val m = "^p=<(-?\\d+),(-?\\d+),(-?\\d+)>, v=<(-?\\d+),(-?\\d+),(-?\\d+)>, a=<(-?\\d+),(-?\\d+),(-?\\d+)>".toRegex().find(ln)?.groupValues
        if (m != null) {
            val n = m.subList(1, m.size).map { it.toInt() }
            parts.add(Part( ix, XYZ( n[0], n[1], n[2]), XYZ( n[3], n[4], n[5]), XYZ( n[6], n[7], n[8])))
        }
    }
    return parts
}

// a 3D point implementing Manhattan distance and vector addition (and a terser toString())
data class XYZ(val x:Int, val y: Int, val z: Int) {
    fun add(vc: XYZ) = XYZ(x+vc.x, y+vc.y, z+vc.z)
    fun man(p2: XYZ) = abs(x - p2.x) + abs(y - p2.y) + abs(z - p2.z)
    override fun toString() = "($x,$y,$z)"
}

// the particle class
data class Part(val ix: Int, var loc: XYZ, var vel: XYZ, val acc: XYZ) {
    fun mv() { vel = vel.add(acc); loc = loc.add(vel) }
}

// simulating the movements is somewhat trivial
// collision is detected by creating a map<XYZ,Int> for each cycle
// that shows how many particles are at a location
fun sim(parts: List<Part>, n: Int): List<Part> {
    var nparts = parts.toMutableList()
    (1..n).forEach{ _ ->
        val mp = mutableMapOf<XYZ, Int>()
        nparts.forEach { it.mv(); mp[it.loc] = mp.getOrDefault(it.loc, 0) + 1 }
        nparts = nparts.filter { mp[it.loc]!! < 2 }.toMutableList()
    }
    return nparts
}

fun main() {

    val start = System.nanoTime()
    val parts = parse(readTxtFile("d20.input.txt"))

    // for part 1, the long term closest to zero is the one with the least acceleration.
    // ties are broken up by velocity and by location ... the latter two are not perfect
    // we would have to run the simulation for some time before they are true some initial configurations
    // might break that assumptions ... but it worked for me
    // if it does not work, it should work if executed after the simulation below in part 2
    println( "\nThe particle that will stay closes to 0,0,0 in the long term is particle no. $red$bold" +
            parts.sortedWith(compareBy( { it.acc.man( XYZ(0,0,0) )} ,
                                { it.vel.man( XYZ(0,0,0) )} ,
                                { it.loc.man( XYZ(0,0,0) )} ))[0].ix.toString() + reset)

    // after a look at the numbers on the input, 5000 simulated moves should be more than enough ...
    println( "After some collisions there are $red$bold${sim(parts, 5000).size}$reset particles left")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")

}
