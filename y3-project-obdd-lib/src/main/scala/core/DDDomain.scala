package obdd.core

import scala.collection.immutable.HashMap

sealed trait BooleanExpression
case class Var(variable: String)                             extends BooleanExpression
case class And(exps: BooleanType*)                           extends BooleanExpression
case class Or(exps: BooleanType*)                            extends BooleanExpression
case class Implication(exp1: BooleanType, exp2: BooleanType) extends BooleanExpression
case class Not(exp: BooleanType)                             extends BooleanExpression

type BooleanType = BooleanExpression | Boolean

case class Node(id: Int, label: Var, high: Int, low: Int)

case class Diagram(high: Option[Diagram], node: Node, low: Option[Diagram])

val TRUE  = Node(-1, Var("true"), -3, -4)
val FALSE = Node(-2, Var("false"), -3, -4)

private def boolToNode(b: Boolean): Node = b match
  case true  => TRUE
  case false => FALSE

sealed trait QUANT
case class EXISTS(vars: Seq[Var], formula: QUANT | BooleanExpression) extends QUANT
case class FORALL(vars: Seq[Var], formula: QUANT | BooleanExpression) extends QUANT

case class DDContext(ordering: Seq[Var], nodes: HashMap[(Var, Int, Int), Int])
case class OBDDContext(
  ddCtx: DDContext,
  id: Int,
  ITETable: HashMap[(Node, Node, Node), Either[DDException, Node]],
  ITE_step_Table: HashMap[(Node, Node, Node), Either[DDException, Node]],
  graphs_vis: Seq[String]
)

case class ZDDContext(
  ddCtx: DDContext,
  id: Int,
  subset1cache: HashMap[(Node, Var), Either[DDException, Node]],
  subset0cache: HashMap[(Node, Var), Either[DDException, Node]],
  changeCache: HashMap[(Node, Var), Either[DDException, Node]],
  unionCache: HashMap[(Node, Node), Either[DDException, Node]],
  intersectionCache: HashMap[(Node, Node), Either[DDException, Node]],
  differenceCache: HashMap[(Node, Node), Either[DDException, Node]],
  mulCache: HashMap[(Node, Node), Either[DDException, Node]],
  divCache: HashMap[(Node, Node), Either[DDException, Node]]
)
