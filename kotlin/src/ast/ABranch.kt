package ast

import java.util.function.Function
import java.util.stream.Collectors

data class ABranch(val value: List<ATree>) : ATree {

    override fun evalAST(state: State?): ALeaf? {
        val head: ATree = value.get(0)
        val function = head.evalAST(state)
        val collect: List<ALeaf?> = value.subList(1, value.size).stream()
            .map(Function { a: ATree -> a.evalAST(state) })
            .collect(Collectors.toList())
        return function?.applyAST(collect, state)
    }

    override fun toString(): String {
        val collect = value.stream()
            .map<String>(Function { obj: ATree -> obj.toString() })
            .collect(Collectors.toList())
        return "[L " + java.lang.String.join(" ", collect) + " R]"
    }

    override fun applyAST(args: List<ALeaf?>?, st: State?): ALeaf {
        return AString("nothing")
    } //    @Override
    //    public String toString() {
    //        final StringBuilder xx = new StringBuilder();
    //        xx.append("[");
    //        for (ATree x : value) {
    //            xx.append(x);
    //            xx.append(" ");
    //        }
    //        xx.append("]");
    //        return xx.toString();
    //    }
}