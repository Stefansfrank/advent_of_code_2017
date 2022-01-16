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

class Node (val weight: Int, val dep: List<String>?, var up: String?, var tot: Int?) {

    override fun toString() = this.up.toString() + " " + this.weight.toString() +
            " " + this.dep.toString() + " " + this.tot.toString()
}

fun parse(lines: List<String>): Map<String, Node> {
    val blMp = mutableMapOf<String, Node>()

    // fill all nodes (without resolving 'up')
    for (ln in lines) {
        val wg = "\\d+".toRegex().find(ln)?.value?.toInt() ?: 0
        val nn = Node(wg, "-> [\\w, ]+".toRegex().find(ln)?.value?.substring(3)?.split(", "),null, null)
        val ix = "^\\w+ ".toRegex().find(ln)?.value?.substringBefore(' ')
        if (ix != null) blMp[ix] = nn
    }

    // now add the up dependencies
    for (sn in blMp)
        if (sn.value.dep != null)
            sn.value.dep!!.forEach { blMp[it]?.up = sn.key }

    // find the root
    var root: String? = null
    for (sn in blMp)
        if (sn.value.up == null) {
            println("\nRoot of the tree is block $red$bold${sn.key}$reset")
            root = sn.key
            break
        }

    // now calculate the totals by repeatedly looping through the tree
    while (blMp[root]?.tot == null) {

        // loop through blocks
        blp@for (sn in blMp) {

            // blocks that have not yet set their totals
            if (sn.value.tot == null) {

                // blocks without dependencies
                if (sn.value.dep == null) {
                    sn.value.tot = sn.value.weight
                    continue@blp
                }

                // loop through dependencies and add
                // give up if a dependency does not have tot set yet
                var sm = 0
                for (dn in sn.value.dep!!) {
                    if (blMp[dn]?.tot == null) continue@blp
                    sm += blMp[dn]!!.tot!!
                }
                sn.value.tot = sm + sn.value.weight
            }
        }
    }

    return blMp
}

// function that determines whether there is an imbalance in this node
// by comparing all dependent tot values to the tot value of the first
// returns true if balances
fun detBalance(s: String, blMp: Map<String, Node>): Boolean {
    if (blMp[s]?.dep == null) return true            // no towers on top -> balanced
    val dp = blMp[s]!!.dep!!.map { blMp[it]!!.tot }  // the list of dependent tot values
    if (dp.any { it != dp[0] }) return false         // does it contain any that are not like the first?
    return true
}

// Kotlin has no tuples ..
data class StrInt (val str: String, val int: Int)

// returns the new weight needed to balance the towers
fun balance(blMp: Map<String, Node>): StrInt {

    // determines all unbalanced blocks (the blocks below the unbalanced are all unbalanced
    val unbal = mutableListOf<String>()
    for (sn in blMp) if (!detBalance(sn.key, blMp)) unbal.add(sn.key)

    // finds the one with the lowest weight
    unbal.sortBy { blMp[it]?.tot }

    // now identify the odd one out
    val twrs = blMp[unbal[0]]!!.dep!!
    var oddt = ""
    tl@for (t in twrs) {
        for (t2 in twrs) {
            if (t == t2) continue
            if (blMp[t]?.tot == blMp[t2]?.tot) continue@tl
        }
        oddt = t
        break
    }

    // identify an arbitrary tower that is not the odd one
    val eqt  = if (oddt == twrs[0]) twrs[1] else twrs[0]

    // compute the weight that the odd one out should have.
    // there were many checks before assuring the existence of these values ...
    return StrInt(oddt, blMp[oddt]!!.weight - (blMp[oddt]!!.tot!! - blMp[eqt]!!.tot!!))
}

fun main() {
    val start  = System.nanoTime()
    val lines  = readTxtFile("d7.input.txt")
    val result = balance(parse(lines))
    println("In order to balance the tree:")
    println("The weight of $bold${result.str}$reset would have to be changed to $red$bold${result.int}$reset")
    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")
}