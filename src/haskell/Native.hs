module Native (nativeFunction) where

	import Type

	import Data.List
	import Data.Maybe

	defun env (AtomValue (StrAtom name):ListValue args:body)
		| length strArgs < length args = error "Incorrect defun: arguments must be string atoms"
		| length (nub args) < length args = error "Incorrect defun: argument names must be unique"
		| otherwise = Defun name strArgs body env
			where
				strArgs = catMaybes $ map strAtom args
	defun env _ = error "Incorrect defun: incorrect syntax"

	quote _ [] = error "Incorrect quote: no argument given"
	quote env (v:[]) = ReturnValue $ QuotedValue v env
	quote _ _ = error "Incorrect quote: only one argument allowed"

	unquote _ _ [] = error "Incorrect unquote: no arguments given"
	unquote eval env (v:[]) = case head $ eval env v of
		ReturnValue (QuotedValue v env') -> head $ eval (env' ++ env) v
		_ -> error "Incorrect unquote: can't unquote non-quoted value"
	unquote _ _ _ = error "Incorrect unquote: only one argument allowed"

	condition eval env (p:a:b:[]) = case head $ eval env p of
		ReturnValue (AtomValue (StrAtom "T")) -> head $ eval env a
		ReturnValue _ -> head $ eval env b
		_ -> error "Incorrect if condition"
	condition _ _ _ = error "Incorrect if: three arguments must be given"

	equals eval env (a:b:[]) = ReturnValue $ AtomValue $ StrAtom $ case (head $ eval env a, head $ eval env b) of
		(ReturnValue (AtomValue (IntAtom a)), ReturnValue (AtomValue (IntAtom b))) -> if a == b then "T" else "NIL"
		_ -> error "Incorrect comparison: only integer values can be compared"

	reduce f eval' env args
		| length intArgs < length args = error $ "Incorrect call: arguments must be integers"
		| length args == 0 = error "Incorrect call: must have at least one argument"
		| otherwise = ReturnValue $ AtomValue $ IntAtom $ foldl f (head intArgs) (tail intArgs)
			where
				intArgs = catMaybes $ map intAtom $ catMaybes $ map (extractReturnValue . head . eval' env) args

	strConst name _ _ = ReturnValue $ AtomValue $ StrAtom name

	nativeFunctions eval' = [
		("defun", defun), ("quote", quote), ("unquote", unquote eval'), ("if", condition eval'),
		("T", strConst "T"), ("NIL", strConst "NIL"),
		("=", equals eval'),
		("+", reduce (+) eval'), ("-", reduce (-) eval'), ("*", reduce (*) eval'), ("/", reduce div eval')]

	nativeFunction eval' name = lookup name $ nativeFunctions eval'