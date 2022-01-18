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

data class Link ( val from: Int, val to: List<Int>)

fun parse(lines: List<String>): List<Link> {
    val links = mutableListOf<Link>()
    for (ln in lines) {
        val match = "^(\\d+) <-> ([0-9, ]+)".toRegex().find(ln)
        if (match != null) {
            val from = match.groupValues[1].toInt()
            val to = match.groupValues[2].split(", ").map { it.toInt() }
            links.add(Link(from, to))
        }
    }
    return links
}

fun reduce(groups: MutableList<Set<Int>>): MutableList<Set<Int>> {

    var changed = true
    while (changed) {
        changed = false
        lp@ for ((i1, g1) in groups.withIndex()) {
            for ((i2, g2) in groups.withIndex()) {
                if (i1 == i2) continue
                if (g1.intersect(g2).isNotEmpty()) {
                    changed = true
                    groups.add(g1 + g2)
                    groups.remove(g1)
                    groups.remove(g2)
                    break@lp
                }
            }
        }
    }
    return groups
}

fun groups(links: List<Link>): MutableList<Set<Int>> {
    val groups = mutableListOf<Set<Int>>()
    links.forEach { groups.add((it.to + it.from).toSet()) }
    reduce(groups)
    return groups
}

fun main() {
    val start = System.nanoTime()
    val lines = readTxtFile("d12.input.txt")
    val links = parse(lines)
    val groups = groups(links)

    groups.forEach { if (it.contains(0)) println("\nThe group containing 0 is of size $red$bold${it.size}$reset") }
    println("The total amount of disjunctive groups is $red$bold${groups.size}$reset")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime() - start)}$reset ns")

}