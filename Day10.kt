import java.io.File

const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"
const val green = "\u001b[32m"

fun readTxtFileInt(name: String): List<Int> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) } }
    return lines[0].split(',').map { it.toInt() }
}

fun readTxtFile(name: String): List<String> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines
}

// executes an inversion of a sub-list of length 'amt' from the start
// it returns the list starting at 'amt + skip' so the next inversion can start at 0
fun invert(inp: List<Int>, amt: Int, skip: Int): List<Int> {
    val tmp = inp.slice(0 until amt).reversed() + inp.slice(amt until inp.size)
    val ix  = (amt + skip) % inp.size
    return tmp.slice(ix until inp.size) + tmp.slice(0 until ix)
}

// executes a list of inversions 'rpt' times starting with a sorted list of length 'len'
fun run(invs: List<Int>, rpt: Int, len: Int): List<Int> {

    var skip = 0
    // since I rearrange the ring so that the current position is always 0
    // I need to remember where the zero element of the original list is
    var zero = 0
    var ring = (0 until len).toList()

    for (i in 0 until rpt) {
        invs.forEach {
            ring = invert(ring, it, skip)
            zero = (zero + len - (it + skip)) % len
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

fun main() {
    val start = System.nanoTime()
    val len = 256

    // part 1
    val invs1 = readTxtFileInt("d10.input.txt")
    val ring = run(invs1, 1, len)
    println("\nProduct of the first two elements after one round is $red$bold${ring[0] * ring[1]}$reset")

    // part 2
    var invs2 = readTxtFile("d10.input.txt")[0].toCharArray().map{ it.code } + listOf(17, 31, 73, 47, 23)
    val ring2 = run(invs2, 64, len)
    val dns   = dense(ring2)
    print("Knot hash after 64 iterations: $red$bold")
    dns.forEach { print("%02x".format(it)) }
    println(reset)

    println("\nElapsed time: $green${"%,d".format(System.nanoTime() - start)}$reset ns")
}