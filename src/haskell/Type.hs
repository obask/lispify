module Type where

	data Atom = StrAtom String | IntAtom Int
		deriving (Show, Eq)

	data Value = AtomValue Atom | ListValue [Value] | QuotedValue Value [EnvEntry]
		deriving (Eq)

	instance Show Value where
		show (AtomValue (StrAtom s)) = s
		show (AtomValue (IntAtom i)) = show i
		show (ListValue l) = '(':(unwords $ map show l) ++ ")"
		show (QuotedValue v _) = "(quote " ++ show v ++ ")"

	data EnvEntry = ReturnValue Value | Defun String [String] [Value] [EnvEntry]
		deriving (Show, Eq)

	strAtom (AtomValue (StrAtom s)) = Just s
	strAtom _ = Nothing

	intAtom (AtomValue (IntAtom i)) = Just i
	intAtom _ = Nothing

	extractReturnValue (ReturnValue rv) = Just rv
	extractReturnValue _ = Nothing