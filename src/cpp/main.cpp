#include <iostream>
#include <vector>
#include <unordered_map>
#include <list>
#include <unordered_set>

#include "ltoken.hpp"
#include "parser.hpp"
#include "utilz.hpp"

using namespace std;



typedef unordered_map<string, ALeaf> StateX;


unordered_set<string> MACROS_vector = {"if", "define", "lambda", "defmacro", "quote", "unquote"};


shared_ptr<ADouble> sum_fun(vector<ALeaf> &args) {
    double res = 0.0;
    cout << to_string(args) << endl;
    for (auto x: args) {
        // match x
        if (typeid(*x) == typeid(ADouble)) {
            auto num = dynamic_pointer_cast<ADouble>(x);
            res += num->value;
            continue;
        }
        if (typeid(*x) == typeid(ANumber)) {
            auto num = dynamic_pointer_cast<ANumber>(x);
            res += num->value;
            continue;
        }
//        if (typeid(*x) == typeid(ALeafX)) {
//            throw logic_error("ALeafX");
//        }
//        if (typeid(*x) == typeid(CodeTreeX)) {
//            throw logic_error("CodeTreeX");
//        }
//        if (typeid(*x) == typeid(AString)) {
//            throw logic_error("AString");
//        }
//        if (typeid(*x) == typeid(ABranch)) {
//            throw logic_error("ABranch");
//        }

        // else {
        throw bad_typeid();
    }
    return make<ADouble>(res);
}

template< typename T1 >
inline bool operator==(const CodeTree& lhs, const shared_ptr<T1>& rhs) {
    if (typeid(*lhs) == typeid(T1)) {
        auto xs = dynamic_pointer_cast<T1>(lhs);
        return xs->value == rhs->value;
    }
    throw bad_typeid();
}

ALeaf evalLambda(ALambda fun, vector<ALeaf> vals, StateX &st);


ALeaf applyAST(CodeTree &fun, vector<ALeaf> &args, StateX &st) {
    cout << "applyAST: " << fun->toString() << endl;
    if (fun == make<AString>("+")) {
        return sum_fun(args);
    }
    if (typeid(*fun) == typeid(ALambda)) {
        auto lam = dynamic_pointer_cast<ALambda>(fun);
        return evalLambda(*lam, args, st);
    }
    if (typeid(*fun) == typeid(AString)) {
        auto var_name = dynamic_pointer_cast<AString>(fun);
        if (st.count(var_name->value)) {
            return st.at(var_name->value);
        }
    }

//    case ALambda(vars, code) => evalLambda[ALeaf](vars, args, code, st)

    // else
    throw bad_typeid();
}

ALeaf evalAST(CodeTree tree, StateX &st);

ALeaf evalLambda(ALambda fun, vector<ALeaf> vals, StateX &st) {
    cout << "evalLambda" << endl;
    auto st1 = st;
    if (fun.args.size() != vals.size())
        throw logic_error("evalLambda vals number");
    for (int i=0; i< vals.size(); ++i) {
        st1[fun.args[i]] = vals[i];
    }
    ALeaf res;
    for (auto cc: fun.code) {
        res = evalAST(cc, st1);
    }
    return res;
}


ALeaf macroAST(CodeTree fun, vector<CodeTree> args, StateX &st) {
    if (fun == make<AString>("if")) {
        auto tt = evalAST(args.front(), st);
        if (tt == make<ANumber>(0)) {
            return evalAST(args.at(2), st);
        } else {
            return evalAST(args.at(1), st);
        }
    }
    if (fun == make<AString>("define")) {
        auto xx = dynamic_pointer_cast<AString>(args.at(0));
        st[xx->value] = evalAST(args.at(1), st);
        return make<ANumber>(0);
    }
    if (fun == make<AString>("lambda")) {
        auto vars_raw = dynamic_pointer_cast<ABranch>(args.at(0));
        vector<string> vars;
        for (auto var_raw: vars_raw->value) {
            auto var = dynamic_pointer_cast<AString>(var_raw);
            vars.push_back(var->value);
        }
        vector<CodeTree> code = get_tail(args);
        return make<ALambda>(vars, code);
    }
    if (typeid(*fun) == typeid(ALambda)) {
        cout << "ALambda" << endl;
        auto lambda_fun = dynamic_pointer_cast<ALambda>(fun);
//        return evalLambda(*lambda_fun, args, st);
        return make<ANumber>(0);
    }
    throw bad_typeid();
}


ALeaf evalAST(CodeTree tree, StateX &st) {
    cout << "evalAST: " << endl;
    // tree match:
    if (typeid(*tree) == typeid(ABranch)) {
        cout << "ABranch" << endl;
        auto xs = dynamic_pointer_cast<ABranch>(tree);
        auto head = xs->value.front();
        // head match
        if (typeid(*head) == typeid(AString)) {
            auto cmd = dynamic_pointer_cast<AString>(head);
            vector<CodeTree> args = get_tail(xs->value);
            auto xx = cmd->value;
            if (MACROS_vector.count(xx)) {
                return macroAST(head, args, st);
            } else {
                vector<ALeaf> calc_args;
                for (auto arg: args) {
                    calc_args.push_back(evalAST(arg, st));
                }
                return applyAST(head, calc_args, st);
            }
        }
        if (typeid(*head) == typeid(ABranch)) {
            // TODO add ABranch lambda support
            throw logic_error("this feature not supported yet");
        }
        // else
        throw bad_typeid();
    }
    if (typeid(*tree) == typeid(AString)) {
        auto ss = dynamic_pointer_cast<AString>(tree);
        if (st.count(ss->value)) {
            return st[ss->value];
        } else {
            return ss;
        }
    }
    if (typeid(*tree) == typeid(ANumber)) {
        ALeaf res = dynamic_pointer_cast<ANumber>(tree);
        return res;
    }
    if (typeid(*tree) == typeid(ADouble)) {
        ALeaf res = dynamic_pointer_cast<ADouble>(tree);
        return res;
    }
    // else
    throw bad_typeid();
}


void print_state(const StateX& st) {
    cout << "state:" << endl;
    for (auto x: st) {
        cout << x.first << ":" << x.second->toString() << endl;
    }
    cout << "--" << endl;
}

int main() {
    string program;
    program += "(define fac (lambda (a b) (+ a b))) \n";
    program += "(fac 1 2)\n";

    cout << "program:" << endl;
    cout << program << endl;

    list<LToken> tokens = tokenize(program);

    cout << "tokens:" << endl;
    cout << to_string(tokens) << endl;

    auto ast = makeFullAST(tokens);

    cout << "AST:" << endl;
    for (auto cmd: ast) {
        cout << cmd->toString() << endl;
    }

    StateX st;
    for (auto cmd: ast) {
        auto res0 = evalAST(cmd, st);
        print_state(st);
        cout << res0->toString() << endl;
    }

    cout << "finish!" << endl;
    return 0;
}

//
//int main() {
//    vector<int> xs = {1,2,3};
//    auto ys = get_tail(xs);
//    for (auto x: ys) {
//        cout << x << endl;
//    }
//}
