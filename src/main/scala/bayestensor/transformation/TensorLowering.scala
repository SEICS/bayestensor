package bayestensor
package transformation

import ir._

class TensorLowering(val linear: Boolean) extends (Exp => TensorExp) {
  import BayesTensorNormalChecker._
  def apply(e: Exp): TensorExp = {
    val query = fullQuery(e)(Map(), Map(), Map())
    val vars = varOrder(e)
    val cpts = computeCPTs(e)(Map())
    if(linear)
      linearVE(query, vars, cpts)
    else
      variableElimination(query, vars, cpts)
  }
  def computeCPTs(e: Exp)(acc: Map[Sym, TensorConst]): Map[Sym, TensorConst] = e match {
    case LetBinding(x, e1 @ AcceptableBinding(_), e2) =>
      computeCPTs(e2)(acc + (x -> CPT.fromExp(e1)(acc.map(y => y._1 -> y._2.dim))))
    case LetBinding(x, QueryBinding(_), e2) =>
      computeCPTs(e2)(acc)
    case _ =>
      acc
  }
  def varOrder(e: Exp): Seq[Sym] = e match {
    case LetBinding(x, AcceptableBinding(_), e2) =>
      x +: varOrder(e2)
    case LetBinding(x, QueryBinding(_), e2) =>
      varOrder(e2)
    case _ => Seq()
  }
  def linearVE(query: FullQuery, vars: Seq[Sym], cpts: Map[Sym, TensorConst]): TensorExp = {
    assert(query.outputVars.size == 1)
    assert(query.outputVars.head == vars.last)
    assert(query.obsSels.size == 0)
    val tensorVars = vars.map(v => v -> TensorSym(v.name, cpts(v).dim)).toMap
    val bindings = vars.map(v => {
      val pvars = query.dag(v)
      if(pvars.isEmpty) {
        tensorVars(v) -> cpts(v)
      } else {
        val ndag = pvars.map(pv => pv -> Seq()).toMap ++ Map(v -> query.dag(v))
        val tvs = tensorVars + (v -> cpts(v))
        tensorVars(v) -> 
          contract(pvars ++ Seq(v), tvs, v, ndag)
      }
    })
    TensorLetN(bindings, bindings.last._1)
  }
  def contract(vars: Seq[Sym], tensorVars: Map[Sym, TensorExp], outputVar: Sym, dag: Dag[Sym]): TensorExp = {
    // if(linear)
    //   println(s"contract($vars, $tensorVars, $outputVar)")
    val charVars = vars.zipWithIndex.map(vi => vi._1 -> ('a' + vi._2).toChar).toMap
    val n = tensorVars(outputVar).dim.size
    val inArgs = vars.map(v => s"${dag(v).map(charVars).mkString}${charVars(v)}")
    val outArg = charVars(outputVar).toString
    val einsumArg = s"${inArgs.mkString(",")}->$outArg"
    TensorContract(vars.map(tensorVars), einsumArg, TensorDim(1, Seq(n)))
  }
  def variableElimination(query: FullQuery, vars: Seq[Sym], cpts: Map[Sym, TensorConst]): TensorExp = {
    val outputVar = query.outputVars match {
      case Seq(x) => x
      case seq => raise(s"Only single output vars supported, not $seq")
    }
    val tensorVars = vars.map(v => v -> TensorSym(v.name, cpts(v).dim)).toMap
    val obssSels = query.obsSels.groupBy(_.sym).map(x => x._1 -> (x._2 match {
      case Seq(os) => os
      case list => raise(s"Only one obs|sel but found $list")
    })).toMap
    // println(s"obssels: $obssSels")
    val tensorRhs = cpts.map(symTen => {
      val (sym, ten) = symTen
      val rhs = obssSels.get(sym) match {
        case Some(os) => TensorSelect(ten, os.values)
        case None => ten
      }
      sym -> rhs
    })
    val body = contract(vars, tensorVars, outputVar, query.dag)
    val res = vars.foldRight[TensorExp](body)((cur, acc) => TensorLet(tensorVars(cur), tensorRhs(cur), acc))
    // println(backend.NumpyCodeGenerator.codegen(res))
    // println(query.dag)
    // println(query.dag.reverse)
    if(query.observations.nonEmpty && query.selections.nonEmpty)
      raise(s"Cannot handle lowering for both selections and observations")
    def newSym(name: String, e: TensorExp): TensorSym = 
      TensorSym(Sym.fresh(name).name, e.dim)
    def let(name: String)(e1: TensorExp)(f: TensorExp => TensorExp): TensorExp = e1 match {
      case TensorLetN(stmts, lastExpr) =>
        val nsym = newSym(name, lastExpr)
        TensorLetN(stmts :+ (nsym -> lastExpr), f(nsym))
      case lastExpr => 
        val nsym = newSym(name, lastExpr)
        TensorLet(nsym, lastExpr, f(nsym))
    }
    val resNormalized = 
      if(query.observations.nonEmpty) {
        let("normalized")(res)(x => TensorNormalize(x))
      } else
        res
    let("res")(resNormalized)(x => x)
  }
}