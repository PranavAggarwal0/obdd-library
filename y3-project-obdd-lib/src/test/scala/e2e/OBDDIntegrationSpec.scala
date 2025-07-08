package obdd.e2e

import org.scalatest.matchers.should._
import org.scalatest.wordspec.AnyWordSpec
import obdd.api.OBDDLib.*
import obdd.core.*
import obdd.e2e.OBDDe2eFixture.*
import org.scalatest.{EitherValues, OptionValues}

class OBDDIntegrationSpec extends AnyWordSpec with Matchers with EitherValues with OptionValues {

  def obdd_init_ctx = newOBDD(Seq("x1", "x2", "x3", "x4", "x5", "x6", "x7"))

  "getNodeByExpression" should {

    "return node for valid expression" in {
      val (c, n) = getNodeByExpression("x1 && x2").run(obdd_init_ctx).value
      c.ddCtx.nodes.keySet.map(_._1).map(_.variable) should contain("x1")
      c.ddCtx.nodes.keySet.map(_._1).map(_.variable) should contain("x2")
      n.value.label.variable shouldBe "x1"
    }

    "fail if expression has variables not found in ordering" in {
      val (c, n) = getNodeByExpression("x20 && x30").run(obdd_init_ctx).value
      c shouldBe obdd_init_ctx
      n.left.value shouldBe a[RuntimeException]
    }

    "fail if expression is not valid" in {
      val (c, n) = getNodeByExpression("x1 |& !x30").run(obdd_init_ctx).value
      c shouldBe obdd_init_ctx
      n.left.value shouldBe a[RuntimeException]
    }

  }

  "getNodeByDIMACS" should {

    "return node for valid dimacs file" in {
      val (c, n) = getNodeByDIMACS(getFileContents("src/test/scala/e2e/resource/dimacs.txt")).run(obdd_init_ctx).value
      c.ddCtx.nodes.keySet.map(_._1).map(_.variable) should contain("x1")
      c.ddCtx.nodes.keySet.map(_._1).map(_.variable) should contain("x2")
      c.ddCtx.nodes.keySet.map(_._1).map(_.variable) should contain("x3")
      n.value.label.variable shouldBe "x1"
    }

    "fail if dimacs is not valid" in {
      val (c, n) = getNodeByDIMACS(getFileContents("src/test/scala/e2e/resource/clauses-same-line.txt"))
        .run(obdd_init_ctx)
        .value
      c shouldBe obdd_init_ctx
      n.left.value shouldBe a[RuntimeException]
    }

  }

  "getNodeByQDIMACS" should {

    "return node for valid qdimacs file" in {
      val (c, n) =
        getNodeByQDIMACS(getFileContents("src/test/scala/e2e/resource/trueqbf.qdimacs")).run(obdd_init_ctx).value
      n.value.label.variable shouldBe "true"
    }

    "fail if qdimacs is not valid" in {
      val (c, n) = getNodeByQDIMACS(getFileContents("src/test/scala/e2e/resource/invalid.qdimacs"))
        .run(obdd_init_ctx)
        .value
      c shouldBe obdd_init_ctx
      n.left.value shouldBe a[RuntimeException]
    }

  }

  "getDiagramByNode" should {
    "return the correct diagram" in {
      val (c, n) = getNodeByExpression("x1 && x2").run(obdd_init_ctx).value
      def d      = n.map(node => getDiagramByNode(node, c))
      d.value shouldBe AndDiagram
    }
  }

  "getGlobalDAGSize" should {
    "return the correct size" in {
      val (c, n) = getNodeByExpression("x1 && x2").run(obdd_init_ctx).value
      def size   = getGlobalDAGSize(c)
      size shouldBe 5
    }
  }

  "getDiagramSize" should {
    "return the correct size" in {
      getDiagramSize(AndDiagram) shouldBe 4
    }
  }

  "restrict" should {
    "correctly replace given variable with the boolean" in {
      val (c, n) = getNodeByExpression("x1 && x2").run(obdd_init_ctx).value
      for {
        node    <- n
        (c2, n2) = restrict(node, "x1", false).run(c).value
        node2   <- n2
      } yield node2.label.variable shouldBe "false"
    }

    "return same node if variable is not present in diagram" in {
      val (c, n) = getNodeByExpression("x1 && x2").run(obdd_init_ctx).value
      for {
        node    <- n
        (c2, n2) = restrict(node, "x20", false).run(c).value
      } yield {
        n2 shouldBe n
        c2 shouldBe c
      }
    }
  }

  "conjunction" should {
    "correctly combine nodes" in {
      val (c1, n1) = getNodeByExpression("x1").run(obdd_init_ctx).value
      val (c2, n2) = getNodeByExpression("x2").run(c1).value
      for {
        node1   <- n1
        node2   <- n2
        (c3, n3) = conjunction(node1, node2).run(c2).value
        node3   <- n3
      } yield getDiagramByNode(node3, c3) shouldBe AndDiagram
    }
  }

  "disjunction" should {
    "correctly combine nodes" in {
      val (c1, n1) = getNodeByExpression("x1").run(obdd_init_ctx).value
      val (c2, n2) = getNodeByExpression("x2").run(c1).value
      for {
        node1   <- n1
        node2   <- n2
        (c3, n3) = disjunction(node1, node2).run(c2).value
        node3   <- n3
      } yield getDiagramByNode(node3, c3) shouldBe OrDiagram
    }
  }

  "negation" should {
    "correctly negate node" in {
      val (c1, n1) = getNodeByExpression("x1").run(obdd_init_ctx).value
      for {
        node1   <- n1
        (c2, n2) = negation(node1).run(c1).value
        node2   <- n2
      } yield getDiagramByNode(node2, c2) shouldBe NotDiagram
    }
  }

  "exists" should {
    "correctly apply quantification algorithm" in {
      val (c1, n1) = getNodeByExpression("x1 && x2").run(obdd_init_ctx).value
      for {
        node1   <- n1
        (c2, n2) = exists(Seq("x1"), Seq(node1)).run(c1).value
        node2   <- n2
      } yield {
        getDiagramByNode(node2, c2).node.label.variable shouldBe "x2"
        getDiagramByNode(node2, c2).high.value.node.label.variable shouldBe "true"
        getDiagramByNode(node2, c2).low.value.node.label.variable shouldBe "false"
      }
    }
  }

  "forAll" should {
    "correctly apply quantification algorithm" in {
      val (c1, n1) = getNodeByExpression("x1 && x2").run(obdd_init_ctx).value
      for {
        node1   <- n1
        (c2, n2) = forAll(Seq("x1"), Seq(node1)).run(c1).value
        node2   <- n2
      } yield getDiagramByNode(node2, c2).node.label.variable shouldBe "false"
    }
  }

  "reorder algorithms" should {
    "reduce size when starting with a non optimal ordering and sifting is used" in {
      def obddctx = newOBDD(Seq("a", "c", "e", "b", "d", "f"))
      val (c, n)  = getNodeByExpression("(a && b) || (c && d) || (e && f)").run(obddctx).value
      for {
        node      <- n
        init_size  = getDiagramSize(getDiagramByNode(node, c))
        c2         = sifting_reorder(node).runS(c).value
        final_size = getDiagramSize(getDiagramByNode(node, c2))
      } yield final_size should be < init_size
    }

    "reduce size when starting with a non optimal ordering and window permutation is used" in {
      def obddctx = newOBDD(Seq("a", "c", "e", "b", "d", "f"))
      val (c, n)  = getNodeByExpression("(a && b) || (c && d) || (e && f)").run(obddctx).value
      for {
        node      <- n
        init_size  = getDiagramSize(getDiagramByNode(node, c))
        c2         = window_permutation_reorder(node).runS(c).value
        final_size = getDiagramSize(getDiagramByNode(node, c2))
      } yield final_size should be < init_size
    }
  }

}
