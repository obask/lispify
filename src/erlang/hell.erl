%%%-------------------------------------------------------------------
%%% @author obask
%%% @copyright (C) 2015, <COMPANY>
%%% @doc
%%%
%%% @end
%%% Created : 02. Jul 2015 4:12 PM
%%%-------------------------------------------------------------------
-module(hell).
-author("obaskakov").

%% API
-export([start/0]).


-define(FILEPATH, "/Users/obaskakov/code.scm").
-define(l_PAREN, "(").
-define(r_PAREN, ")").

%% -record(aTree, {}).
-record(aBranch, {val}).
%% -record(aLeaf, {}).
-record(aString, {val}).


printAST(X) when is_record(X, aBranch) ->
  Tmp = lists:map(fun (Z) -> printAST(Z) end, X#aBranch.val),
  string:join(["["] ++ Tmp ++ ["]"], " ");

printAST(X) when is_record(X, aString) ->
  X#aString.val;

printAST(PPP) ->
  io:format("~p~n", [PPP]),
  erlang:error("Bad AST.", PPP).


restore_arr([H|T], Sep) ->
  case T of
    [] -> [H];
    _Else -> [H, Sep | restore_arr(T, Sep)]
  end.


makeAST(["("|Tail], State) ->
  Tree = makeAST(Tail, []),
  makeAST(Tail, [Tree | State]);

makeAST([")"|_Tail], State) ->
  #aBranch{val = lists:reverse(State)};

makeAST([Atom|Tail], State) ->
  El = #aString{val = Atom},
  makeAST(Tail, [El|State]);

makeAST([], State) ->
%%   erlang:error("Bad Input.").
  #aBranch{val = lists:reverse(State)}.



tokenize(Text) ->
%%   Strings2 = lists:map(fun (X) -> re:replace(X, "\\)", " \\) ", [global, {return, list}]) end, Strings1),
  Strings0 = [Text],
  Strings1 = lists:flatmap(fun (X) -> restore_arr(string:tokens(" " ++ X ++ " ", ")"), ")") end, Strings0),
  Strings2 = lists:flatmap(fun (X) -> restore_arr(string:tokens(" " ++ X ++ " ", "("), "(") end, Strings1),
  Strings3 = lists:flatmap(fun (X) -> string:tokens(X, " \t\n") end, Strings2),
  Strings3.


start() ->
%%   {ok, Bina} = file:read_file(?FILEPATH),
%%   Str = binary_to_list(Bina),
  Str = "(+ 1 2)\n (define ololol)",
  Tokens = tokenize(Str),
  io:fwrite("~p~n", [Tokens]),
  Res = makeAST(Tokens, []),
%%   io:fwrite("~p~n", [Res]).
  Repr = printAST(Res),
  io:fwrite("~p~n", [Repr]).


