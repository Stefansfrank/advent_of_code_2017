const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"
const val green = "\u001b[32m"

fun run(genAv: Long, genBv: Long, n: Long, p2: Boolean): Long {

    val facA = 16807L
    val facB = 48271L
    val mod  = 2147483647L

    var genA = genAv
    var genB = genBv
    var sum = 0L

    repeat((1..n).count()) {
        genA = genA * facA % mod
        while (p2 && genA % 4 != 0L) genA = genA * facA % mod
        genB = genB * facB % mod
        while (p2 && genB % 8 != 0L) genB = genB * facB % mod
        if ((65535L and genA) == (65535L and genB)) sum++
    }

    return sum
}

fun main() {

    val start = System.nanoTime()
    val genA = 634L // my AoC17.d15 input
    val genB = 301L // my AoC17.d15 input

    println("\nWith synchronous generators, the lower 16 bits are equal " +
                "$red$bold${run(genA, genB, 40000000, false)}$reset times during the first 40M runs")
    println("With divisibility of 4 and 8 enforced, the lower 16 bits are equal " +
                "$red$bold${run(genA, genB, 5000000, true)}$reset times during the first 5M runs")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")

}
