package obdd.api

import obdd.core.*
import obdd.visual.*
import scala.collection.immutable.HashMap
import cats.data.State
import obdd.core.ZDD.*

object ZDDLib {

  def newZDD(ordering: Seq[String]): ZDDContext =
    ZDDContext(
      ddCtx = DDContext(
        ordering = ordering.map(Var(_)) ++ Seq(Var("true"), Var("false")),
        nodes = HashMap(
          ((Var("true"), -3, -4), -1),
          ((Var("false"), -3, -4), -2)
        )
      ),
      id = 0,
      subset1cache = HashMap(),
      subset0cache = HashMap(),
      changeCache = HashMap(),
      unionCache = HashMap(),
      intersectionCache = HashMap(),
      differenceCache = HashMap(),
      mulCache = HashMap(),
      divCache = HashMap()
    )

  def empty: Node = ZDD.empty()

  def base: Node = ZDD.base()

  def subset1(node: Node, v: String): State[ZDDContext, Either[DDException, Node]] = zdd_subset1(node, Var(v))

  def subset0(node: Node, v: String): State[ZDDContext, Either[DDException, Node]] = zdd_subset0(node, Var(v))

  def set(vars: Set[String]): State[ZDDContext, Either[DDException, Node]] = zdd_set(vars.map(Var(_)))

  def family(sets: Set[Set[String]]): State[ZDDContext, Either[DDException, Node]] = zdd_family(sets.map(_.map(Var(_))))

  def change(node: Node, v: String): State[ZDDContext, Either[DDException, Node]] = zdd_change(node, Var(v))

  def union(node1: Node, node2: Node): State[ZDDContext, Either[DDException, Node]] = zdd_union(node1, node2)

  def intersection(node1: Node, node2: Node): State[ZDDContext, Either[DDException, Node]] =
    zdd_intersection(node1, node2)

  def difference(node1: Node, node2: Node): State[ZDDContext, Either[DDException, Node]] = zdd_difference(node1, node2)

  def mul(node1: Node, node2: Node): State[ZDDContext, Either[DDException, Node]] = zdd_mul(node1, node2)

  def div(node1: Node, node2: Node): State[ZDDContext, Either[DDException, Node]] = zdd_div(node1, node2)

  def remainder(node1: Node, node2: Node): State[ZDDContext, Either[DDException, Node]] = zdd_remainder(node1, node2)

  def getDiagramByNode(node: Node, ctx: ZDDContext): Diagram = getDiagram(node, ctx.ddCtx)

  def get_global_dag(ctx: ZDDContext): Set[Node] = getglobalDAG(ctx.ddCtx)

  def visualiseDiagram(diagram: Diagram): String = toSVG(diagram)

}
