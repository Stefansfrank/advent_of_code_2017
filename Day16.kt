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

// the class representation of a move - parameters designed for speed
data class Move (val op: Int, val ip1: Int, val ip2: Int, val cp1: Char, val cp2: Char)

// parses dance move into the fast class representation
fun parse(moves: List<String>): List<Move> {
    val nmv = mutableListOf<Move>()
    for (mv in moves) when (mv[0]) {
        's' -> nmv.add( Move(0, mv.substring(1).toInt(), 0, ' ', ' '))
        'x' -> nmv.add( Move(1, mv.substring(1, mv.indexOf('/')).toInt(),
            mv.substring(mv.indexOf('/') + 1).toInt(), ' ', ' '))
        'p' -> nmv.add( Move(2, 0, 0, mv[1], mv[3]))
    }
    return nmv
}

// executing the moves
fun dance(progsv: MutableList<Char>, moves: List<Move>): MutableList<Char> {

    var progs = progsv
    var cut: Int
    var cut2: Int
    for (mv in moves) {
        when (mv.op) {
            0 -> { cut = progs.size - mv.ip1
                    progs = (progs.slice(cut until progs.size) +
                        progs.slice(0 until cut)).toMutableList() }
            1 -> { progs[mv.ip1] = progs[mv.ip2].also { progs[mv.ip2] = progs[mv.ip1] } }
            2 -> { cut = progs.indexOf(mv.cp1); cut2 = progs.indexOf(mv.cp2)
                    progs[cut] = progs[cut2].also { progs[cut2] = progs[cut] } }
        }
    }
    return progs
}

fun main() {

    val start = System.nanoTime()
    val moves = parse(readTxtFile("d16.input.txt")[0].split(','))
    val len = 16
    val progsInit = (0 until len).map { 'a' + it  }.toMutableList()
    var progs = progsInit

    // Part 1
    println("\nAfter executing all dance moves once, the programs are in order $red$bold" +
             "${dance(progs, moves).fold("") { acc, it -> acc + it.toString() }}$reset")

    // Part 2 - looking for the looping frequency (trusting there is one)
    var freq = 0
    while (true) {
        freq++
        progs = dance(progs, moves)
        if (progs == progsInit) break
    }

    // Part 2 - now dance one billion (modulo frequency ;)) times
    progs = progsInit
    if ((1000000000 % freq) > 0)
        (0 until(1000000000 % freq)).forEach { progs = dance(progs, moves) }

    println("After dancing all moves 1,000,000,000 times, the programs are in the order $red$bold" +
            "${progs.fold("") { acc, it -> acc + it.toString() }}$reset\nThey kill horses, don't they?")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")
}