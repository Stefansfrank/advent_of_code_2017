const val red = "\u001b[31m"
const val bold = "\u001b[1m"
const val reset = "\u001b[0m"
const val green = "\u001b[32m"

// builds the buffer pretty much like displayed in the puzzle
// using a regular List (no linked list) but instantiate in the right size
// and just move things around within to avoid resizing overheads
fun build(size: Int, del: Int): MutableList<Int> {
    var cur = 0    // the current position
    var csize = 1  // the current size (the array is created with max size thus I can't use buffer.size)
    val buffer = MutableList(size) { 0 }
    while (csize < size) {
        cur = (cur + del) % csize + 1  // ring simulated by using modulo
        if (cur < csize) buffer.subList(cur, csize).reversed().forEachIndexed { i, v -> buffer[csize-i] = v }
        buffer[cur] = csize++
    }
    return buffer
}

// this is similar to the above, but it does not actually build the buffer and move things around
// it only computes where it would insert which number and watches one particular location remembering
// only what it would put there. That is enough for part 2 as 0 never moves away from location / index 0
// so the number next to 0 is whatever would be put into 1 last. 0 does never move since:
// - if current location is at the very end of buffer, the new insert would be added after extending buffer
// - if current location is at 0, the new insert would be at 1
fun redBuild(size: Int, del: Int, loc: Int): Int {
    var cur = 0
    var csize = 1
    var res = 0
    while (csize < size) {
        cur = (cur + del) % csize + 1
        if (cur == loc) res = csize
                csize++
    }
    return res
}

fun main() {

    val start = System.nanoTime()
    val input = 348
    val size1 = 2017
    val size2 = 50_000_000

    val buffer = build(size1 + 1, input)
    println("\nThe element following $size1 after it's inserted is $red$bold${buffer[1 + buffer.indexOf(size1)]}$reset")

    println("The element following 0 after 50M inserts is $red$bold${redBuild(size2, input, 1)}$reset")

    println("\nElapsed time: $green${"%,d".format(System.nanoTime()-start)}$reset ns")

}
