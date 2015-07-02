/**
 * Created by oleg on 30/03/14.

 */

import scala.io.Source
import scala.collection.mutable

import Lisp._

/**
 * Created by oleg on 06/04/14.
 */
object Parser {


  sealed abstract class LToken
  case object LeftParenthesis extends LToken
  case object RightParenthesis extends LToken
  case object LQuote extends LToken
  case object LUnquote extends LToken
  case class LInt(value: Int) extends LToken
  case class LDouble(value: Double) extends LToken
  case class LAtom(value: String) extends LToken


  def tokenize(ss: String): Array[LToken] = {
    val ss1 = ss.replace("(", " ( ").replace(")", " ) ").replace("`", " ` ").replace(",", " , ").replace("~", " ~ ")
    val tokens = Array(ss1) flatMap (_ split " ") flatMap (_ split "\n") filter (!_.isEmpty)
    for (ss <- tokens)
    yield ss match {
      case "(" => LeftParenthesis
      case ")" => RightParenthesis
      case "`" => LQuote
      case "," | "~" => LUnquote
      case x => toSomeInt(x).map(LInt).getOrElse(
        toSomeDouble(x).map(LDouble).getOrElse(
          LAtom(x)))
    }
  }


  def makeFullAST(tokens: List[LToken]): List[CodeTree] = {
    val (branch, rest) = makeAST(tokens, List())
    branch.value
  }


  def makeAST(tokens: List[LToken], state: List[CodeTree]): (ABranch, List[LToken]) = {
    if (tokens.isEmpty) {
      (ABranch(state.reverse), tokens)
    } else {
      tokens.head match {
        case LeftParenthesis =>
          val tt = makeAST(tokens.tail, List())
          makeAST(tt._2, tt._1 :: state)
        case LQuote => {
          val (ABranch(tt), rest) = makeAST(tokens.tail, List())
          val tmp = ABranch(state.reverse ::: ABranch(List(AString("quote"), tt.head)) :: tt.tail)
          (tmp, rest)
        }
        case LUnquote => {
          val (ABranch(tt), rest) = makeAST(tokens.tail, List())
          val tmp = ABranch(state.reverse ::: ABranch(List(AString("unquote"), tt.head)) :: tt.tail)
          (tmp, rest)
        }
        case LInt(x) => makeAST(tokens.tail, ANumber(x) :: state)
        case LDouble(x) => makeAST(tokens.tail, ADouble(x) :: state)
        case LAtom("#f") => makeAST(tokens.tail, AFalse :: state)
        case LAtom("null") => makeAST(tokens.tail, ANull :: state)
        case LAtom(x) => makeAST(tokens.tail, AString(x) :: state)
        case RightParenthesis => (ABranch(state.reverse), tokens.tail)
      }
    }
  }

}

object Lisp {


val myLispProgram = Source.fromFile("/Users/oleg/IdeaProjects/lisa/src/prog1.scm").getLines().mkString("\n")


  sealed abstract class CodeTree
  case class ABranch(value: List[CodeTree]) extends CodeTree {
    override def toString = {
       "[LP " + (value map {_.toString} mkString " ") + " RP]"
    }
  }

  object ALeaf {
    def unapply(ct: CodeTree): Option[ALeaf] =
      ct match {
        case leaf: ALeaf => Some(leaf)
        case _ => None
      }
  }

  sealed abstract class ALeaf extends CodeTree
  case class AString(value: String) extends ALeaf {
    override def toString = value.toString
  }

  case class ANumber(value: Int) extends ALeaf {
    override def toString = value.toString
  }

  case class ADouble(value: Double) extends ALeaf {
    override def toString = value.toString
  }
  case class ALambda(args: List[String], code: CodeTree) extends ALeaf
  case class AQuoted(code: CodeTree) extends ALeaf
  case object AFalse extends ALeaf
  case object ATrue extends ALeaf
  case object AUnit extends ALeaf
  case object ANull extends ALeaf
  case class ACons(head: ALeaf, tail: ALeaf) extends ALeaf






  def toSomeInt(ss: String):Option[Int] = {
    try {
      Some(ss.toInt)
    } catch {
      case e:Exception => None
    }
  }

  def toSomeDouble(ss: String):Option[Double] = {
    try {
      Some(ss.toDouble)
    } catch {
      case e:Exception => None
    }
  }






  def sumFun(args: List[ALeaf]): Double = {
    if (args.isEmpty) 0.0
    else
      args.head match {
        case ANumber(x) => x.toDouble + sumFun(args.tail)
        case ADouble(x) => x + sumFun(args.tail)
      }
  }

  def prodFun(args: List[ALeaf]): Double = {
    if (args.isEmpty) 1.0
    else
      args.head match {
        case ANumber(x) => x.toDouble * prodFun(args.tail)
        case ADouble(x) => x * sumFun(args.tail)
      }
  }


  def diffFun(args: List[ALeaf]): Double = {
    if (args.isEmpty) 0.0
    else
      args.head match {
        case ANumber(x) => x.toDouble - sumFun(args.tail)
        case ADouble(x) => x - sumFun(args.tail)
      }
  }

  var MACROS_LIST = List("if", "define", "lambda", "defmacro", "quote", "unquote")

  type State = mutable.HashMap[String, ALeaf]



  def quoteAST(data: CodeTree, st: State): CodeTree = {
    data match {
      case ABranch(AString("unquote") :: body :: Nil) => evalAST(body, st) match {case AQuoted(x) => x}
      case ABranch(ll) => ABranch(ll.map(quoteAST(_, st)))
      case xx: ALeaf => xx
    }
  }


  def evalLambda[A <: CodeTree](vars: List[String], args: List[A], code: CodeTree, st: State): ALeaf = {
//    println("applyLambda", args)
    val st1 = st.clone
    for ((a, b) <- vars zip args) {
      st1(a) = b match {
        case x: ABranch => AQuoted(x)
        case x: ALeaf => x
      }
    }
    evalAST(code, st1)
  }


  def applyAST(fun: CodeTree, args: List[ALeaf], st: State): ALeaf = {
//    println("applyAST", fun, args)
    fun match {
      case AString("+") => ADouble(sumFun(args))
      case AString("*") => ADouble(prodFun(args))
      case AString("-") => ADouble(diffFun(args))
      case AString("cons") => ACons(args(0), args(1))
      case AString("car") => args(0) match { case ACons(x, _) => x }
      case AString("cdr") => args(0) match { case ACons(_, x) => x }
      case AString(">") => if (args(0).asInstanceOf[ADouble].value > args(1).asInstanceOf[ADouble].value)
                                ATrue
                           else AFalse
      case AString("==") => if (args(0) == args(1)) ATrue
                            else AFalse
      case ALambda(vars, code) => evalLambda[ALeaf](vars, args, code, st)
      case AString(ss) if st.contains(ss) => st(ss) match { case x: ALeaf => x}
    }
  }



  def macroAST(fun: CodeTree, args: List[CodeTree], st: State): ALeaf = {
//    println("macroAST", fun, args)
//    println(st)
    fun match {
      case AString("if") => if (evalAST(args.head, st) == AFalse) {
        evalAST(args(2), st)
      } else {
        evalAST(args(1), st)
      }
      case AString("define") => args.head match {
        case AString(ss) => st(ss) = evalAST(args(1), st); AUnit
      }
      case AString("defmacro") => args.head match {
          case AString(ss) =>
            MACROS_LIST = ss :: MACROS_LIST
            val tt = evalAST(args(1), st)
            println("tt = ", tt)
            st(ss) = tt
            AUnit
        }
      case AString("lambda") => {
        val vars = args(0) match {
          case ABranch(data) => data.map {
            case AString(ss) => ss
          }
        }
        ALambda(vars, args(1))
      }
      case AString("quote") => {
        val tree = quoteAST(args.head, st)
        evalAST(tree, st)
      }
      case ALambda(vars, code) => evalLambda[CodeTree](vars, args, code, st)
      case AString(x) => st.getOrElse(x, AString(x))

    }
  }


  def evalAST(tree: CodeTree, st: State): ALeaf = {
    tree match {
      case ABranch(xs) => {
        val fun = evalAST(xs.head, st)
        xs.head match {
          case AString(s1) =>
            if (MACROS_LIST contains s1)
              macroAST(fun, xs.tail, st)
            else
              applyAST(fun, xs.tail.map(evalAST(_, st)), st)
          case _: ABranch =>
            applyAST(fun, xs.tail.map(evalAST(_, st)), st)
        }
      }
      case AString(x) => st.getOrElse(x, AString(x))
      case code: AQuoted => code
      case x: ALeaf => x
    }
  }


  def main(args: Array[String]) {
    val state = new State
    val tokens = Parser.tokenize(myLispProgram)
    val tree = Parser.makeFullAST(tokens toList)
    for (t1 <- tree) {
      println(t1)
      println(evalAST(t1, state))
    }

  }




}



