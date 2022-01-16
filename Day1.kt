import java.io.File

const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"

fun readTxtFile(name: String): List<String> {
    val lines = mutableListOf<String>()
    File(name).useLines { ls -> ls.forEach { lines.add(it) }}
    return lines
}

fun main() {
    val inp  = readTxtFile("d1.input.txt")
    val inpD = inp[0].flatMap { listOf( it.digitToInt() ) }
    var sum = 0
    inpD.forEachIndexed{ ix, vl -> sum += if (vl == inpD[(ix + 1) % inpD.size]) { vl } else { 0 } }
    println("\nPart1: $red$bold$sum$reset")
    sum = 0
    inpD.forEachIndexed{ ix, vl -> sum += if (vl == inpD[(ix + inpD.size/2) % inpD.size]) { vl } else { 0 } }
    println("Part2: $red$bold$sum$reset")
 }