package bayestensor
package frontend

import ir._

object TensorDimChecker extends (TensorExp => TensorExp) {
  def apply(exp: TensorExp): TensorExp = 
    dimCheck(exp)(Map())
  def dimCheck(exp: TensorExp)(ctx: Map[String, TensorDim]): TensorExp = exp match {
    case TensorLet(x, e1, e2) =>
      val te1 = dimCheck(e1)(ctx)
      val tx = TensorSym(x.name, te1.dim)
      val te2 = dimCheck(e2)(ctx + (tx.name -> tx.dim))
      TensorLet(tx, te1, te2)
    case tc @ TensorConst(elems, TensorDim(order, dims)) => 
      if(order != dims.size) 
        raise(s"Order $order doesn't match dims $dims in `$tc`")
      if(elems.size != dims.product) {
        raise(s"Number of elements ${elems.size} doesn't match dims $dims in `$tc`")
      }
      tc
    case TensorSym(name, _) => 
      val dim = ctx.getOrElse(name, raise(s"Symbol `$name` not in the environment: $ctx"))
      TensorSym(name, dim)
    case TensorContract(args, einsum, _) =>
      val targs = args.map(a => dimCheck(a)(ctx))
      val dim = einsum match {
        case "i,ij->j" => TensorDim(1, Seq(targs(1).dim.size))
        case "i,j,ijk->k" => TensorDim(1, Seq(targs(2).dim.size))
        case "i,j,ijk->i" => TensorDim(1, Seq(targs(0).dim.size))
        // FIXME add more cases
      }
      // FIXME add checks
      TensorContract(targs, einsum, dim)
    case TensorSelect(e, filter) =>
      TensorSelect(dimCheck(e)(ctx), filter)
    case TensorNormalize(e) =>
      TensorNormalize(dimCheck(e)(ctx))
  }
}