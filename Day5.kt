import java.io.File

const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"

fun readTxtFileInt(name: String): List<Int> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines.map { it.toInt() }
}

fun run(mem: MutableList<Int>, neg: Boolean): Int {
    var cnt = 1 ; var pc = 0
    while (true) {
        val npc = pc + mem[pc]
        if (npc in 0 until mem.size) {
            mem[pc] += if (neg && mem[pc] > 2) -1 else 1
            pc = npc
            cnt += 1
        } else return cnt
    }
}

fun main() {
    val mem = readTxtFileInt("d5.input.txt")
    println("\nOut of bounds after $red$bold${run(mem.toMutableList(), false)}$reset steps with +1 jumps")
    println("Out of bounds after $red$bold${run(mem.toMutableList(), true)}$reset steps with +/-1 jumps")
}