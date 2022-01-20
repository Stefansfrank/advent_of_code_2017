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

// I am representing all 2x2, 3x3, and 4x4 squares as a 4, 9, 16-bit integers
// highest bit top left then in book reading order to lowest bit bottom right
// thus I have to express rotations and mirroring in these coordinates
// which I do below for 2x2 and 3x3 (as I need these to expand the rules input)

// This makes the code rather wordy but it executes part 2 in 150 ms on a JVM

// rotation of a 2x2 matrix (90 degree cc)
fun rot2(n: Int):Int {
    var res = (1 and n) shl 2
    res += (2 and n) shr 1
    res += (4 and n) shl 1
    return res + ((8 and n) shr 2)
}

// mirroring of a 2x2 matrix (across vertical axis)
fun mir2(n: Int):Int {
    var res = (1 and n) shl 1
    res += (2 and n) shr 1
    res += (4 and n) shl 1
    return res + ((8 and n) shr 1)
}

// rotation of a 3x3 matrix (90 degree cc)
fun rot3(n: Int):Int {
    var res = (1 and n) shl 6
    res += (2 and n) shl 2
    res += (4 and n) shr 2
    res += (8 and n) shl 4
    res += (16 and n)
    res += (32 and n) shr 4
    res += (64 and n) shl 2
    res += (128 and n) shr 2
    return res + ((256 and n) shr 6)
}

// mirroring of a 2x2 matrix (across vertical axis)
fun mir3(n: Int):Int {
    var res = (1 and n) shl 2
    res += (2 and n)
    res += (4 and n) shr 2
    res += (8 and n) shl 2
    res += (16 and n)
    res += (32 and n) shr 2
    res += (64 and n) shl 2
    res += (128 and n)
    return res + ((256 and n) shr 2)
}

// converting of a string of type "..#/#.#/##" into integers representing the square
// in my scheme described above
fun conv(s: String):Int {
    return s.toCharArray().filter{ it != '/' }.map { if (it == '#') 1 else 0 }.reduce{ acc, it -> acc * 2 + it }
}

// the input data is converted into two map<Int,Int> maps
// m2 maps 2x2 matrices to 3x3
// m3 maps 3x3 matrices to 4x4
data class Mp (val s2: Map<Int, Int>, val s3: Map<Int, Int>)

// parses the input rules into the two maps
// adding rules for all possible transformations of the left side of the input
fun parse(inp: List<String>):Mp {
    val re2 = "^([.#]{2}/[.#]{2}) => ([.#]{3}/[.#]{3}/[.#]{3})$".toRegex()
    val re3 = "^([.#]{3}/[.#]{3}/[.#]{3}) => ([.#]{4}/[.#]{4}/[.#]{4}/[.#]{4})$".toRegex()
    val s2 = mutableMapOf<Int, Int>()
    val s3 = mutableMapOf<Int, Int>()
    for (ln in inp) {
        if (re2.find(ln) != null) { // this is a 2x2 -> 3x3 rules
            val m = re2.find(ln)!!.groupValues
            var r = conv(m[1])
            s2[r] = conv(m[2])
            // rotating three times by 90 degrees, mirroring and rotating three more times
            // covers all possible orientations. The use of a map takes care of duplicates
            (1..3).forEach { _ -> r = rot2(r); s2[r] = conv(m[2]) }
            r = mir2(r); s2[r] = conv(m[2])
            (1..3).forEach { _ -> r = rot2(r); s2[r] = conv(m[2]) }
        } else if (re3.find(ln) != null) {
            val m = re3.find(ln)!!.groupValues
            var r = conv(m[1])
            s3[r] = conv(m[2])
            // see above for the transformations
            (1..3).forEach { _ -> r = rot3(r); s3[r] = conv(m[2]) }
            r = mir3(r); s3[r] = conv(m[2])
            (1..3).forEach { _ -> r = rot3(r); s3[r] = conv(m[2]) }
        }
    }
    return Mp(s2, s3)
}

// a grid with meta-information about the grid block size (2,3, or 4) and
// how often that block is repeated across
data class Grid (var blk: Int, var rpt: Int, var bits: List<List<Int>>) {

    // counts the bits in the grid
    fun bits():Int = bits.fold(0) { acc, ln -> acc + ln.fold(0) { ac2, v -> ac2 + v.countOneBits() } }

    // prints out the grid with '.' and '#' for debugging
    fun dump() {
        when (blk) {
            2 -> bits.forEach {
                println( it.fold("") { acc, num -> acc + num.toString(2).padStart(4, '0').substring(0..1).replace('0','.').replace('1', '#') } )
                println( it.fold("") { acc, num -> acc + num.toString(2).padStart(4, '0').substring(2..3).replace('0','.').replace('1', '#') } )
            }
            3 -> bits.forEach {
                println( it.fold("") { acc, num -> acc + num.toString(2).padStart(9, '0').substring(0..2).replace('0','.').replace('1', '#') } )
                println( it.fold("") { acc, num -> acc + num.toString(2).padStart(9, '0').substring(3..5).replace('0','.').replace('1', '#') } )
                println( it.fold("") { acc, num -> acc + num.toString(2).padStart(9, '0').substring(6..8).replace('0','.').replace('1', '#') } )
            }
            4 -> bits.forEach {
                println( it.fold("") { acc, num -> acc + num.toString(2).padStart(16, '0').substring(0..3).replace('0','.').replace('1', '#') } )
                println( it.fold("") { acc, num -> acc + num.toString(2).padStart(16, '0').substring(4..7).replace('0','.').replace('1', '#') } )
                println( it.fold("") { acc, num -> acc + num.toString(2).padStart(16, '0').substring(8..11).replace('0','.').replace('1', '#') } )
                println( it.fold("") { acc, num -> acc + num.toString(2).padStart(16, '0').substring(12..15).replace('0','.').replace('1', '#') } )
            }
        }
    }
}

// this function transforms a grid of 4x4 blocks into a grid of 2x2 blocks
fun trans42(bits: List<List<Int>>): List<List<Int>> {
    val nBits = mutableListOf<List<Int>>()
    for (ln in bits) {
        val nLn1 = mutableListOf<Int>()
        val nLn2 = mutableListOf<Int>()
        for (sq in ln) {
            nLn1. add(((sq and 32768) shr 12) + ((sq and 16384) shr 12) + ((sq and 2048) shr 10) + ((sq and 1024) shr 10))
            nLn1. add(((sq and 8192) shr 10) + ((sq and 4096) shr 10) + ((sq and 512) shr 8) + ((sq and 256) shr 8))
            nLn2. add(((sq and 128) shr 4) + ((sq and 64) shr 4) + ((sq and 8) shr 2) + ((sq and 4) shr 2))
            nLn2. add(((sq and 32) shr 2) + ((sq and 16) shr 2) + (sq and 2) + (sq and 1))
        }
        nBits.add(nLn1)
        nBits.add(nLn2)
    }
    return nBits
}

// this function transforms a grid of 3x3 blocks into 2x2 blocks
// under the assumption that there is an even number of 3x3 blocks
fun trans32(bits: List<List<Int>>): List<List<Int>> {
    val nBits = mutableListOf<List<Int>>()
    for (lx in bits.indices step 2) {
        val nLn1 = mutableListOf<Int>()
        val nLn2 = mutableListOf<Int>()
        val nLn3 = mutableListOf<Int>()
        for (ix in bits[lx].indices step 2) {
            val s1 = bits[lx][ix]
            val s2 = bits[lx][ix+1]
            val s3 = bits[lx+1][ix]
            val s4 = bits[lx+1][ix+1]
            nLn1. add(((s1 and 256) shr 5) + ((s1 and 128) shr 5) + ((s1 and 32) shr 4) + ((s1 and 16) shr 4))
            nLn1. add(((s1 and 64) shr 3) + ((s2 and 256) shr 6) + ((s1 and 8) shr 2) + ((s2 and 32) shr 5))
            nLn1. add(((s2 and 128) shr 4) + ((s2 and 64) shr 4) + ((s2 and 16) shr 3) + ((s2 and 8) shr 3))
            nLn2. add(((s1 and 4) shl 1) + ((s1 and 2) shl 1) + ((s3 and 256) shr 7) + ((s3 and 128) shr 7))
            nLn2. add(((s1 and 1) shl 3) + (s2 and 4) + ((s3 and 64) shr 5) + ((s4 and 256) shr 8))
            nLn2. add(((s2 and 2) shl 2) + ((s2 and 1) shl 2) + ((s4 and 128) shr 6) + ((s4 and 64) shr 6))
            nLn3. add(((s3 and 32) shr 2) + ((s3 and 16) shr 2) + ((s3 and 4) shr 1) + ((s3 and 2) shr 1))
            nLn3. add((s3 and 8) + ((s4 and 32) shr 3) + ((s3 and 1) shl 1) + ((s4 and 4) shr 2))
            nLn3. add(((s4 and 16) shr 1) + ((s4 and 8) shr 1) + (s4 and 2) + (s4 and 1))
        }
        nBits.add(nLn1)
        nBits.add(nLn2)
        nBits.add(nLn3)
    }
    return nBits
}

// this is one step of grid evolution
fun step(grid: Grid, mp: Mp): Grid {

    val nBits = mutableListOf<List<Int>>()
    return if ((grid.blk * grid.rpt) % 2 == 0) {    // an even number means we need a grid of 2x2
        when (grid.blk) {
            4 -> trans42(grid.bits)                 // transform down from 4x4 blocks
            3 -> trans32(grid.bits)                 // transform down from 3x3 blocks
            else -> grid.bits                       // I don't think there are ever 2x2 blocks after a transformation
        }.forEach { ln -> nBits.add( ln.map { mp.s2[it]!! }) }  // executes the mapping rules
        Grid(3, nBits.size, nBits)
    } else {                                        // if we already have 3x3 and it's not even we stick with them (not sure if that ever happens)
        grid.bits.forEach { ln -> nBits.add( ln.map { mp.s3[it]!! }) }
        Grid(4, grid.rpt , nBits)
    }
}

fun main() {

    val start = System.nanoTime()
    val mp = parse(readTxtFile("d21.input.txt"))
    var grid = Grid(3, 1, listOf( listOf( 143 )))

    (1..5).forEach { _ -> grid = step(grid, mp) }
    println("\nAfter 5 iterations, the art has $red$bold${grid.bits()}$reset bits set")
    (1..13).forEach { _ -> grid = step(grid, mp) }
    println("After 18 iterations, the art has $red$bold${grid.bits()}$reset bits set")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")

}
