package obdd.visual

import obdd.core.*
import obdd.core.DD
import sys.process._
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

private[obdd] def nodesVisualise(
  nodes: Set[Node],
  id: Map[Int, String],
  showID: Boolean = false,
  ctx: DDContext
): Set[String] = {
  val dd = new DD
  nodes.map { node =>
    if (node.id == -1 || node.id == -2) Set(get_node_dot(node, false, None))
    else {
      (dd.getHigh(node, ctx), dd.getLow(node, ctx)) match
        case (Right(hi), Right(lo)) =>
          Set(get_node_dot(node, showID, id.get(node.id)) + "\n") ++
            Set(get_node_dot(hi, showID, id.get(hi.id)) + "\n") ++
            Set(get_node_dot(lo, showID, id.get(lo.id)) + "\n") ++
            Set(node.id.toString + " -> " + hi.id.toString + "\n") ++
            Set(node.id.toString + " -> " + lo.id.toString + " [style=dotted] " + "\n")
        case _ => Set(get_node_dot(node, showID, id.get(node.id)) + "\n")
    }
  }.flatten
}

private def get_node_dot(node: Node, showID: Boolean, colour: Option[String]): String =
  colour match
    case Some(col) =>
      if (showID && node.id >= 0) {
        node.id.toString + s" [label=\"${node.label.variable}(${node.id.toString})\"] [color=\"$col\"]"
      } else {
        node.id.toString + s" [label=\"${node.label.variable}\"] [color=\"$col\"]"
      }
    case None =>
      if (showID && node.id >= 0) {
        node.id.toString + s" [label=\"${node.label.variable}(${node.id.toString})\"]"
      } else {
        node.id.toString + s" [label=\"${node.label.variable}\"]"
      }

def nodesToSVG(nodes: Set[Node], id: Map[Int, String], showID: Boolean = false, ctx: DDContext): String =
  val s = "digraph OBDD {\n" + nodesVisualise(nodes, id, showID, ctx).mkString("\n") + "}"
  Files.write(Paths.get("tmp.svg"), s.getBytes(StandardCharsets.UTF_8))
  val result = "dot -Tsvg tmp.svg -o tmp.svg".!
  getFileContents("tmp.svg")
