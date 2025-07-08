package obdd.visual

import obdd.core.*
import sys.process._
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

private[obdd] def visualise(diagram: Diagram, id: Int, colours: Map[Int, String]): Set[String] =
  (diagram.high, diagram.low) match
    case (Some(hi), Some(lo)) =>
      def f = colours.get(diagram.node.id) match
        case None => Set(diagram.node.id.toString + s" [label=${diagram.node.label.variable}]" + "\n")
        case Some(value) =>
          Set(diagram.node.id.toString + s" [label=${diagram.node.label.variable}] [color=\"$value\"]" + "\n")
      f ++
        Set(hi.node.id.toString + s" [label=${hi.node.label.variable}]" + "\n") ++
        Set(lo.node.id.toString + s" [label=${lo.node.label.variable}]" + "\n") ++
        Set(diagram.node.id.toString + " -> " + hi.node.id.toString + "\n") ++
        Set(diagram.node.id.toString + " -> " + lo.node.id.toString + " [style=dotted] " + "\n") ++
        visualise(hi, id, colours) ++ visualise(lo, id, colours)
    case _ => Set(diagram.node.id.toString + s" [label=${diagram.node.label.variable}]" + "\n")

private[obdd] def toSVG(diagram: Diagram, colours: Map[Int, String] = Map.empty): String =
  val s = "digraph OBDD {\n" + visualise(diagram, diagram.node.id, colours).mkString("\n") + "}"
  Files.write(Paths.get("tmp.svg"), s.getBytes(StandardCharsets.UTF_8))
  val result = "dot -Tsvg tmp.svg -o tmp.svg".!
  getFileContents("tmp.svg")
