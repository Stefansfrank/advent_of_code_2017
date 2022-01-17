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

// holding the details of an instruction
data class Instr ( val reg: String, val smd: Int, val cReg: String, val op: String, val cmp: Int )

// parse instructions into class
fun parse(inp: List<String>): List<Instr> {
    val insts = mutableListOf<Instr>()
    for (ln in inp) {
        val match = "^(\\w+) (inc|dec) (-?\\d+) if (\\w+) ([<>=!]{1,2}) (-?\\d+)".toRegex().find(ln)
        if (match != null) {
            val reg = match.groupValues[1]
            val smd = (if (match.groupValues[2] == "inc") 1 else -1) * match.groupValues[3].toInt()
            val cReg = match.groupValues[4]
            val op  = match.groupValues[5]
            val cmp = match.groupValues[6].toInt()
            insts.add(Instr(reg, smd, cReg, op, cmp))
        }
    }
    return insts
}

// tests one comparison as they occur in the input
fun test(op: String, i1: Int, i2: Int): Boolean {
    return when (op) {
        "<" -> (i1 < i2)
        "<=" -> (i1 <= i2)
        ">" -> (i1 > i2)
        ">=" -> (i1 >= i2)
        "==" -> (i1 == i2)
        "!=" -> (i1 != i2)
        else -> false
    }
}

// a tuple returning the two results
data class MpMax (val mp: Map<String, Int>, val mx: Int)

// runs the instructions and keeps track of the maximum across all registers
fun run(insts: List<Instr>): MpMax {
    val regs = mutableMapOf<String, Int>().withDefault { 0 }
    var mx   = 0
    for (ins in insts)
        if (test(ins.op, regs.getValue(ins.cReg), ins.cmp)) {
            regs[ins.reg] = regs.getValue(ins.reg) + ins.smd
            mx = maxOf(mx, regs.getValue(ins.reg))
        }
    return MpMax(regs, mx)
}

fun main() {
    val start  = System.nanoTime()
    val input  = readTxtFile("d8.input.txt")
    val result = run(parse(input))

    val mx = result.mp.maxByOrNull { it.value }
    println("\nLargest value in register $bold${mx?.key}$reset is $red$bold${mx?.value}$reset")
    println("Largest value in any register during computation was $red$bold${result.mx}$reset")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")

}
