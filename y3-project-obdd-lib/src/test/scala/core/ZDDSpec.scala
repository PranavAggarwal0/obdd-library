package obdd.core

import ZDDFixture.*
import obdd.core.ZDD.*
import obdd.core.ZDDFixture
import org.scalatest.matchers.should._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, OptionValues}
import scala.collection.immutable.HashMap

class ZDDSpec extends AnyWordSpec with Matchers with EitherValues with OptionValues {

  val ctx = ZDDContext(
    ddCtx = DDContext(
      ordering = ordering,
      nodes = HashMap(
        ((Var("true"), -3, -4), -1),
        ((Var("false"), -3, -4), -2)
      )
    ),
    id = 0,
    subset1cache = HashMap(),
    subset0cache = HashMap(),
    changeCache = HashMap(),
    unionCache = HashMap(),
    intersectionCache = HashMap(),
    differenceCache = HashMap(),
    mulCache = HashMap(),
    divCache = HashMap()
  )

  val (c1, a_node) = zdd_change(base(), ZDDFixture.a).run(ctx).value
  val (c2, b_node) = zdd_change(base(), b).run(c1).value

  "change" should {
    "work correctly" in {
      a_node.value.label shouldBe Var("a")
      b_node.value.label shouldBe Var("b")
    }
  }

  val (c1u, u1) = (for {
    a <- a_node
    b <- b_node
  } yield zdd_union(a, b)).value.run(c2).value

  val (c2u, u2) = (for {
    a <- u1
    b  = base()
  } yield zdd_union(a, b)).value.run(c1u).value

  "union" should {
    "work correctly" in {
      val d1 = getDiagram(u1.value, c2u.ddCtx)
      d1.node.label.variable shouldBe "a"
      d1.high.value.node.label.variable shouldBe "true"
      d1.low.value.node.label.variable shouldBe "b"
      d1.low.value.high.value.node.label.variable shouldBe "true"
      d1.low.value.low.value.node.label.variable shouldBe "false"

      val d2 = getDiagram(u2.value, c2u.ddCtx)
      d2.node.label.variable shouldBe "a"
      d2.high.value.node.label.variable shouldBe "true"
      d2.low.value.node.label.variable shouldBe "b"
      d2.low.value.high.value.node.label.variable shouldBe "true"
      d2.low.value.low.value.node.label.variable shouldBe "true"
    }
  }

  val (c3d, diff) = (for {
    b <- b_node
    u <- u2
  } yield zdd_difference(u, b)).value.run(c2u).value

  "difference" should {
    "work correctly" in {
      val di = getDiagram(diff.value, c3d.ddCtx)
      di.node.label.variable shouldBe "a"
      di.high.value.node.label.variable shouldBe "true"
      di.low.value.node.label.variable shouldBe "true"
    }
  }

  "div" should {
    "work correctly" in {
      val (cdiv, n) = zdd_family(Set(Set(ZDDFixture.a, b, c), Set(b, c), Set(ZDDFixture.a, c))).run(c3d).value
      for {
        n1        <- n
        (c2, n2n)  = zdd_family(Set(Set(b, c))).run(cdiv).value
        n2        <- n2n
        (c3, divv) = zdd_div(n1, n2).run(c2).value
        div       <- divv
        d          = getDiagram(div, c3.ddCtx)
      } yield {
        d.node.label.variable shouldBe "a"
        d.high.value.node.label.variable shouldBe "true"
        d.low.value.node.label.variable shouldBe "true"
      }
    }
  }

}
