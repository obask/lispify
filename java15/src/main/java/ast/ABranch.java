package ast;

import java.util.List;

import static java.util.stream.Collectors.toList;

public record ABranch(List<ATree> value) implements ATree {

    public ALeaf evalAST(ast.State state) {
        final var head = value.get(0);
        final var function = head.evalAST(state);
        final List<ALeaf> collect = value.subList(1, value.size()).stream().map((a) -> a.evalAST(state)).collect(toList());
        return function.applyAST(collect, state);
    }

    public String toString() {
        return "[L " + String.join(" ", value.stream().map(Object::toString).collect(toList())) + " R]";
    }

    @Override
    public ALeaf applyAST(List<ALeaf> args, ast.State state) {
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
