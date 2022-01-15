import java.io.File

val red = "\u001b[31m"
val bold = "\u001b[1m"
val reset = "\u001b[0m"

fun readTxtFile(name: String): List<String> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines
}

fun parse(inp: List<String>): List<List<Int>> {
    val lines = mutableListOf<List<Int>>()
    inp.forEach { ln ->
        val temp = ln.replace("\\s+".toRegex(), " ").split(' ').map { it.toInt() }
        lines.add(temp)
    }
    return lines
}

fun main() {

    var sum = 0
    var lines = parse(readTxtFile("input.txt"))
    lines.forEach{ ln -> sum += ln.sorted()[ln.size-1] - ln.sorted()[0] }
    println("\nPart 1: $red$bold$sum$reset")

    sum = 0
    for (ln in lines) {
        val lnr = ln.sortedDescending()
         lp@ for (i1 in 0..ln.size-2) {
            for (i2 in i1+1..ln.size-1) {
                if (lnr[i1] % lnr[i2] == 0) {
                    sum += lnr[i1] / lnr[i2]
                    break@lp
                }
            }
        }
    }

    println("Part 2: $red$bold$sum$reset")
}
