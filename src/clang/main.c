#include <stdio.h>
#include <string.h>
#include <assert.h>
#include "tokenizer.h"
#include "parser.h"


struct StringMap {
    const char *keys[256];
    struct ATree values[256];
    size_t size;
};

struct StringMap
map_create() {
    struct StringMap tmp;
    tmp.size = 0;
    return tmp;
}

struct ATree
map_get(struct StringMap const *map, const char* key) {
    for (int i=0; i<map->size; ++i) {
        if (strcmp(map->keys[i], key)) {
            return map->values[i];
        }
    }
    return {AST_NULL};
}

int
map_contains(struct StringMap const *map, const char* key) {
    for (int i=0; i<map->size; ++i) {
        if (strcmp(map->keys[i], key)) {
            return 1;
        }
    }
    return 0;
}

void
map_set(struct StringMap *map, const char* key, const struct ATree new_val) {
    for (int i=0; i<map->size; ++i) {
        if (strcmp(map->keys[i], key)) {
            map->values[i] = new_val;
        }
    }
    map->values[map->size] = new_val;
    map->size += 1;
}


void
print_tokens(const char *ss) {
    struct Lexem curr = tokenize(&ss);
    while (curr.tok != NULL) {
        char buf[100];
        strncpy(buf, curr.data, curr.size);
        buf[curr.size] = '\0';
        printf("%d: %s\n", curr.tok, buf);
        curr = tokenize(&ss);
    }
}


inline int
is_leaf(enum TypeTags tag) {
    switch (tag) {
        case AST_BRANCH:
            return 0;
        default:
            return 1;
    }
}

struct ATree
ast_eval(const struct ATree tt, struct StringMap *st);

struct ATree
ast_macro(struct ATree fun, const struct ATree* args, const size_t len, void *st) {
    if (fun.tag == AST_IF) {
        if (ast_eval(args[0], st).tag == AST_TRUE) {
            return ast_eval(args[1], st);
        } else {
            return ast_eval(args[2], st);
        }
    } else if (fun.tag == AST_DEFINE) {
        assert(args[0].tag == AST_STRING);
        const struct ATree tmp = ast_eval(args[1], st);
        map_set(st, args[0].val3.value, tmp);
        return {AST_NULL};
    } else if (fun.tag == AST_LAMBDA) {
        struct ATree tmp = {AST_FUNCTION};
        assert(args[0].tag == AST_BRANCH);
        tmp.lambda.size = len;
        for (int i=0; i<len; ++i) {
            strcpy(tmp.lambda.args[i], args[i].val3.value);
        }
        tmp.lambda.code = &args[1];
        return tmp;
    }

}


struct ATree
ast_apply(struct ATree fun, const struct ATree* args, const size_t len, void *st) {
    struct ATree tmp = {};
    return tmp;
}


struct ATree
ast_eval(const struct ATree tt, struct StringMap *st) {
    if (tt.tag == AST_BRANCH) {
        const struct ATree head = tt.branch.value[0];
        const struct ATree *tail = tt.branch.value + 1;
        struct ATree fun = ast_eval(head, st);
        switch (head.tag) {
            case AST_IF:
            case AST_DEFINE:
            case AST_LAMBDA: {
                return ast_macro(fun, tail, tt.branch.size-1, st);
            }
            case AST_STRING: {
                // FIXME user-defined macro are here
                struct ATree args[tt.branch.size-1];
                for (int i=0; i < tt.branch.size-1; ++i) {
                    args[i] = ast_eval(tail[i], st);
                }
                return ast_apply(fun, args, tt.branch.size-1, st);
            }
            case AST_BRANCH: {
                struct ATree args[tt.branch.size-1];
                for (int i=0; i< tt.branch.size-1; ++i) {
                    args[i] = ast_eval(tail[i], st);
                }
                return ast_apply(fun, args, tt.branch.size-1, st);
            }
            default: {
                printf("ast_eval PANIC\n");
                exit(1);
            }
        }

    } else if (tt.tag == AST_STRING) {
        const struct ATree pVoid = map_get(st, tt.val3.value);
        return (pVoid.tag != AST_NULL) ? pVoid : tt;
    } else if (is_leaf(tt.tag)) {
        return tt;
    } else {
        printf("ast_eval PANIC\n");
        exit(1);
    }
}


int
main() {
    char *s;
    const char *start;
    size_t length;

//    test_tokenizer();

    char tmp[100];
    const char *ss = tmp;
    strncpy(tmp, "(dsa(( dsa aaa)) 1234)", 100);

//    print_tokens(ss);

    struct ATree buf[100];
    size_t len = 0;
    const struct ATree tree = make_ast(&ss, buf, &len);


//    printf("%lu\n", tree.branch.size);

    print_ast(tree);

    printf("Hello, world\n");
    return 0;
}
