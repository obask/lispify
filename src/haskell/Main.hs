module Main (eval, getEvalValues, main) where

	import Type
	import Native
	import Data.Maybe

	sampleProgram = [
		ListValue [AtomValue $ StrAtom "defun", AtomValue $ StrAtom "fact", ListValue [AtomValue $ StrAtom "n"], ListValue [
			AtomValue $ StrAtom "if", ListValue [AtomValue $ StrAtom "=", AtomValue $ StrAtom "n", AtomValue $ IntAtom 0],
			AtomValue $ IntAtom 1, ListValue [AtomValue $ StrAtom "*", AtomValue $ StrAtom "n",
			ListValue [AtomValue $ StrAtom "fact", ListValue [AtomValue $ StrAtom "-", AtomValue $ StrAtom "n", AtomValue $ IntAtom 1]]]]],
		ListValue [AtomValue $ StrAtom "defun", AtomValue $ StrAtom "fact'", ListValue [AtomValue $ StrAtom "acc", AtomValue $ StrAtom "n"], ListValue [
			AtomValue $ StrAtom "if", ListValue [AtomValue $ StrAtom "=", AtomValue $ StrAtom "n", AtomValue $ IntAtom 0],
			AtomValue $ StrAtom "acc", ListValue [AtomValue $ StrAtom "fact'", ListValue [AtomValue $ StrAtom "*", AtomValue $ StrAtom "acc", AtomValue $ StrAtom "n"],
			ListValue [AtomValue $ StrAtom "-", AtomValue $ StrAtom "n", AtomValue $ IntAtom 1]]]],
		ListValue [AtomValue $ StrAtom "fact", AtomValue $ IntAtom 10],
		ListValue [AtomValue $ StrAtom "fact'", AtomValue $ IntAtom 1, AtomValue $ IntAtom 10],
		ListValue [AtomValue $ StrAtom "quote", ListValue [AtomValue $ StrAtom "fact", AtomValue $ IntAtom 10000000]],
		-- AtomValue $ StrAtom "quote", ListValue [AtomValue $ StrAtom "fact", AtomValue $ IntAtom 10000000], -- если раскомментировать, ждать придется долго
		ListValue [AtomValue $ StrAtom "if", AtomValue $ StrAtom "T", AtomValue $ IntAtom 0,
		ListValue [AtomValue $ StrAtom "fact", AtomValue $ IntAtom 10000000]] -- значение факториала не было вычислено, поскольку оно в false ветке
		]

	closureExampleProgram = [
		ListValue [AtomValue $ StrAtom "defun", AtomValue $ StrAtom "x", ListValue [], AtomValue $ IntAtom 1], -- x := 1
		ListValue [AtomValue $ StrAtom "x"], -- 1
		ListValue [AtomValue $ StrAtom "defun", AtomValue $ StrAtom "f", ListValue [], ListValue [AtomValue $ StrAtom "x"]], -- f := x
		ListValue [AtomValue $ StrAtom "defun", AtomValue $ StrAtom "x", ListValue [], AtomValue $ IntAtom 0], -- x := 0
		ListValue [AtomValue $ StrAtom "x"], -- 0
		ListValue [AtomValue $ StrAtom "f"] -- 1
		]

	substitute args (AtomValue (StrAtom s)) = replace (lookup s args)
		where
			replace Nothing = AtomValue (StrAtom s)
			replace (Just value) = value
	substitute args x@(ListValue (AtomValue (StrAtom "quote"):_)) = x
	substitute args (ListValue l) = ListValue (map (substitute args) l)
	substitute _ x = x

	userFunction' env [] name = Nothing
	userFunction' env (Defun f argNames body env':rest) name
		| f == name = Just $ \ env args ->
			if length argNames /= length args then error "Incorrect argument count"
			else head $ foldl eval' (env' ++ env) $ map (substitute $ zip argNames args) body
		| otherwise = userFunction' env rest name
	userFunction' env (_:rest) name = userFunction' env rest name

	userFunction env = userFunction' env env

	eval' env (AtomValue v) = ReturnValue (AtomValue v):env
	eval' env (ListValue []) = ReturnValue (ListValue []):env
	eval' env (ListValue (AtomValue (StrAtom f):args)) = call (nativeFunctionFactory f) (userFunction env f):env
		where
			call (Just nativeFunction) _ = nativeFunction env args
			call _ (Just userFunction) = userFunction env args
			call _ _ = error $ "Couldn't call '" ++ f ++ "'"
	eval' env (ListValue (l:[])) = eval' env l
	eval' env (QuotedValue v env') = eval' (env' ++ env) v
	eval' _ expr = error $ "Couldn't eval " ++ (show expr)

	nativeFunctionFactory = nativeFunction eval'

	eval = let env = [] in foldl eval' env

	getEvalValues = reverse . catMaybes . map extractReturnValue . eval

	main = do
		mapM (putStrLn . show) $ sampleProgram
		putStrLn "-----"
		mapM (putStrLn . show) $ getEvalValues sampleProgram
		putStrLn "====="
		mapM (putStrLn . show) $ closureExampleProgram
		putStrLn "-----"
		mapM (putStrLn . show) $ getEvalValues closureExampleProgram