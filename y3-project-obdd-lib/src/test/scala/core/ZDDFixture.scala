package obdd.core

import obdd.core.Var

object ZDDFixture {

  def a = Var("a")
  def b = Var("b")
  def c = Var("c")
  def d = Var("d")
  def e = Var("e")
  def f = Var("f")
  def g = Var("g")
  def h = Var("h")

  def ordering = Seq(a, b, c, d, e, f, g, h, Var("true"), Var("false"))

}
