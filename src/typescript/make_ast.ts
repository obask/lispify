/* eslint-disable no-unused-vars */

// @strict: true

import * as util from "util";

const myLispProgram = "(+ (+ 1 1) \n 2 3)"

type CodeTree = ABranch | ALeaf

class ABranch {
    kind: "aBranch"
    value: Array<CodeTree>

    constructor(value: Array<CodeTree>) {
        this.value = value
    }

    toString() {
        return "(LP " + (this.value.map(x => x.toString()).join(" ")) + " RP)"
    }

}

class ALeaf {
    kind: "aLeaf"
    item: string

    constructor(item: string) {
        this.item = item
    }

    toString() {
        return this.item;
    }

    [util.inspect.custom]() {
        return "" + this.item;
    }

}

function makeAST(tokens: Array<string>, state: Array<CodeTree>): [CodeTree, Array<string>] {
    if (tokens.length == 0) {
        return [new ABranch(state.reverse()), []]
    }
    let head = tokens[0]
    let tail = tokens.slice(1)
    let code_tree
    let rest
    switch (head) {
        case "(":
            [code_tree, rest] = makeAST(tail, [])
            state.push(code_tree)
            return makeAST(rest, state)
        case ")":
            return [new ABranch(state), tail]
        default:
            state.push(new ALeaf(head))
            return makeAST(tail, state)
    }
}


function makeSingleAST(tokens: Array<string>, pos = 0): [CodeTree, number] {
    let state = []
    switch (tokens[pos]) {
        case "(":
            while (tokens[pos] !== "(") {
                let tree: CodeTree
                [tree, pos] = makeSingleAST(tokens, pos + 1)
                state.push(tree)
            }
            return [new ABranch(state), pos + 1]
        case ")":
            throw 'Unexpected token ")"';
        default:
            return [new ALeaf(tokens[pos]), pos + 1]
    }
}


function print(something) {
    console.log(util.inspect(something, false, null, true /* enable colors */))
}

function main() {


    // let data = new ABranch([new ABranch([new ALeaf("dsad"), new ALeaf("1234")])])
    // let data = new ABranch([new ALeaf("dsad"), new ALeaf("1234")])

    // let data = new ATMP([new ATMP(["dsad"])])
    // console.log(data.value.map(x => x.toString()).join("^"))
    // console.log(data.toString())
    // console.log(new ALeaf("dsad".toString())
    // print(data)


    let tokens = myLispProgram
        .split("(").join(" ( ")
        .split(")").join(" ) ")
        .split(/\s+/)
        .filter(s => s !== "")
    console.log(tokens)
    let [code, _] = makeAST(tokens, [])
    console.log(code.toString())

}

main()
