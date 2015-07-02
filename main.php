<?php

$file = "/Users/obaskakov/code.scm";

//$res = readfile($file);

// Loop through our array, show HTML source as HTML source; and line numbers too.
//foreach ($lines as $line) {
//    $line.explode(" ", )
//
//
//}
//
//
//function isValidStatusCode(int $statusCode): bool {
//    return isset($this->statuses[$statusCode]);
//}



function array_restore(array $arr, string $ss) {
    if (empty($arr)) {
        return array();
    }
    $st = array();
    foreach($arr as $x) {
        array_push($st, $x);
        array_push($st, $ss);
    }
    array_pop($st);
    return $st;
}

function split_by(string $line, string $sep)
{
    $st = explode($sep, $line);
    return array_restore($st, $sep);
}

function flatten(array $arr) {
    return call_user_func_array('array_merge', $arr);
};

function flat_map(array $arr1, $callback) {
    return flatten(array_map($callback, $arr1));
}

function string_non_empty(string $ss) {
    return !($ss === "");
}


abstract class ATree {}


class ABranch extends ATree
{
    public $val;

    public function __construct(array $value) {
        $this->val = $value;
    }

    public function __toString(): string {
        $tmp = array_map(function(ATree $x) { return $x->__toString();}, $this->val);
//        print_r($tmp);
        return "[" . implode(" ", $tmp) . "]";
    }

}


abstract class ALeaf extends ATree
{

}

class AString extends ALeaf
{
    public $val;

    public function __construct(string $value) {
        $this->val = $value;
    }

    public function __toString() {
        return $this->val;
    }

}

$g;

function reduce_tree(array $tokens, int $cont, array $state): ATree {
    $L_PAREN = "(";
    $R_PAREN = ")";
    static $it;
    $it = $cont;
    if ($it < count($tokens)) {
        $curr = $tokens[$it];
        $it += 1;
        switch ($curr) {
            case $L_PAREN:
//                echo("L_PAREN\n");
                $aTree = reduce_tree($tokens, $it, []);
                array_push($state, $aTree);
                return reduce_tree($tokens, $it, $state);
            case $R_PAREN:
    //            echo("R_PAREN\n");
                global $g;
                $g = $it;
                return new ABranch($state);
            default:
    //            echo("L_ATOM\n");
                array_push($state, new AString($curr));
                return reduce_tree($tokens, $it, $state);
        }
    } else {
        return new ABranch($state);
    }
}



$lines0 = file($file);

$lines1 = array_map(function(string $ll) {
    return trim($ll);
}, $lines0);




$lines2 = flat_map($lines1, function(string $ll): array {
    return explode(" ", $ll);
});

$lines3 = flat_map($lines2, function(string $ll): array {
    return split_by($ll, "(");
});

$lines4 = flat_map($lines3, function(string $ll): Array {
    return split_by($ll, ")");
});

$lines5 = array_values(array_filter($lines4, string_non_empty));


//while ($it[0] < count($lines5)) {
//    $it[0] += 1;

global $g;
$g = 1;

while ($g < count($lines5)) {
    echo("RESULT:\n");

    $res = reduce_tree($lines5, $g, []);

    echo($res->__toString());
    echo("\n");
}

//print_r($res);

//}

//echo(implode("\n", $res));


?>



