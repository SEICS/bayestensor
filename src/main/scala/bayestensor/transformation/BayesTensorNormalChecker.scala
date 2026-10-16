package bayestensor
package transformation

import ir._
import scala.annotation.tailrec

object BayesTensorNormalChecker extends (Exp => Unit) {
  def isAcceptableBinding(e: Exp): Boolean = e match {
    case Discrete(_) => true
    case MatchCase(_, vs) => vs.forall(x => isAcceptableBinding(x._2))
    case _ => false
  }
  object AcceptableBinding {
    def unapply(e: Exp): Option[Seq[Sym]] = 
      if(isAcceptableBinding(e))
        e match {
          case Discrete(_) => Some(Seq())
          case MatchCase(MatchVars(vs), _) => Some(vs)
          case _ => ???
        }
      else
        None
  }
  def isQueryBinding(e: Exp): Boolean = e match {
    case ObserveNorm(_, _) | SelectNorm(_, _) => true
    case _ => false
  }
  object QueryBinding {
    def unapply(e: Exp): Option[Exp] = 
      if(isQueryBinding(e))
        Some(e)
      else
        None
  }
  type Dag[T] = Map[T, Seq[T]]
  implicit class DagOps[T](dag: Dag[T]) {
    def addEdges(src: T, dsts: Seq[T]): Dag[T] = {
      dag + (src -> (dag.getOrElse(src, Seq()) ++ dsts))
    }
    def reverse: Dag[T] = {
      dag.toSeq.flatMap(kv => kv._2.map(vx => vx -> kv._1)).groupBy(_._1).mapValues(_.map(_._2)).toMap
    }
  }
  type FullQueryCtx = (Map[Sym, Select], Map[Sym, Observe], Dag[Sym])
  case class ObsSel(sym: Sym, values: Seq[Int], isObs: Boolean)
  case class FullQuery(outputVars: Seq[Sym], selections: Seq[Select], observations: Seq[Observe], dag: Dag[Sym]) {
    def obsSels: Seq[ObsSel] = {
      val sels = selections.map(sel => sel match {
        case SelectNorm(s, is) => ObsSel(s, is, false)
        case _ => raise(s"Select $sel not in normal form")
      })
      val obss = observations.map(obs => obs match {
        case ObserveNorm(s, is) => ObsSel(s, is, true)
        case _ => raise(s"Observe $obs not in normal form")
      })
      sels ++ obss
    }
  }
  implicit class ObserveOps(o: Observe) {
    def name: String = o match {
      case ObserveNorm(Sym(name), _) => name
      case _ => raise(s"Observe $o not in normal form (random variable)")
    }
    def value: Int = o match {
      case ObserveNorm(_, Seq(i)) => i
      case _ => raise(s"Observe $o not in normal form (params)")
    }
  }
  @tailrec
  def fullQuery(e: Exp)(ctx: FullQueryCtx): FullQuery = e match {
    case LetBinding(x, AcceptableBinding(ps), e2) => fullQuery(e2)((ctx._1, ctx._2, ctx._3.addEdges(x, ps)))
    case LetBinding(x, o @ ObserveNorm(_, _), e2) => fullQuery(e2)((ctx._1, (ctx._2 + (x -> o)), ctx._3))
    case LetBinding(x, s @ SelectNorm(_, _), e2)  => fullQuery(e2)(((ctx._1 + (x -> s)), ctx._2, ctx._3))
    case LetBinding(x, e1, e2) =>
      raise(s"Let-binding rhs not in normal form: $e1")
    case x @ Sym(_) => 
      if(ctx._1.keySet.nonEmpty)
        raise(s"selection not consistent: ${ctx._1.keySet} not empty")
      if(ctx._2.keySet.nonEmpty)
        raise(s"observe not consistent: ${ctx._2.keySet} not empty")
      FullQuery(Seq(x), Seq(), Seq(), ctx._3)
    case Query(Seq(x), sels, obss) => 
      if(ctx._1.keySet != sels.toSet)
        raise(s"selection not consistent: ${ctx._1.keySet} != ${sels.toSet}")
      if(ctx._2.keySet != obss.toSet)
        raise(s"observe not consistent: ${ctx._2.keySet} != ${obss.toSet}")
      FullQuery(Seq(x), sels.map(ctx._1), obss.map(ctx._2), ctx._3)
    case _ => raise(s"Normal form doesn't support:`$e`")
  }
  def check(e: Exp): Unit = fullQuery(e)(Map(), Map(), Map())
  def apply(e: Exp): Unit = check(e)
}