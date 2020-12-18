package ast

interface ATree {
    fun applyAST(args: List<ALeaf?>?, st: State?): ALeaf?
    fun evalAST(state: State?): ALeaf?
}