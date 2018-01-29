package ast;


import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.List;

@AllArgsConstructor
public class ANumber extends ALeaf {
    @NonNull
    final Integer value;

    @Override
    public String toString() {
        return value.toString();
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