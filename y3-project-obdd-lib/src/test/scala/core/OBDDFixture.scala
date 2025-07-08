package obdd.core

object OBDDFixture {

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

  val ordering = Seq(Var("x1"), Var("x2"), Var("x3"), Var("x4"), Var("x5"), Var("x6"), Var("x7"))

  val x1 = Var("x1")
  val x2 = Var("x2")
  val x3 = Var("x3")
  val x4 = Var("x4")
  val x5 = Var("x5")
  val x6 = Var("x6")
  val x7 = Var("x7")

}
