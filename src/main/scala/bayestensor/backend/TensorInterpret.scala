package bayestensor
package backend

import ir._
import utils._

object TensorInterpreter {
  def interpret(e: TensorExp)(ctx: Map[TensorSym, TensorConst]): TensorConst = e match {
    case v @ TensorConst(vs, TensorDim(n, ds)) => v
    case TensorSelect(e1, ids) => 
      val v = interpret(e1)(ctx)
      val dv = v.dim
      val dr = e.dim
      val N = dv.size
      val vs = v.elems.zipWithIndex.filter(xi => ids.contains(xi._2 % N)).map(_._1)
      // println(s"selected: $vs")
      TensorConst(vs, dr)
    case TensorLet(x, e1, e2) =>
      val v1 = interpret(e1)(ctx)
      interpret(e2)(ctx + (x -> v1))
    case v @ TensorSym(_, _) =>
      ctx(v)
    case TensorContract(args, einsum, dim) =>
      val vs = args.map(a => interpret(a)(ctx))
      val npCode = NumpyCodeGenerator.codegen(TensorContract(vs, einsum, dim))
      // println(npCode)
      if(dim.order != 1) 
        raise(s"Tensor contraction doesn't support output order `${dim.order}`")
      val output = utils.PythonProxy.runPythonCode(s"import numpy as np; print($npCode)")
      val toutput = output.trim()
      try {
        
        val elems = toutput.substring(1, toutput.size - 1).trim.split(" ").filter(_.trim().size != 0).toSeq.map(_.toDouble)
        TensorConst(elems, TensorDim(1, Seq(elems.size)))
      } catch {
        case ex => raise(s"Error in parsing `$toutput`")
      }
    case TensorNormalize(e) =>
      val v = interpret(e)(ctx)
      val sum = v.elems.sum
      val elems = v.elems.map(x => x / sum)
      TensorConst(elems, v.dim)
  }
}
