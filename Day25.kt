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

// parse into the representation of rules and the Turing machine
fun parse(inp: List<String>):Turing {

    // a lot of direct indexing as the rules are wordy but relatively static
    // in terms of where the important data is located and in things like
    // listing the =0 rules first before the =1 rules ...
    val begin = inp[0][15]
    val check = inp[1].slice(inp[1].indexOf("after")+6 until inp[1].indexOf("steps")-1).toInt()
    val rules = mutableMapOf<Char, Rule>()

    inp.drop(2).chunked(10).forEach{ blk ->
        val key = blk[1][9]
        val nVal = listOf( blk[3][22] - '0', blk[7][22] - '0')
        val nLoc = listOf( if (blk[4][27] == 'r') 1 else -1, if (blk[8][27] == 'r') 1 else -1 )
        val nStat = listOf( blk[5][26], blk[9][26])
        rules[key] = Rule(nVal, nLoc, nStat)
    }

    return Turing(mutableMapOf(), begin, 0, rules, check)
}

// representation of a rule i.e. the stuff that needs to be done for one given state
// all Lists are len(2) and contain the next value, the location addition direction and the next state
// depending on the existing value at the cursor
data class Rule (val nVal:List<Int>, val nLoc:List<Int>, val nStat:List<Char>)

// the Turing machine with a tape, a cursor location, a state, a set of rules, and the checksum counter treshold
class Turing (private val tape:MutableMap<Int,Int>, private var state: Char,
              private var loc:Int, private val rules:Map<Char, Rule>, val check: Int ) {

    // run until check
    fun run():Int {
        repeat(check) {
            val ct = tape.getOrDefault(loc, 0)
            with (rules[state]!!) { tape[loc] = nVal[ct]; loc += nLoc[ct]; state = nStat[ct] }
        }
        return tape.values.sum()
    }
}

fun main() {

    val start = System.nanoTime()
    val turing = parse(readTxtFile("d25.input.txt"))
    println("\nAfter ${turing.check} moves, the tape has a total of $red$bold${turing.run()}$reset bits set")
    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")

}
