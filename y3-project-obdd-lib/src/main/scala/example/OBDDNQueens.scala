package obdd.example

import obdd.api.OBDDLib.*
import obdd.core.*

class OBDDNQueens(n: Int) {

  val ordering = Seq.range(1, (n * n) + 1).map(i => s"x${i.toString}")

  val init_ctx = newOBDD(ordering)

  val (ctx, node) = getNodeByDIMACS(getFileContents(s"src/main/scala/example/resource/coding.cnf")).run(init_ctx).value

  val diagram = node.map(getDiagramByNode(_, ctx))

  def solve = {
    val startTimeMillis = System.currentTimeMillis()
    val d               = diagram
    val endTimeMillis   = System.currentTimeMillis()
    println(s"Time taken for $n queens: ${(endTimeMillis - startTimeMillis) / 1000}")
    println(s"Number of solutions: ${d.map(noOfSols)}")
    println(s"Number of nodes in diagram: ${diagram.map(getDiagramSize(_))}")
  }

  def noOfSols(d: Diagram): Int =
    (d.high, d.low) match
      case (Some(hi), Some(lo)) => noOfSols(hi) + noOfSols(lo)
      case _ =>
        if (d.node.label.variable == "true") 1
        else 0

}
