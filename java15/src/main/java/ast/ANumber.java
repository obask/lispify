package ast;


import java.util.List;

public record ANumber(Integer value) implements ALeaf {

    @Override
    public String toString() {
        return value.toString();
    }

    public ALeaf applyAST(List<ALeaf> args, ast.State st) {
        return null;
    }

    public ALeaf evalAST(ast.State state) {
        return null;
    }
}