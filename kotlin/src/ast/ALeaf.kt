package ast

interface ALeaf : ATree

data class ADouble(val value: Int) : ALeaf {
    override fun toString(): String {
        return value.toString()
    }

    override fun applyAST(args: List<ALeaf?>?, st: State?): ALeaf? {
        return AString("")
    }

    override fun evalAST(state: State?): ALeaf? {
        return AString("null")
    }
}

data class ANumber(val value: Int) : ALeaf {
    override fun toString(): String {
        return value.toString()
    }

    override fun applyAST(args: List<ALeaf?>?, st: State?): ALeaf? {
        return AString("nothing")
    }

    override fun evalAST(state: State?): ALeaf? {
        return AString("nothing")
    }
}

data class AString(val value: String) : ALeaf {

    override fun toString(): String {
        return value
    }

    override fun applyAST(args: List<ALeaf?>?, st: State?): ALeaf? {
        return AString("nothing")
    }

    override fun evalAST(state: State?): ALeaf? {
        return AString("nothing")
    }
}

