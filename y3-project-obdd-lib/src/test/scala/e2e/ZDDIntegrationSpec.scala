package obdd.e2e

import org.scalatest.matchers.should._
import org.scalatest.wordspec.AnyWordSpec
import obdd.api.ZDDLib.*
import obdd.core.*
import obdd.e2e.ZDDe2eFixture.*
import org.scalatest.{EitherValues, OptionValues}

class ZDDIntegrationSpec extends AnyWordSpec with Matchers with EitherValues with OptionValues {

  def init_zdd_ctx = newZDD(Seq("x1", "x2", "x3", "x4", "x5", "x6", "x7", "x8"))

  "subset1" should {
    "return the correct node" in {
      val (c1, n1) = set(Set("x1", "x2", "x3")).run(init_zdd_ctx).value
      def d = for {
        node    <- n1
        (c2, n2) = subset1(node, "x2").run(c1).value
        node2   <- n2
      } yield getDiagramByNode(node2, c2)
      d.value shouldBe subset1Diagram
    }
  }

  "subset0" should {
    "return the correct node" in {
      val (c1, n1) = set(Set("x1", "x2", "x3")).run(init_zdd_ctx).value
      def d = for {
        node    <- n1
        (c2, n2) = subset0(node, "x2").run(c1).value
        node2   <- n2
      } yield getDiagramByNode(node2, c2)
      d.value shouldBe falseDiagram
    }
  }

  "set" should {
    "return the node representing the given set of vars" in {
      val (c1, n1) = set(Set("x1", "x2")).run(init_zdd_ctx).value
      def d        = n1.map(getDiagramByNode(_, c1))
      d.value shouldBe setDiagram
    }
  }

  "family" should {
    "return the node representing the given set of sets of vars" in {
      val (c1, n1) = family(Set(Set("x1"), Set("x2"))).run(init_zdd_ctx).value
      def d        = n1.map(getDiagramByNode(_, c1))
      d.value shouldBe familyDiagram
    }
  }

  "change" should {
    "return the correct node" in {
      val (c1, n1) = change(base, "x1").run(init_zdd_ctx).value
      def d1       = n1.map(getDiagramByNode(_, c1))
      d1.value shouldBe changeDiagram
      val (c2, n2) = change(n1.value, "x1").run(c1).value
      n2.value shouldBe base
    }
  }

  "union" should {
    "return the node for the union of the specified nodes" in {
      val (c1, n1) = change(base, "x1").run(init_zdd_ctx).value
      val (c2, n2) = change(base, "x2").run(c1).value
      def d = for {
        node1   <- n1
        node2   <- n2
        (c3, n3) = union(node1, node2).run(c2).value
        node3   <- n3
      } yield getDiagramByNode(node3, c3)
      d.value shouldBe unionDiagram
    }
  }

  "intersection" should {
    "return the node for the intersection of the specified nodes" in {
      val (c1, n1) = set(Set("x1", "x2", "x3")).run(init_zdd_ctx).value
      val (c2, n2) = change(base, "x1").run(c1).value
      def d = for {
        node1   <- n1
        node2   <- n2
        (c3, n3) = intersection(node1, node2).run(c2).value
        node3   <- n3
      } yield getDiagramByNode(node3, c3)
      d.value shouldBe falseDiagram
    }
  }

  "difference" should {
    "return the node for the difference of the specified nodes" in {
      val (c1, n1) = family(Set(Set("x1", "x3"), Set("x2", "x4"))).run(init_zdd_ctx).value
      val (c2, n2) = set(Set("x2", "x4")).run(c1).value
      def d = for {
        node1   <- n1
        node2   <- n2
        (c3, n3) = difference(node1, node2).run(c2).value
        node3   <- n3
      } yield getDiagramByNode(node3, c3)
      d.value shouldBe differenceDiagram
    }
  }

  "mul" should {
    "return the node for the multiplication of the specified nodes" in {
      val (c1, n1) = family(Set(Set("x1", "x2"), Set("x2"), Set("x3"))).run(init_zdd_ctx).value
      val (c2, n2) = family(Set(Set("x1", "x2"))).run(c1).value
      val (c3, n3) = family(Set(Set("x1", "x2"), Set("x1", "x2", "x3"))).run(c2).value
      val (expected, actual) = (for {
        node1   <- n1
        node2   <- n2
        node3   <- n3
        (c4, n4) = mul(node1, node2).run(c3).value
        node4   <- n4
      } yield (node3, node4)).value
      expected shouldBe actual
    }
  }

  "div" should {
    "return the node for the division of the specified nodes" in {
      val (c1, n1) = family(
        Set(
          Set("x1", "x2", "x4"),
          Set("x1", "x2", "x5"),
          Set("x1", "x2", "x7"),
          Set("x3", "x4"),
          Set("x3", "x5"),
          Set("x3", "x8")
        )
      ).run(init_zdd_ctx).value
      val (c2, n2) = family(Set(Set("x1", "x2"), Set("x3"))).run(c1).value
      val (c3, n3) = family(Set(Set("x4"), Set("x5"))).run(c2).value
      val (expected, actual) = (for {
        node1   <- n1
        node2   <- n2
        node3   <- n3
        (c4, n4) = div(node1, node2).run(c3).value
        node4   <- n4
      } yield (node3, node4)).value
      expected shouldBe actual
    }
  }
}
