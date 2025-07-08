## obdd4s: A Scala library for managing OBDDs

### Getting Started

Add the following to your build.sbt file:
`libraryDependencies += "obddlib" %% "obdd" % "0.1.0-SNAPSHOT"`

### Example

```
import obdd.api.OBDDLib.*

@main def hello: Unit = {
  def ctx     = newOBDD(Seq("x1", "x2", "x3", "x4"))
  val (c1, n1) = getNodeByExpression("!x2").run(ctx).value
  for {
    node1 <- n1
    (c2, n2)    = negation(node1).run(c1).value
    node2 <- n2
  } yield (println(visualiseDiagram(getDiagramByNode(node2, c2))))
}

```

This code does the following:
- Creates an OBDD with the specified ordering of variables
- Creates a node for "Not(x2)"
- Computes another node for its negation
- Gets the sub-DAG for the negated node
- Prints the sub-DAG to std out.
