// Go has strange behavior -- in one case it doesn't support generics
// In over case it can't convert []AnyType to []interface[] even pointers
// So it don't support sub-typing that makes generic coding really hard

package main

import (
	"fmt"
	"go/scanner"
	"go/token"
	"io/ioutil"
)

// %!v(PANIC=runtime error: invalid memory address or nil pointer dereference)

func check(e error) {
	if e != nil {
		panic(e)
	}
}

type ATree interface {
	String() string
}

type ABranch struct {
	ATree
	val []ATree // denoted object; or nil
}

type ALeaf interface {
	ATree
}

type ANumber struct {
	ALeaf
	val  string // denoted object; or nil
	kind token.Token
}

type ASymbol struct {
	ALeaf
	val string // denoted object; or nil
}

type AString struct {
	ALeaf
	val string // denoted object; or nil
}

// String returns the string corresponding to the token tok.
// For operators, delimiters, and keywords the string is the actual
// token character sequence (e.g., for the token ADD, the string is
// "+"). For all other tokens the string corresponds to the token
// constant name (e.g. for the token IDENT, the string is "IDENT").
//
func (stmt ABranch) String() string {
	res := "["
	for _, x := range stmt.val {
		res += x.String()
		res += " "
	}
	res += "]"
	return res
}

func (stmt ANumber) String() string {
	return stmt.val
}

func (stmt ASymbol) String() string {
	return stmt.val
}

func (stmt AString) String() string {
	return stmt.val
}

func parseCode(lexer *scanner.Scanner, state []ATree) ATree {
	_, tok, lit := lexer.Scan()
	// skip endlines
	for tok == token.SEMICOLON {
		_, tok, lit = lexer.Scan()
	}
	switch tok {
	case token.EOF:
		fmt.Println("dbg: token.EOF")
		return ABranch{val: state}

	case token.LPAREN, token.LBRACK, token.LBRACE:
		fmt.Println("dbg: token.LPAREN")
		expr := parseCode(lexer, []ATree{})
		return parseCode(lexer, append(state, expr))

	case token.RPAREN, token.RBRACK, token.RBRACE:
		fmt.Println("dbg: token.RPAREN")
		return ABranch{val: state}

	case token.INT, token.FLOAT:
		fmt.Println("dbg: ANumber")
		newElem := ANumber{val: lit, kind: tok}
		return parseCode(lexer, append(state, newElem))

	case token.STRING, token.CHAR:
		fmt.Println("dbg: AString")
		newElem := AString{val: lit}
		return parseCode(lexer, append(state, newElem))

	}
	switch {
	case tok.IsOperator() || tok.IsKeyword() || tok == token.IDENT || tok == token.IF:
		// IsOperator has a bug with brackets and semicolon!!
		fmt.Println("dbg: token.IsOperator")
		newElem := ASymbol{val: lit}
		return parseCode(lexer, append(state, newElem))

	default:
		// Unknown token
		fmt.Println("dbg: default")
		fmt.Printf("\t%s    ->  %q\n", tok, lit)
		panic("SUCCESS default branch of parseCode")
		return nil
	}
}

//default:
////                    System.out.println("L_ATOM");
////                    System.out.println(state.getClass());
//state.add(new AString(curr));
//return reduceTree(tokens, state);
//}
//} else {
//return new ABranch(state);
//}

func ParseFile(fset *token.FileSet, filename string) ATree {

	fmt.Println("dbg 1")

	// get source
	text, err := ioutil.ReadFile(filename)
	if err != nil {
		return nil
	}

	var lexer scanner.Scanner

	fileSet := fset.AddFile(filename, -1, len(text))

	errorHandler := func(pos token.Position, msg string) {
		// FIXME this happened for ILLEGAL tokens, forex '?'
		panic("SUCCESS in scanner errorHandler")
	}

	var m scanner.Mode
	lexer.Init(fileSet, text, errorHandler, m)

	fmt.Println("dbg 2.1")

	// Repeated calls to Scan yield the token sequence found in the input.
	//	for {
	//		_, tok, lit := lexer.Scan()
	//		if tok == token.EOF {
	//			break
	//		}
	//		fmt.Printf("\t%s    %q\n", tok, lit)
	//	}
	//

	fmt.Println("dbg 2.2")

	return parseCode(&lexer, []ATree{})
}

func main() {
	filename := "/Users/obaskakov/IdeaProjects/goCrazy/code.scm"
	fset := token.NewFileSet()
	tree := ParseFile(fset, filename)

	fmt.Println("dbg 3")

	//	tree := ABranch{val: []ATree{
	//		ASymbol{val: "main"},
	//		ASymbol{val: "dsa"},
	//		ABranch{val: []ATree{
	//			ASymbol{val: "+"},
	//			ANumber{val: "1"},
	//			ANumber{val: "2"},
	//			},},
	//	},}

	fmt.Println(tree)

}

func main2() {

	filename := "/Users/obaskakov/IdeaProjects/goCrazy/code.scm"
	src, err := ioutil.ReadFile(filename)
	check(err)

	//	src := []byte("cos(x) + 1i*sin(x) // Euler")

	// Initialize the scanner.
	var s scanner.Scanner
	fset := token.NewFileSet()
	file := fset.AddFile("", fset.Base(), len(src)) // register input "file"
	s.Init(file, src, nil /* no error handler */, scanner.ScanComments)

	// Repeated calls to Scan yield the token sequence found in the input.
	for {
		_, tok, lit := s.Scan()
		if tok == token.EOF {
			break
		}
		fmt.Printf("\t%s    %q\n", tok, lit)
		//		fmt.Printf("%s\t%s\t%q\n", fset.Position(pos), tok, lit)
	}

}
