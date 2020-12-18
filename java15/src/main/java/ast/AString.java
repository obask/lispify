package ast;


import java.util.List;

public record AString(String value) implements ALeaf {

    @Override
    public String toString() {
        return value;
    }

    @Override
    public ALeaf applyAST(List<ALeaf> args, ast.State st) {
        return null;
    }

    @Override
    public ALeaf evalAST(ast.State state) {
        return null;
    }
}