import java.io.File
import java.util.*

const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"
const val green = "\u001b[32m"

fun readTxtFile(name: String): List<String> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines
}

// operation for the cpu (pulled out of the cpu class as I have two cpu versions
// for part 1 and 2 using the same structure
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

        // opcode (replaced with Int for speed, not sure that that was needed)
        val cmd  = listOf("snd", "rcv", "add", "mul", "mod", "set", "jgz")
        val op   = cmd.indexOf( opln[0] )

        // first parameter (can be relative in jump commands)
        var tmp = opln[1].toIntOrNull()
        val trel = (tmp == null)
        val tgt = if (trel) opln[1][0] - 'a' else tmp!!

        // second parameter
        val srel:Boolean
        var src: Int
        if (op > 1) {
            tmp = opln[2].toIntOrNull()
            srel = (tmp == null)
            src = if (srel) opln[2][0] - 'a' else tmp!!
        } else {
            src = -1
            srel = false
        }
        ops.add( Operation( op, trel, tgt, srel, src) )
    }
    return ops
}

// the actual cpu with a state and a memory full of operations
// implements the run command returning the last played sound
// a sound played is stored in state.snd, state.ip is the instruction pointer and state.regs the registers
class Cpu1 (val state:State = State ( 0, MutableList(26 ) { 0 }, 0 ),
            var mem:MutableList<Operation> = mutableListOf() ) {

    data class State( var ip: Int, val regs: MutableList<Long>, var snd: Long )

    fun run(): Long {
        while (state.ip in mem.indices) {
            val op = mem[state.ip]
            when (op.op) {
                0 -> state.snd = if (op.treg) state.regs[op.tgt] else op.tgt.toLong()
                1 -> if (state.regs[op.tgt] > 0) return state.snd
                2 -> state.regs[op.tgt] += if (op.sreg) state.regs[op.src] else op.src.toLong()
                3 -> state.regs[op.tgt] *= if (op.sreg) state.regs[op.src] else op.src.toLong()
                4 -> state.regs[op.tgt] %= if (op.sreg) state.regs[op.src] else op.src.toLong()
                5 -> state.regs[op.tgt]  = if (op.sreg) state.regs[op.src] else op.src.toLong()
                6 -> if (if (op.treg) (state.regs[op.tgt] > 0) else (op.tgt > 0))
                    state.ip += -1 + if (op.sreg) state.regs[op.src].toInt() else op.src
            }
            state.ip += 1
        }
        return -1
    }
}

// the actual cpu for part 2 with a state and a memory full of operations
// implements the run command
// state.ip is the instruction pointer, state.regs the registers, and state.stat can be
// "NEW" if newly created, "INP" when waiting for input, "EXH" if ip is out of range
class Cpu2 (val state:State = State ( 0, MutableList(26 ) { 0 }, "NEW" ),
           var mem:MutableList<Operation> = mutableListOf() ) {

    data class State( var ip: Int, val regs: MutableList<Long>, var stat: String )

    var inp:Queue<Long> = LinkedList() // the input queue
    var sndCnt = 0L                    // the counter for send commands
    lateinit var cpu:Cpu2              // the other CPU for access to it's input queue

    fun run() {
        while (state.ip in mem.indices) {
            val op = mem[state.ip]
            when (op.op) {
                0 -> {
                    cpu.inp.add(if (op.treg) state.regs[op.tgt] else op.tgt.toLong())
                    sndCnt += 1
                }
                1 -> {
                    val tmp = inp.poll()
                    if (tmp == null) { state.stat = "INP"; return }
                    state.regs[op.tgt] = tmp
                }
                2 -> state.regs[op.tgt] += if (op.sreg) state.regs[op.src] else op.src.toLong()
                3 -> state.regs[op.tgt] *= if (op.sreg) state.regs[op.src] else op.src.toLong()
                4 -> state.regs[op.tgt] %= if (op.sreg) state.regs[op.src] else op.src.toLong()
                5 -> state.regs[op.tgt]  = if (op.sreg) state.regs[op.src] else op.src.toLong()
                6 -> if (if (op.treg) (state.regs[op.tgt] > 0) else (op.tgt > 0))
                    state.ip += -1 + if (op.sreg) state.regs[op.src].toInt() else op.src
            }
            state.ip += 1
        }
        state.stat = "EXH"
        return
    }
}

fun main() {

    val start = System.nanoTime()
    val prog  = parse(readTxtFile("d18.input.txt"))

    val cpu = Cpu1()
    cpu.mem = prog
    println("\nThe last sound played when RCV was executed was: $red$bold${cpu.run()}$reset")

    val cpu0 = Cpu2()
    val cpu1 = Cpu2()
    cpu0.mem = prog
    cpu1.mem = prog
    cpu0.cpu = cpu1 // link each other for access to the queues
    cpu1.cpu = cpu0
    cpu1.state.regs['p' - 'a'] = 1 // set special value for CPU-1
    while (true) {
        cpu0.run()
        cpu1.run()
        if ((cpu0.state.stat == "EXH" || (cpu0.state.stat == "INP" && cpu0.inp.size == 0)) &&
                (cpu1.state.stat == "EXH" || (cpu1.state.stat == "INP" && cpu1.inp.size == 0))) break
    }
    println("CPU-0 stuck with status ${cpu0.state.stat}, CPU-1 with status ${cpu1.state.stat} after sending " +
            "$red$bold${cpu1.sndCnt}$reset integers over to CPU-0")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")

}
