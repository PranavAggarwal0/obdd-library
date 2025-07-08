import obdd.api.ZDDLib.*
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

@main def hello: Unit = {
  def ctx      = newZDD(Seq("x1", "x2", "x3"))
  val (c1, n1) = family(Set(Set("x1", "x2"), Set("x3"))).run(ctx).value
  for {
    node1   <- n1
    (c2, n2) = family(Set(Set("x1", "x2"))).run(c1).value
    node2   <- n2
    (c3, n3) = intersection(node1, node2).run(c2).value
    node3   <- n3
  } yield Files.write(
    Paths.get("intersection.svg"),
    visualiseDiagram(getDiagramByNode(node3, c3)).getBytes(StandardCharsets.UTF_8)
  )
}
