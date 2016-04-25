#ifndef PARSER_HPP
#define PARSER_HPP

#include "ltoken.hpp"
#include "utilz.hpp"

using namespace std;


struct CodeTreeX {
    virtual string toString() = 0;
};

typedef shared_ptr<CodeTreeX> CodeTree;

struct ABranch : CodeTreeX {
    vector<CodeTree> value;

    ABranch(vector<CodeTree> _value)
            : value(_value)
    {}

    virtual string toString() override {
        string res = "[ ";
        for (CodeTree x: value) {
            res += x->toString() + ", ";
        }
        res += "]";
        return res;
    }

};

struct ALeafX: CodeTreeX {
    ALeafX() {};

    virtual string toString() = 0;
};

typedef shared_ptr<ALeafX> ALeaf;

struct AString: ALeafX {
    string value;
    AString(string _value)
            :value(_value)
    {}

    virtual string toString() override {
        return "\"" + this->value + "\"";
    }

};

struct ANumber: ALeafX {
    long value;
    ANumber(long _value)
            :value(_value)
    {}

    virtual string toString() override {
        return to_string(value);
    }

};

struct ADouble: ALeafX {
    double value;
    ADouble(double _value)
            :value(_value)
    {}

    virtual string toString() override {
        return to_string(value);
    }

};

struct ALambda: ALeafX {
    const vector<string> args;
    const vector<CodeTree> code;

    ALambda(const vector<string> &_args, const vector<CodeTree> &_code)
            :args(_args), code(_code)
    {}

    virtual string toString() override {
        return to_string(code);
    }

};


shared_ptr<ABranch> makeAST(list<LToken> &tokens, shared_ptr<vector<CodeTree> > state) {
    if (tokens.empty()) {
        return make<ABranch>(*state);
    } else {
        auto head = tokens.front();
        tokens.pop_front();
        if (typeid(*head) == typeid(LParenthesisLeft)) {
            auto st1 = makeAST(tokens, shared_ptr<vector<CodeTree> >(new vector<CodeTree>));
            state->push_back(st1);
            return makeAST(tokens, state);
        }
        if (typeid(*head) == typeid(LParenthesisRight)) {
            return make<ABranch>(*state);
        }
        if (typeid(*head) == typeid(LAtom)) {
            shared_ptr<LAtom> x = dynamic_pointer_cast<LAtom>(head);
            state->push_back(make<AString>(x->value));
            return makeAST(tokens, state);
        }
        if (typeid(*head) == typeid(LInt)) {
            shared_ptr<LInt> x = dynamic_pointer_cast<LInt>(head);
            state->push_back(make<ANumber>(x->value));
            return makeAST(tokens, state);
        }
        throw 0;
    }
}


string to_string(CodeTree x) {
    return x->toString();
}

string to_string(ALeaf x) {
    return x->toString();
}

vector<CodeTree> makeFullAST(list<LToken> tokens) {
    shared_ptr<ABranch> tmp = makeAST(tokens, shared_ptr<vector<CodeTree> >(new vector<CodeTree>()));
    return tmp->value;
}

#endif
