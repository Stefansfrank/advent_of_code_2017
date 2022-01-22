import java.io.File
import java.lang.Integer.max

const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"
const val green = "\u001b[32m"

fun readTxtFile(name: String): List<String> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines
}

fun parse(inp: List<String>): List<Block> {
    val list = mutableListOf<Block>()
    inp.forEach { list.add( Block( it.split('/').map { n -> n.toInt() } ) ) }
    return list
}

// Representation of a block
class Block(val p: List<Int>) {

    // Determines whether it can be used to continue a bridge with open end 'end'
    fun match(end: Int):Int = if (p[0] == end) p[1] else if (p[1] == end) p[0] else -1
}

// The representation of a bridge
// 'end' is the value of the open end of the bridge (opposite the 0)
// 'useMask' indicates which links are already used (co-indexed with global list of blocks)
class Bridge(val blocks: MutableList<Block>, val useMask: MutableList<Boolean>, var end:Int ) {

    // the strength (bridges are always initialized with 1 block)
    var strength:Int = blocks[0].p[0] + blocks[0].p[1]
}

// a class holding the maximum values for the puzzle answers during bridge construction
data class MaxCnt(var strength:Int = 0, var len:Int = 0, var strengthLongest:Int = 0)

// BFS building bridges
fun build(blocks: List<Block>, max: MaxCnt):List<Bridge> {

    val bridges = mutableListOf<Bridge>()

    // identify potential starting block
    blocks.forEachIndexed { ix, it ->
        if (it.p[0] == 0) bridges.add( Bridge( mutableListOf( it ), MutableList(blocks.size){ it == ix }, it.p[1] ) )
        if (it.p[1] == 0) bridges.add( Bridge( mutableListOf( it ), MutableList(blocks.size){ it == ix }, it.p[0] ) )
    }

    // loops through all bridges, determining potential extensions etc.
    var cur = 0
    while (cur < bridges.size) {

        // loop through all blocks that are not yet used and identify potential next blocks
        blocks.forEachIndexed { ix, blk -> if (!bridges[cur].useMask[ix]) {

            // see whether this block is a match
            val nEnd = blk.match(bridges[cur].end)
            if (nEnd > - 1) {

                // copy the current chain
                val nc = Bridge( bridges[cur].blocks.toMutableList(),
                                 bridges[cur].useMask.toMutableList(), 0 )

                // extend newly created copy
                nc.blocks.add(blk)
                nc.end = nEnd
                nc.useMask[ix] = true
                nc.strength = bridges[cur].strength + blk.p[0] + blk.p[1]

                // add to the list of bridges
                bridges.add(nc)

                // run the max counters for the puzzle answers while creating the bridge
                max.strength = max(max.strength, nc.strength)
                if (nc.blocks.size == max.len) max.strengthLongest = max(max.strengthLongest, nc.strength)
                else if (nc.blocks.size > max.len) {
                    max.len = nc.blocks.size
                    max.strengthLongest = nc.strength
                }
            }
        } }
        cur += 1
    }
    return bridges
}

fun main() {

    val start = System.nanoTime()

    val blocks = parse(readTxtFile("d24.input.txt"))
    val max   = MaxCnt()
    build(blocks, max)
    println("\nThe strength of the strongest bridge possible is $red$bold${max.strength}$reset")
    println("The longest bridge is ${max.len} blocks long with a strength of $red$bold${max.strengthLongest}$reset")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")

}
