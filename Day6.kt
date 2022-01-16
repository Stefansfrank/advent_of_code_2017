import java.io.File

const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"

fun readTxtFileInt(name: String): List<Int> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines[0].replace("\\s+".toRegex(), " ").split(' ').map { it.toInt() }
}

// a hash string representing the block state ("n/m/.../")
fun hash(lst: List<Int>) = lst.fold("") { acc, i -> "$acc$i/" }

// Kotlin has no tuples :(
data class CntLp (val cnt: Int, val lp: Int)

// runs until repetition and returns the count of iterations and the loop length
fun runToRpt(blocks: MutableList<Int>): CntLp {
    val hshMp = mutableMapOf( hash(blocks) to 0 )
    var cnt   = 0

    while (true) {
        cnt += 1

        // detect max and redistribute
        val mIx = blocks.indices.fold(0) { acc, i -> if (blocks[i] > blocks[acc]) i else acc }
        val max = blocks[mIx]
        blocks[mIx] = 0
        for (ix in 1..max) {
            blocks[(mIx + ix) % blocks.size] += 1
        }

        // check pattern, compute cycle length and maintain hash
        val h = hash(blocks)
        val first = hshMp[h]
        if (first != null) return CntLp(cnt, cnt-first)
        hshMp[h] = cnt
    }
}

fun main() {
    val blocks = readTxtFileInt("d6.input.txt")
    val rrpt   = runToRpt(blocks.toMutableList())
    println("\nInfinity loop after $red$bold${rrpt.cnt}$reset iterations")
    println("The loop length is $red$bold${rrpt.lp}$reset iterations")
}
