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

// the scanner class with a computed property 'freq' containing the frequency of
// the oscillation this scanner scans through its range
data class Scanner (val depth: Int, val range: Int, val freq: Int = 2 * (range - 1))

fun parse(lines: List<String>): List<Scanner> {
    val scanners = mutableListOf<Scanner>()
    lines.forEach { val m = "(\\d+): (\\d+)".toRegex().find(it)
                    if (m != null) scanners.add( Scanner( m.groupValues[1].toInt(), m.groupValues[2].toInt())) }
    return scanners
}

fun main() {
    val start = System.nanoTime()
    val input = readTxtFile("d13.input.txt")
    val scanners = parse(input)

    // part 1
    // the idea is that the package arrives at each scanner at time 'depth' so all
    // we have to do is add the severity of a scanner if its depth MOD (frequency of oscillation) is zero
    var sum = 0
    scanners.forEach { if (it.depth % it.freq == 0) sum += it.depth * it.range }
    println("\nThe severity of the package starting right away is $red$bold$sum$reset")

    // part 2
    // the only thing changing is that the time the packet reaches a scanner is now depth + delay
    // run up the delay until no scanner's depth MOD frequency is zero
    var delay = 0
    oflp@while (++delay > 0) {
        for (s in scanners) if ((s.depth + delay) % s.freq == 0) continue@oflp
        break
    }
    println("The package makes it through when starting after $red$bold$delay$reset ps")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")

}
