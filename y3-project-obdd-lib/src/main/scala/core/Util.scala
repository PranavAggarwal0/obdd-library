package obdd.core

import cats.effect.{IO, Resource}
import cats.effect.unsafe.IORuntime.global
import scala.io.Source.fromFile

private[obdd] def getVars(expression: BooleanType): Set[Var] =
  expression match
    case _: Boolean            => Set()
    case variable: Var         => Set(variable)
    case And(exps*)            => exps.toSet.flatMap(getVars)
    case Or(exps*)             => exps.toSet.flatMap(getVars)
    case Implication(ex1, ex2) => getVars(ex1) ++ getVars(ex2)
    case Not(ex)               => getVars(ex)

private[obdd] def validateOrdering(order: Seq[Var]): Boolean =
  order.distinct.size == order.size

private[obdd] def validateExpression(exp: BooleanExpression, ordering: Seq[Var]): Boolean =
  getVars(exp).subsetOf(ordering.toSet)

private[obdd] def getFileContents(path: String): String =
  Resource
    .fromAutoCloseable(IO(fromFile(path)))
    .use(f => IO(f.mkString))
    .unsafeRunSync()(global)
