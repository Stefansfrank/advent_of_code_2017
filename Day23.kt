import java.io.File
import kotlin.math.sqrt

const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"
const val green = "\u001b[32m"

fun readTxtFile(name: String): List<String> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines
}

// prime checker (no checking for 1)
private fun checkPrime(n: Long): Boolean {

    if (n == 1L) return true
    if (n % 2 == 0L) return false

    val midP = sqrt(n.toDouble()).toInt()
    for(i in 3..midP step 2) if((n % i) == 0L) return false

    return true
}


// operations for the cpu (should be within the cpu class for proper structure)
data class Operation( val op: Int,      // opcode
                      val treg: Boolean,// first (target) parameter as register? (can be absolute for some)
                      val tgt: Int,     // first (target) parameter
                      val sreg: Boolean,// second (source) parameter as register?
                      val src: Int )    // second (target) parameter

// parses the program into the operations structure
fun parse(lines: List<String>): MutableList<Operation> {
    val ops = mutableListOf<Operation>()
    for (ln in lines) {
        val opln = ln.split(' ')

        // opcode (replaced with Int for speed)
        val cmd  = listOf("set", "sub", "mul", "jnz")
        val op   = cmd.indexOf( opln[0] )

        // first parameter (can be relative in jump commands)
        var tmp = opln[1].toIntOrNull()
        val trel = (tmp == null)
        val tgt = if (trel) opln[1][0] - 'a' else tmp!!

        // second parameter
        tmp = opln[2].toIntOrNull()
        val srel = (tmp == null)
        val src = if (srel) opln[2][0] - 'a' else tmp!!

        ops.add( Operation( op, trel, tgt, srel, src) )
    }
    return ops
}

// the actual cpu with a state and a memory full of operations
// implements the run command and saves relevant numbers in the state
//         val cmd  = listOf("set", "sub", "mul", "jnz")
class Cpu (val state:State = State ( 0, MutableList(8 ) { 0 }, 0 ),
            var mem:MutableList<Operation> = mutableListOf() ) {

    // the state has the instruction pointer and the registers
    // as well as the counter of multiplications for part 1
    data class State( var ip: Int, val regs: MutableList<Long>, var mulCnt: Long )

    // executes the program in memory until either the instruction pointer
    // points at an invalid memory location (regular exit) or if an instruction
    // number given as the input parameter is executed the first time (debugging, -1 suppresses debug exit)
    fun run(stp: Int) {
        while (state.ip in mem.indices) {
            val op = mem[state.ip]
            when (op.op) {
                0 -> state.regs[op.tgt]  = if (op.sreg) state.regs[op.src] else op.src.toLong()
                1 -> state.regs[op.tgt] -= if (op.sreg) state.regs[op.src] else op.src.toLong()
                2 -> {
                    state.regs[op.tgt] *= if (op.sreg) state.regs[op.src] else op.src.toLong()
                    state.mulCnt += 1
                }
                3 -> if (if (op.treg) (state.regs[op.tgt] != 0L) else (op.tgt != 0))
                    state.ip += -1 + if (op.sreg) state.regs[op.src].toInt() else op.src
            }
            if (state.ip == stp) return
            state.ip += 1
        }
        return
    }
}

fun main() {

    val start = System.nanoTime()
    val prog  = parse(readTxtFile("d23.input.txt"))

    // straight forward part 1
    var cpu = Cpu()
    cpu.mem = prog
    cpu.run(-1)
    println("\nMultiply was called $red$bold${cpu.state.mulCnt}$reset times in debug mode")

    // part 2 is based on manual input analysis. The code given is basically setting b and c
    // to some initial values so that (b < c)
    // it then tests whether b is prime and afterwards increments b by some amount
    // it does this test and increment until b == c (inclusive that case)
    // every time b is NOT a prime number, h is incremented by 1 so h holds the amount of non-prime b values
    // since other inputs might have different numbers, the following code should help others as well as it runs
    // the cpu until b and c initial values are set. In order to adapt it to other potential inputs,
    // the line after the initial values for b and c are set and the b increment might need to be changed:
    val bcInitLine = 7    // the code line (starting with 0) after which b and c are set to their inital value
    val increment  = 17L  // the increment b is incremented at the end of the program
    // not that if other's input is using different registers for this role, these values need changing
    val rb = 1 // the register number of the register being incremented and tested for prime (a = 0, b = 1 ...)
    val rc = 2 // the register providing the exit comparator for the one above
    cpu = Cpu()
    cpu.mem = prog
    cpu.state.regs[0] = 1L
    cpu.run(bcInitLine)
    var cnt = 0
    (cpu.state.regs[rb] .. cpu.state.regs[rc]).step(increment).forEach{ cnt += if (checkPrime(it)) 0 else 1 }
    println("Register h will eventually be at $red$bold$cnt$reset as the real program quits")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")

}
