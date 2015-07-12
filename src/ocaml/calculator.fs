module calculator

type 'a Tree = Leaf of 'a | Branch of ('a Tree list)

type Token = Open | Close | StrToken of string | IntToken of int | ReduceableFuncToken of string | BinaryFunction of string

let reduceableFunctions = [("+", (+)); ("-", (-)); ("*", (*)); ("/", (/))]
let binaryFunctions = [("=", (=)); ("!=", (<>)); ("<", (<))]

let tokenize (str:string) =
    let reduceables s = List.filter ((=) s << fst) reduceableFunctions
    let binaryFuncs s = List.filter ((=) s << fst) binaryFunctions
    let getToken s =
        if s = "" then None elif s = "(" then Some Open elif s = ")" then Some Close
        elif List.length (reduceables s) > 0 then Some (ReduceableFuncToken s)
        elif List.length (binaryFuncs s) > 0 then Some (BinaryFunction s)
        elif fst <| System.Int16.TryParse s then Some (IntToken (int s))
        else Some(StrToken s)
    in List.choose getToken [for s in str.Replace("(", " ( ").Replace(")", " ) ").Replace("\n", " ").Replace("\t", " ").Split(' ') -> s]

let tokenListToTree =
    let rec tokenListToTree' currentBranch = function
        | [] -> (currentBranch, [])
        | Open::rest -> let subTree = tokenListToTree' [] rest in tokenListToTree' (currentBranch@[Branch (subTree |> fst)]) (subTree |> snd)
        | Close::rest -> (currentBranch, rest)
        | token::rest -> tokenListToTree' (currentBranch@[Leaf token]) rest
    in fst << tokenListToTree' []

type Value = NoValue | IntValue of int

let eval =
    let getReduceableFunc s = snd (List.head <| List.filter ((=) s << fst) reduceableFunctions)
    let getBinaryFunc s = snd (List.head <| List.filter ((=) s << fst) binaryFunctions)
    let extractInt (IntValue i) = i
    let rec eval' env = function
        | Leaf (IntToken i) -> (IntValue i)::env
        | Branch (Leaf (StrToken "if")::p::a::b::[]) ->
            let p' = List.head <| eval' env p
            List.head (eval' env (if p' = IntValue 0 then b else a))::env
        | Branch (Leaf (ReduceableFuncToken f)::args) ->
            (List.reduce (fun (IntValue a) (IntValue b) -> IntValue ((getReduceableFunc f) a b)) <| List.map (List.head << eval' env) args)::env
        | Branch (Leaf (BinaryFunction f)::a::b::[]) ->
            IntValue (if ((getBinaryFunc f) (extractInt (List.head <| eval' env a)) (extractInt (List.head <| eval' env b))) then 1 else 0)::env
        | Branch body -> List.fold (fun env exp -> (List.head <| eval' env exp)::env) env body
    in List.choose (fun x -> match x with | IntValue i -> Some i | _ -> None) << List.rev << List.fold eval' [] << tokenListToTree << tokenize

eval "(+ 6 7 (if (< 1 3) 2 3))"
