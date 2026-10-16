package bayestensor
package backend

import ir._
import utils._

object TensorCodeGenerator {
  def codegen(e: TensorExp): String = 
    rcr(e).toString
  def rcr(e: TensorExp): Document = e match {
    case TensorConst(vs, TensorDim(n, ds)) => 
      doc"tensor($n, [${ds.mkString(", ")}], [${vs.mkString(",")}])"
    case TensorSelect(e, ids) =>
      doc"select(${rcr(e)}, [${ids.mkString(", ")}])"
    case TensorLet(x, e1, e2) =>
      doc"let ${x.name} = ${rcr(e1)} # ${rcr(e2)}"
    case TensorSym(name, _) =>
      doc"$name"
    case TensorNormalize(e1) =>
      doc"normalize(${rcr(e1)})"
    case TensorContract(args, einsum, _) =>
      doc"contract('$einsum', ${args.map(rcr).mkDocument(", ")})"
  }
}
