package ast;

import java.util.List;

public abstract class ATree {

    abstract ALeaf applyAST(List<ALeaf> args, ast.State st);

    abstract ALeaf evalAST(ast.State state);

}
