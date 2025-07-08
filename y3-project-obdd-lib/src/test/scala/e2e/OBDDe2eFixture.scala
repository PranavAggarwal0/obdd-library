package obdd.e2e

import obdd.core.*

object OBDDe2eFixture {

  val trueDiagram = Diagram(
    high = None,
    node = TRUE,
    low = None
  )

  val falseDiagram = Diagram(
    high = None,
    node = FALSE,
    low = None
  )

  def AndDiagram = Diagram(
    high = Some(
      Diagram(
        high = Some(trueDiagram),
        node = Node(2, Var("x2"), -1, -2),
        low = Some(falseDiagram)
      )
    ),
    node = Node(3, Var("x1"), 2, -2),
    low = Some(falseDiagram)
  )

  def OrDiagram = Diagram(
    high = Some(trueDiagram),
    node = Node(3, Var("x1"), -1, 2),
    low = Some(
      Diagram(
        high = Some(trueDiagram),
        node = Node(2, Var("x2"), -1, -2),
        low = Some(falseDiagram)
      )
    )
  )

  def NotDiagram = Diagram(
    high = Some(falseDiagram),
    node = Node(2, Var("x1"), -2, -1),
    low = Some(trueDiagram)
  )
}
