use std::env;

#[derive(Debug)]
pub enum ATree {
    ABranch(Vec<ATree>),
    ALeaf(String),
}


fn parse_ast<'a>(tokens: &'a [&'a str]) -> (ATree, &'a [&'a str]) {
    let head = tokens[0];
    if head == "(" {
        let mut state = Vec::new();
        let mut current = tokens[0];
        let mut rest = &tokens[1..];
        while current != ")" {
            let (node, tmp) = parse_ast(rest);
            rest = tmp;
            current = rest[0];
            state.push(node);
        }
        return (ATree::ABranch(state), &rest[1..]);
    } else if head == ")" {
        panic!("expected expression, but found ')'.")
    } else {
        return (ATree::ALeaf(head.to_string()), &tokens[1..]);
    }
}

fn eval_ast(tree: &ATree) -> f64 {
    match tree {
        ATree::ABranch(stuff) => {
            match &stuff[0] {
                ATree::ABranch(_) => {
                    panic!("lambda functions aren't supported");
                }
                ATree::ALeaf(x) => {
                    match &x[..] {
                        "+" => {
                            return stuff[1..].iter()
                                .map(|x| eval_ast(x))
                                .sum();
                        }
                        "-" => {
                            return eval_ast(&stuff[1]) - eval_ast(&stuff[2]);
                        }
                        _ => panic!("unknown operation {:?}", x)
                    }
                }
            }
            213.11
        }
        ATree::ALeaf(x) => {
            let four: f64 = x.parse().unwrap();
            return four;
        }
    }
}

fn main() {
    let input = "(+ 1 2(- 3 4))";

    let tokens0 = input
        .replace("(", " ( ")
        .replace(")", " ) ");

    let tokens1 = tokens0
        .split_ascii_whitespace()
        .collect::<Vec<_>>();

    let (result, rest) = parse_ast(&tokens1.as_slice());

    println!("result = {:?}", result);
    println!("rest = {:?}", rest);
    println!("eval = {:?}", eval_ast(&result));
}
