import kotlin.math.abs
import kotlin.math.sqrt

const val red = "\u001b[31m"
const val green = "\u001b[32m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"


data class XY(val x: Int, val y: Int) {
    fun mdist(p: XY): Int {
        return abs(p.x - this.x) + abs(p.y - this.y)
    }
}

// this function determines the coordinates if an arbitrary number in the spiral memory
// it uses the fact that each ring has n^2 in the lower left corner with n being all ODD numbers
// it then segments the ring into 4 equal length straight segments, the first one starting at the
// location over the lower right corner (which holds the lowest number of the ring) and goes up to the corner
// with this segmentation, the input number is located on the subsegment and then the coordinates all depend on n
fun detXY(inp: Int): XY {

    // determines the odd n so that n^2 is the highest number
    // in the ring just inside of the ring that 'inp' will be in
    var n = sqrt( inp.toDouble() ).toInt()
    n += (n % 2 - 1)

    // special case for the actual square in the lower right corner
    // (where n is computed too high)
    if (n * n == inp) return XY(n/2, n/2)

    val rng = n / 2 + 1                    // this is the number of the ring inp is in
    val leg = (inp - n * n - 1) / (n + 1)  // this is which of the four sub-segments of the ring inp is in
    val pos = (inp - n * n - 1) % (n + 1)  // this is the position within that subsegment

    var x = 0; var y = 0
    when (leg) {  // the four sub-segments have different coordinate counting
        0 -> { x = rng; y = rng - 1 - pos }
        1 -> { x = rng - 1 - pos; y = - rng }
        2 -> { x = - rng; y = - rng + pos + 1 }
        3 -> { x = - rng + pos + 1; y = rng }
    }
    return XY(x, y)
}

// builds the memory until a value is encountered higher than 'inp'
// use the same ring by ring / sub-segment by sub-segment idea than above
fun buildMap(inp: Int): Int {
    val mp  = mutableMapOf( XY(0,0) to 1 )
    var rng = 0
    while (true) {
        rng += 1

        // sub-segment 1
        for (y in (rng - 1) downTo (-rng)) {
            var v = mp.getOrDefault(XY(rng,y + 1), 0)
            for (i in -1..1) {
                v += mp.getOrDefault(XY(rng - 1,y + i), 0)
            }
            if (v > inp) return v
            mp.put(XY(rng, y), v)
         }

        // sub-segment 2
        for (x in (rng - 1) downTo (-rng)) {
            var v = mp.getOrDefault(XY(x + 1, -rng), 0)
            for (i in -1..1) {
                v += mp.getOrDefault(XY(x + i,1 - rng), 0)
            }
            if (v > inp) return v
            mp.put(XY(x, -rng), v)
         }

        // sub-segment 3
        for (y in (1 - rng) .. rng) {
            var v = mp.getOrDefault(XY(-rng,y - 1), 0)
            for (i in -1..1) {
                v += mp.getOrDefault(XY(1 - rng,y + i), 0)
            }
            if (v > inp) return v
            mp.put(XY(-rng, y), v)
        }

        // sub-segment 4
        for (x in (1 - rng) .. rng) {
            var v = mp.getOrDefault(XY(x - 1, rng), 0)
            for (i in -1..1) {
                v += mp.getOrDefault(XY(x + i,rng - 1), 0)
            }
            if (v > inp) return v
            mp.put(XY(x, rng), v)
        }
    }
}

fun main() {

    val start = System.nanoTime()

    val input = 325489
    println("\nPart 1: $red$bold${detXY(input).mdist(XY(0, 0))}$reset")
    println("Part 2: $red$bold${buildMap(input)}$reset")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")
}