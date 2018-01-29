package ast;


import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;

@AllArgsConstructor
public class AString extends ALeaf {
    @NonNull
    final String value;

    @Override
    public String toString() {
        return value;
    }

    @Override
    ALeaf applyAST(List<ALeaf> args, ast.State st) {
        return null;
    }

    @Override
    ALeaf evalAST(ast.State state) {
        return null;
    }
}