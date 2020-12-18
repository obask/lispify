import ast.ABranch
import ast.AString
import ast.ATree
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

private val path = Paths.get("/Users/oleg.baskakov/IdeaProjects/lispify/java15/src/main/resources/example.scm")
private const val L_PAREN = "("
private const val R_PAREN = ")"

// TODO replace to splitAsStream method
// Pattern.compile("\\W").splitAsStream("Some sentence");
fun splitMeAsStream(input: String, ch: Char): Stream<String> {
    val value = input.toCharArray()
    val sep = ch.toString()
    var off = 0
    var next = 0
    val limited = false
    val list = ArrayList<String>()
    while (input.indexOf(ch, off).also { next = it } != -1) {
        list.add(input.substring(off, next))
        list.add(sep)
        off = next + 1
    }
    // If no match was found, return this
    if (off == 0) {
        return Stream.of(input)
    }
    // Add remaining segment
    list.add(input.substring(off, value.size))
    return list.stream()
}

fun reduceTree(tokens: Iterator<String?>, state: MutableList<ATree>): ATree {
    return if (tokens.hasNext()) {
        when (val curr = tokens.next()) {
            L_PAREN -> {
                //                    System.out.println("L_PAREN");
                val tmp: MutableList<ATree> = ArrayList()
                val aTree = reduceTree(tokens, tmp)
                state.add(aTree)
                reduceTree(tokens, state)
            }
            R_PAREN -> //                    System.out.println("R_PAREN");
                ABranch(state)
            else -> {
                //                    System.out.println("L_ATOM");
//                    System.out.println(state.getClass());
                state.add(AString(curr!!))
                reduceTree(tokens, state)
            }
        }
    } else {
        ABranch(state)
    }
}

fun main(args: Array<String>) {
    // write your code here
    try {
        val stream0 = Files.lines(path)
        val stream1 = stream0.map { ss: String -> ss.split(" ".toRegex()).toTypedArray() }
            .flatMap { array: Array<String>? -> Arrays.stream(array) }
        val stream2 = stream1.flatMap { ss: String -> splitMeAsStream(ss, L_PAREN[0]) }
        val stream3 = stream2.flatMap { ss: String -> splitMeAsStream(ss, R_PAREN[0]) }
        //            String stream4 = stream3.filter(ss -> !ss.isEmpty()).collect(Collectors.joining("\n"));
//            String[] stmp = stream4.toArray(String[]::new);
//            System.out.println(stream4);
        val objects = stream3.filter { ss: String -> !ss.isEmpty() }.collect(Collectors.toList())
        val stringIterator = objects.iterator()
        while (stringIterator.hasNext()) {
            val next = stringIterator.next()
            assert(next == L_PAREN)
            val aTree = reduceTree(stringIterator, ArrayList())
            println(aTree)
            println("----")
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
