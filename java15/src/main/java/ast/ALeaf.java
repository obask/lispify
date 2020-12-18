package ast;

public sealed interface ALeaf extends ATree permits ANumber, AString, ADouble {
}
