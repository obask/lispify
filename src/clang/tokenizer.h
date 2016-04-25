//
// Created by Oleg Baskakov on 4/25/16.
//

#ifndef UNTITLED_TOKENIZER_H
#define UNTITLED_TOKENIZER_H

#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <assert.h>


enum Tokens {

    TOK_NULL,
    TOK_LPAREN,
    TOK_RPAREN,
    TOK_INT,
    TOK_DOUBLE,




    TOK_ATOM

};


//const char* const SPACE_CHARS = " \t\n";
const char* const NON_ATOM_CHARS = " \t\n\'\")(.,";

struct Lexem {
    enum Tokens tok;
    const char *data;
    size_t size;
};

void
debug(const char *ss) {
    printf("DEBUG: %s\n", ss);
}

struct Lexem
tokenize(const char* *cont) {
    for (;;) {
        struct Lexem res;
        switch (*cont[0]) {
            case '\0':
                res.tok = TOK_NULL;
                res.data = NULL;
                return res;
            case ' ':
            case '\t':
            case '\n':
                *cont += 1;
                continue;
            case '(':
                res.tok = TOK_LPAREN;
                res.data = NULL;
                res.size = 0;
                *cont += 1;
                return res;
            case ')':
                res.tok = TOK_RPAREN;
                res.data = NULL;
                res.size = 0;
                *cont += 1;
                return res;
            default:
                if (isnumber(*cont[0])) {
                    debug("is number");
                    const char* start = *cont;
                    while (isnumber(*cont[0])) {
                        *cont += 1;
                    }
                    // parse floating point
                    if (*cont[0] == '.') {
                        *cont += 1;
                        while (isnumber(*cont[0])) {
                            *cont += 1;
                        }
                        res.tok = TOK_DOUBLE;
                        res.data = start;
                        res.size = *cont - start;
                        return res;

                    } else {
                        res.tok = TOK_INT;
                        res.data = start;
                        res.size = *cont - start;
                        return res;
                    }
                } else if (isalpha(*cont[0]) || strchr("!@#$%^&*", *cont[0])) {
                    debug("is alpha");

                    // alphanum
                    res.tok = TOK_ATOM;
                    res.data = *cont;
                    char *end = strpbrk(*cont, NON_ATOM_CHARS);
                    res.size = end - *cont;
                    *cont = end;
                    return res;
                } else {
                    printf("tokenize ALARM!!\n");
                    exit(1);
                }

        }
    }

}

void
test_tokenizer() {
    const char *ss = " sadsadsa) DMOAS     ";
    const struct Lexem lexem = tokenize(&ss);
    assert(strncmp(lexem.data, "sadsadsa", lexem.size) == 0);
    assert(ss[0] == ')');
    assert(strcmp(ss, ") DMOAS     ") == 0);
    const struct Lexem lexem2 = tokenize(&ss);
    assert(lexem2.tok == TOK_RPAREN);
    tokenize(&ss);
    const struct Lexem lexem3 = tokenize(&ss);
    assert(lexem3.tok == TOK_NULL);
}



#endif //UNTITLED_TOKENIZER_H
