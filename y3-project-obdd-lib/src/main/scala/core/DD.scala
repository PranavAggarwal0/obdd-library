package obdd.core

class DD {

  private[obdd] def getDiagram(node: Node, ctx: DDContext): Diagram =
    Diagram(
      high = getHigh(node, ctx).map(getDiagram(_, ctx)).toOption,
      node = node,
      low = getLow(node, ctx).map(getDiagram(_, ctx)).toOption
    )

  private[obdd] def getglobalDAG(ctx: DDContext): Set[Node] =
    ctx.nodes.map(entryToNode).toSet

  private[obdd] def isBigger(v1: Var, v2: Var, ctx: DDContext): Boolean =
    if (v1.variable == "false" || v1.variable == "true") false
    else {
      val o = ctx.ordering.filter(o => o == v1 || o == v2)
      (o.head == v1) && (o.size == 2)
    }

  def getHigh(node: Node, ctx: DDContext): Either[DDException, Node] =
    ctx.nodes.find(_._2 == node.high).map(entryToNode).toRight(DDException("high node not found"))

  def getLow(node: Node, ctx: DDContext): Either[DDException, Node] =
    ctx.nodes.find(_._2 == node.low).map(entryToNode).toRight(DDException("low node not found"))

}

class DDException(msg: String) extends RuntimeException(msg)
