package bayestensor
package backend

import ir._
import utils._

object NumpyCodeGenerator extends (TensorExp => String) {
  val OPT_EINSUM = true
  def apply(e: TensorExp): String = s"""import numpy as np
from time import perf_counter
import opt_einsum
t_start = perf_counter()
${codegen(e)}
t_end = perf_counter()
print(f'latency: {(t_end-t_start)*1000}ms.')
${codegenRes(e)}
"""
  def codegenRes(e: TensorExp): String = e match {
    case TensorLetN(_, TensorSym(name, _)) => s"print('result:', ${name})"
    case _ => ""
  }
  def codegen(e: TensorExp): String = 
    rcr(e).toString
  def rcr(e: TensorExp): Document = e match {
    case TensorConst(vs, TensorDim(n, ds)) => 
      val arr = doc"np.array([${vs.mkString(", ")}])"
      if(n == 1)
        arr
      else
        doc"$arr.reshape((${ds.mkString(",")}))"
    case TensorSelect(e, ids) =>
      doc"np.take(${rcr(e)}, [${ids.mkString(",")}], axis=-1)"
    case TensorNormalize(e) =>
      doc"${rcr(e)} / np.sum(${rcr(e)})"
    case TensorLet(x, e1, e2) =>
      doc"${x.name} = ${rcr(e1)} # ${rcr(e2)}"
    case TensorSym(name, _) =>
      doc"$name"
    case TensorContract(args, einsum, _) =>
      val method = 
        if(OPT_EINSUM) 
          doc"opt_einsum.contract"
        else
          doc"np.einsum"
      doc"np.einsum('$einsum', ${args.map(rcr).mkDocument(", ")})"
  }
}
