package ast;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.val;

import java.util.List;

import static java.util.stream.Collectors.toList;

@AllArgsConstructor
public class ABranch extends ATree {
    @NonNull
    List<ATree> value;

    public ALeaf evalAST(ast.State state) {
        val head = value.get(0);
        val function = head.evalAST(state);
        final List<ALeaf> collect = value.subList(1, value.size()).stream().map((a) -> a.evalAST(state)).collect(toList());
        return function.applyAST(collect, state);
    }

    public String toString() {
        return "[L " + String.join(" ", value.stream().map(Object::toString).collect(toList())) + " R]";
    }

    @Override
    ALeaf applyAST(List<ALeaf> args, ast.State state) {
        return null;
    }

//    @Override
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
