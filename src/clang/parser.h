//
// Created by Oleg Baskakov on 4/25/16.
//

#ifndef UNTITLED_PARSER_H
#define UNTITLED_PARSER_H

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <assert.h>





const size_t TREE_DEPTH = 16;

enum TypeTags {
    AST_NUMBER,
    AST_STRING,
    AST_DOUBLE,
    AST_BRANCH,
    AST_IF,
    AST_LAMBDA,
    AST_FUNCTION,
    AST_DEFINE,
    AST_TRUE,
    AST_FALSE,

    AST_NULL
};


struct ANumber {
    long value;
};

struct ADouble {
    double value;
};

struct AFunction {
    const char args[7][16];
    size_t size;
    const struct ATree *code;

};

struct AString {
    char value[32];
};


struct ABranch {
    struct ATree *value;
    size_t size;
};


struct ATree {

    enum TypeTags tag;
    union {
        struct ABranch branch;
        struct ANumber val1;
        struct ADouble val2;
        struct AString val3;
        struct AFunction lambda;
    };

};


struct ATree
make_ast(const char* *cont, struct ATree *state, size_t *len) {
    const struct Lexem token = tokenize(cont);
    switch (token.tok) {
        case TOK_NULL:
        case TOK_RPAREN: {
            const size_t sz = sizeof(struct ATree) * *len;
            struct ATree *buf = malloc(sz);
            memcpy(buf, state, sz);
            struct ATree tmp = {AST_BRANCH, buf, *len};
            return tmp;
        }
        case TOK_LPAREN: {
            size_t tmp_len = 0;
            struct ATree tmp_buf[TREE_DEPTH];
            const struct ATree tree = make_ast(cont, tmp_buf, &tmp_len);
            state[*len] = tree;
            *len += 1;
            return make_ast(cont, state, len);
        }
        case TOK_INT: {
            struct ATree tmp;
            tmp.tag = AST_NUMBER;
            sscanf(token.data, "%ld", &tmp.val1.value);
            state[*len] = tmp;
            *len += 1;
            return make_ast(cont, state, len);
        }
        case TOK_DOUBLE: {
            struct ATree tmp;
            tmp.tag = AST_DOUBLE;
            sscanf(token.data, "%lf", &tmp.val2.value);
            state[*len] = tmp;
            *len += 1;
            return make_ast(cont, state, len);
        }
        case TOK_ATOM: {
            struct ATree *tmp = &state[*len];
            tmp->tag = AST_STRING;
            strncpy(tmp->val3.value, token.data, token.size);
            tmp->val3.value[token.size] = '\0';
            if (strcmp(tmp->val3.value, "if")) {
                tmp->tag = AST_IF;
            } else if (strcmp(tmp->val3.value, "define")) {
                tmp->tag = AST_DEFINE;
            } else if (strcmp(tmp->val3.value, "lambda")) {
                    tmp->tag = AST_LAMBDA;
            }
            *len += 1;
            return make_ast(cont, state, len);
        }
    }

    struct ATree tmp = {AST_NULL};
    return tmp;
}


void
print_ast(struct ATree tree) {
    switch (tree.tag) {
        case AST_BRANCH: {
            printf("AST_BRANCH:\n");
            for (int i=0; i<tree.branch.size; ++i) {
                print_ast(tree.branch.value[i]);
            }
            break;
        }
        case AST_IF:
        case AST_LAMBDA:
        case AST_DEFINE:
        case AST_STRING: {
            printf("AST_STRING: %s\n", tree.val3.value);
            break;
        }
        case AST_NUMBER: {
            printf("AST_NUMBER: %ld\n", tree.val1.value);
            break;
        }
        case AST_DOUBLE: {
            printf("AST_DOUBLE: %lf\n", tree.val2.value);
            break;
        }
        case AST_NULL: {
            break;
        }
        case AST_TRUE:
            printf("AST_TRUE\n");
            break;
        case AST_FALSE:
            printf("AST_FALSE\n");
            break;
    }
}



#endif //UNTITLED_PARSER_H
