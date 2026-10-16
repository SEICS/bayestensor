package bayestensor
package frontend

import ir._
import fastparse._, NoWhitespace._, CharPredicates._


object TensorParser {
  def keywords[_: P] = P (
    StringIn("tensor", "select", "let", "in", "contract", "normalize") ~
      !idRest
  )  

  def singleComment[_: P] = P( "//" ~/ (!newline ~ AnyChar).rep ~/ newline)
  def multiComment[_: P]  = P( "/*" ~/ (!"*/" ~ AnyChar).rep ~/ "*/")
  def comment[_: P]       = P( multiComment | singleComment )

  def newline[_: P]       = P( "\n" | "\r\n" | "\r" )
  def whitespace[_: P]    = P( " " | "\t" | newline)
  def spaceToken[_: P]    = P( comment | whitespace.rep(1) )
  def space[_: P]         = P( spaceToken.rep )
  def digits[_: P]        = P( CharsWhileIn("0-9") )
  def exponent[_: P]      = P( CharIn("eE") ~ CharIn("+\\-").? ~ digits )
  def fractional[_: P]    = P( "." ~ digits )
  def integral[_: P]      = P( "0" | CharIn("1-9")  ~ digits.? )
  def stringChars(c: Char) = c != '\'' && c != '\\'
  def hexDigit[_: P]      = P( CharIn("0-9a-fA-F") )
  def unicodeEscape[_: P] = P( "u" ~ hexDigit ~ hexDigit ~ hexDigit ~ hexDigit )
  def escape[_: P]        = P( "\\" ~ (CharIn("\"/\\\\bfnrt") | unicodeEscape) )
  def alpha[_: P]         = P( CharPred(isLetter) )
  def idRest[_: P]: P[Char] = P( CharPred(c => isLetter(c) | isDigit(c) | c == '_').! ).map(_(0))

  def int[_: P] = P(  CharIn("+\\-").? ~ integral ).!.map(
    x => x.toInt
  )

  def double[_: P] = P(  CharIn("+\\-").? ~ integral ~ fractional ~ exponent.? ).!.map(
    x => x.toDouble
  )

  def number[_: P] = P(  double | int ).map(x => x match {
    case x: Int => x.toDouble
    case x: Double => x
  })

  def strChars[_: P] = P( CharsWhile(stringChars) )

  def string[_: P] =
    P( space ~ "\'" ~/ (strChars | escape).rep.! ~ "\'")

  def letBinding[_: P]: P[TensorLet] = P( "let" ~/ tensorVar ~/ "=" ~/ expr ~/ "in".? ~/ expr).map(x => {
    TensorLet(x._1, x._2, x._3)
  })
  def const[_: P]: P[TensorConst] = P( "tensor" ~/ "(" ~/ int ~/ space ~/ "," ~/ space ~/ intParams ~/ space ~/ "," ~/ space ~/ numParams ~/ space ~/ ")" ).map(x =>
    TensorConst(x._3, TensorDim(x._1, x._2))
  )
  def contract[_: P]: P[TensorContract] = P( "contract" ~/ "(" ~/ string ~/ space ~/ "," ~/ space ~/ (expr).rep(sep=","./) ~/ space ~/ ")" ).map(x =>
    TensorContract(x._2, x._1, EMPTY_DIM)
  )
  def select[_: P]: P[TensorSelect] = P( "select" ~/ "(" ~/ expr ~/ space ~/ "," ~/ space ~/ intParams ~/ space ~/ ")" ).map(x =>
    TensorSelect(x._1, x._2)
  )
  def normalize[_: P]: P[TensorNormalize] = P( "normalize" ~/ "(" ~/ expr ~/ ")" ).map(x =>
    TensorNormalize(x)
  )
  def variable[_: P]: P[String] = P( space ~ !keywords ~ ((alpha | "_" | "$") ~ idRest.rep).! ~ space )
  def tensorVar[_: P]: P[TensorSym] = P( variable ).map(x => varNoDim(x))
  def intParams[_: P] = 
      P( ("[" ~/ (space ~/ int ~/ space).rep(sep=","./) ~ space ~/ "]") )
  def numParams[_: P] = 
      P( ("[" ~/ (space ~/ number ~/ space).rep(sep=","./) ~ space ~/ "]") )

  def factor[_: P]: P[TensorExp] = 
    P(space ~ (const | letBinding | contract | select | normalize | tensorVar | parens) ~ space)

  def expr[_: P]: P[TensorExp] = P( factor )
  def top[_: P]: P[TensorExp] = P(expr ~ End)

  def parens[_: P]: P[TensorExp] = P( "(" ~/ expr ~/ ")")

  val EMPTY_DIM = TensorDim(0, Seq())

  def varNoDim(name: String) = TensorSym(name, EMPTY_DIM)

  def apply(str: String): TensorExp = {
    val value = parse(str, top(_)) match {
      case Parsed.Success(value, _) => value
      case f @ Parsed.Failure(msg, p, extra) => raise(s"Parse failed `$f` for tensor`$str`")
    }
    TensorDimChecker(value)
  }
}
