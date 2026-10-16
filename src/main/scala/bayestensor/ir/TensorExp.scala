package bayestensor
package ir

import scala.annotation.tailrec

sealed trait TensorExp {
  def dim: TensorDim
}
case class TensorConst(elems: Seq[Double], dim: TensorDim) extends TensorExp
case class TensorContract(args: Seq[TensorExp], einsum: String, dim: TensorDim) extends TensorExp
case class TensorSym(name: String, dim: TensorDim) extends TensorExp
case class TensorLet(sym: TensorSym, e1: TensorExp, e2: TensorExp) extends TensorExp {
  def dim: TensorDim = e2.dim
}
case class TensorSelect(e: TensorExp, filter: Seq[Int]) extends TensorExp {
  def dim: TensorDim = {
    val d = e.dim
    TensorDim(d.order, d.dims.dropRight(1) ++ Seq(filter.size))
  }
}
case class TensorNormalize(e: TensorExp) extends TensorExp {
  def dim: TensorDim = e.dim
}

case class TensorDim(order: Int, dims: Seq[Int]) {
  def size: Int = dims.last
}

object TensorLetN {
  def apply(bindings: Seq[(TensorSym, TensorExp)], body: TensorExp): TensorExp =
    bindings.foldRight(body)((cur, acc) => TensorLet(cur._1, cur._2, acc))
  type Res = Option[(Seq[(TensorSym, TensorExp)], TensorExp)]
  def unapply(exp: TensorExp): Res =
    rec(exp, None)

  @tailrec def rec(exp: TensorExp, res: Res): Res = exp match {
    case TensorLet(x, e1, e2) => rec(e2, res match {
      case Some((seq, b)) => Some((seq :+ (x -> e1)) -> e2)
      case None => Some(Seq(x -> e1) -> e2)
    })
    case _ => res
  }
}