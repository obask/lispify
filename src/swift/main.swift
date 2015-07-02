//
//  main.swift
//  qwerty
//
//  Created by Oleg Baskakov on 6/30/15.
//  Copyright (c) 2015 Oleg Baskakov. All rights reserved.
//

import Foundation


protocol ATree : Printable {

}

struct ABranch: ATree {
    let val: [ATree]
    
    var description: String {
        return "[" + join(" ", val.map({$0.description})) + "]"
    }
}


protocol ALeaf: ATree {
    
}

struct ANumber: ALeaf {
    let val: Double
    var description: String {
        return val.description
    }
}

struct AString: ALeaf {
    let val: String
    var description: String {
        return val
    }
}

let L_PAREN = "("
let R_PAREN = ")"


func parse(tokens: Array<String>, it: Int, var state: Array<ATree>) -> (Int, ATree) {
    if it < tokens.count {
        let curr = tokens[it]
        switch curr {
        case L_PAREN:
//            println("L_PAREN")
            let (cont, aTree) = parse(tokens, it+1, [])
            state.append(aTree)
            return parse(tokens, cont, state)
        case R_PAREN:
//            println("R_PAREN")
            return (it+1, ABranch(val: state))
        default:
//            println("L_ATOM")
            state.append(AString(val: curr))
            return parse(tokens, it+1, state)
        }
    } else {
        return (it, ABranch(val: state))
    }
}


println("BEGIN")

let homeDir = "/Users/obaskakov/"

let file = "code.scm"

let path = homeDir.stringByAppendingPathComponent(file);

let text0 = String(contentsOfFile: path, encoding: NSUTF8StringEncoding, error: nil)

let newString1 = text0!.stringByReplacingOccurrencesOfString("(", withString: " ( ")
let newString2 = newString1.stringByReplacingOccurrencesOfString(")", withString: " ) ")
let fullNameArr = newString2.componentsSeparatedByString("\n")
let tmp0 = fullNameArr.flatMap({$0.componentsSeparatedByString("\t")})
let tmp1 = tmp0.flatMap({$0.componentsSeparatedByString(" ")})
let tmp2 = tmp1.filter({!$0.isEmpty})

var it = 0

while it < tmp2.count {
    let (cont, res) = parse(tmp2, it+1, [])
    println(res)
    it = cont
}


println("END")

