#ifndef LTOKEN_HPP
#define LTOKEN_HPP

#include <list>
#include "utilz.hpp"


using namespace std;

struct LTokenX {
    virtual string toString() = 0;
};

typedef shared_ptr<LTokenX> LToken;

struct LParenthesisLeft: public LTokenX {
    virtual string toString() override {
        return "[";
    }
};

struct LParenthesisRight: public LTokenX {
    virtual string toString() override {
        return "]";
    }
};

struct LInt: public LTokenX {
    long value;
    LInt(long a) : value(a) {};

    virtual string toString() override {
        return to_string(value);
    }
};

struct LDouble: public LTokenX {
    double value;
    LDouble(double a) : value(a) {};

    virtual string toString() override {
        return to_string(value);
    }
};

struct LAtom: public LTokenX {
    string value;
    LAtom(string a) : value(a) {};

    virtual string toString() override {
        return value;
    }
};


LToken make_ltoken(string x) {
    if (x == "(") return make<LParenthesisLeft>();
    if (x == ")") return make<LParenthesisRight>();
    if (is_number(x)) {
        long t1 = stol(x);
        double t2 = stod(x);
        if (t1 == t2) {
            return make<LInt>(t1);
        } else {
            return make<LDouble>(t2);
        }
    } else {
        return make<LAtom>(x);
    }
}




list<LToken> tokenize(string text) {
    list<LToken> res;
    string curr;
    for (char x: text) {
        switch ( x ) {
            case '(':
            case ')':
            case ' ':
            case '\t':
            case '\n':
                if (not curr.empty()) {
                    res.push_back(make_ltoken(curr));
                    curr.clear();
                }
                if (x == '(' || x == ')') {
                    res.push_back(make_ltoken(string() + x));
                }
                break;
            default:
                curr += x;
                break;
        }
    }
    return res;
}


string to_string(LToken ptr) {
    return ptr->toString();
}


string to_string1(LToken ptr) {
    if (typeid(*ptr) == typeid(LParenthesisLeft)) return "[[[";
    if (typeid(*ptr) == typeid(LParenthesisRight)) return "[[[";
    if (typeid(*ptr) == typeid(LAtom)) {
        shared_ptr<LAtom> x = dynamic_pointer_cast<LAtom>(ptr);
        return string() + "|" + x->value + "|";
    }

    return ptr->toString();
}

#endif
