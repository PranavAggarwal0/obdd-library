package obdd.e2e

import obdd.core.*

object ZDDe2eFixture {

  val trueDiagram = Diagram(high = None, node = TRUE, low = None)

  val falseDiagram = Diagram(high = None, node = FALSE, low = None)

  def subset1Diagram = Diagram(
    Some(
      Diagram(
        Some(trueDiagram),
        Node(1, Var("x3"), -1, -2),
        Some(falseDiagram)
      )
    ),
    Node(4, Var("x1"), 1, -2),
    Some(falseDiagram)
  )

  def subset0Diagram = Diagram(
    Some(trueDiagram),
    Node(4, Var("x1"), 1, -2),
    Some(
      Diagram(
        Some(trueDiagram),
        Node(1, Var("x3"), -1, -2),
        Some(falseDiagram)
      )
    )
  )

  def setDiagram = Diagram(
    Some(
      Diagram(
        Some(trueDiagram),
        Node(1, Var("x2"), -1, -2),
        Some(falseDiagram)
      )
    ),
    Node(2, Var("x1"), 1, -2),
    Some(falseDiagram)
  )

  def familyDiagram = Diagram(
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

  def changeDiagram = Diagram(
    Some(trueDiagram),
    Node(1, Var("x1"), -1, -2),
    Some(falseDiagram)
  )

  def unionDiagram = familyDiagram

  def intersectionDiagram = changeDiagram

  def differenceDiagram = Diagram(
    Some(
      Diagram(
        Some(trueDiagram),
        Node(1, Var("x3"), -1, -2),
        Some(falseDiagram)
      )
    ),
    Node(2, Var("x1"), 1, -2),
    Some(falseDiagram)
  )

}
