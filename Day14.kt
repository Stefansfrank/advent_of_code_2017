const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"
const val green = "\u001b[32m"

data class XY( val x: Int, val y: Int ) {
    fun plus(v: XY) = XY( this.x + v.x, this.y + v.y)
}

// executes an inversion of a sub-list of length 'amt' from the start
// it returns the list starting at 'amt + skip' so the next inversion can start at 0
fun invert(inp: List<Int>, amt: Int, skip: Int): List<Int> {
    val tmp = inp.slice(0 until amt).reversed() + inp.slice(amt until inp.size)
    val ix  = (amt + skip) % inp.size
    return tmp.slice(ix until inp.size) + tmp.slice(0 until ix)
}

// executes a list of inversions 'rpt' times starting with a sorted list of length 'len'
fun run(inversions: List<Int>, rpt: Int, len: Int): List<Int> {

    var skip = 0
    // since I rearrange the ring so that the current position is always 0
    // I need to remember where the zero element of the original list is
    var zero = 0
    var ring = (0 until len).toList()

    for (i in 0 until rpt) {
        inversions.forEach {
            ring = invert(ring, it, skip)
            zero = (zero + 2*len - (it + skip)) % len
            skip = ++skip % len
        }
    }

    // upon return the list is put into original zero position
    return ring.slice(zero until len) + ring.slice(0 until zero)
}

// computes the Dense Hash
fun dense(ring: List<Int>): List<Int> {
    val dns = mutableListOf<Int>()
    (0 until 16).forEach {
        dns.add(ring.slice(it*16 until (it+1)*16).reduce{ i1, i2 -> i1.xor(i2) })
    }
    return dns
}

// the knot hash class with a few helpers
data class Knot(var dns: List<Int>) {
    //fun hex() = this.dns.fold("") { acc, it -> acc + "%02x".format(it) }
    fun bin() = this.dns.fold("") { acc, it -> acc + Integer.toBinaryString(it).padStart(8,'0') }
    fun cnt() = this.dns.fold(0) { acc, it -> acc + it.countOneBits() }
}

// computes the Knot Hash
fun knot(inp: String): Knot {
    return Knot( dense(run(inp.toCharArray().map{ it.code } + listOf(17, 31, 73, 47, 23), 64, 256)) )
}

// traverses a region and marks all members with 'ix'
fun traverse(x: Int, y: Int, map: MutableList<MutableList<Int>>, ix: Int) {
    val dir = listOf( XY(0,-1), XY(1,0), XY(0,1), XY(-1,0) )
    val next = mutableListOf( XY(x,y) )
    while (next.size > 0) {
        val xy = next.removeFirst()
        dir.forEach { val p = xy.plus(it); if (map[p.y][p.x] == 0) next.add(p) }
        map[xy.y][xy.x] = ix
    }
}

// looks for regions by going through the whole map and finding 1 bits
// that are not yet marked with the index of a region. Once found, traverse
// is called that will mark all points of that region
fun regions(map: MutableList<MutableList<Int>>): Int {

    var cur = 0 // the region index
    for (y in 1..128) {
        for (x in 1..128) {
            if (map[y][x] == 0) traverse(x, y, map, ++cur)
        }
    }
    return cur
}

fun main() {
    val start = System.nanoTime()
    val input = "xlqgujun" // my AoC17.d14 input
    //val input = "flqrgnkx"

    // Part 1 (and prep of the map for 2)
    var sum = 0
    val map = mutableListOf( MutableList(129) { -1 } )
    (0..127).forEach { ix ->
        val k = knot("$input-$ix")
        sum += k.cnt()
        map.add((listOf( -1 ) + k.bin().toCharArray().map { if (it == '0') -1 else 0 } + listOf( -1 )).toMutableList())
    }
    map.add( MutableList(129) { -1 } )
    println("\nSum of all set bits on the map: $red$bold$sum")

    // Part 2
    // the map is -1 for each location with bit 0
    //             0 for each location with bit 1 but not yet assigned to a region
    //             n (>0) for each location with bit 1 in region n
    println("${regions(map)}$reset disjointed regions identified.")
    println("\nElapsed time: $green${"%,d".format(System.nanoTime() - start)}$reset ns")
}