package ast;

import java.util.List;

public interface ATree {

    ALeaf applyAST(List<ALeaf> args, ast.State st);

    ALeaf evalAST(ast.State state);

}
