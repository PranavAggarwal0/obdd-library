package obdd.core

import org.scalatest.matchers.should._
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.{EitherValues, OptionValues}
import obdd.core.OBDDFixture.*
import obdd.core.OBDD.*
import obdd.parser.ExpressionParser
import scala.collection.immutable.HashMap

class OBDDSpec extends AnyWordSpec with Matchers with EitherValues with OptionValues {

  val ctx = OBDDContext(
    ddCtx = DDContext(
      ordering,
      nodes = HashMap(
        ((Var("true"), -3, -4), -1),
        ((Var("false"), -3, -4), -2)
      )
    ),
    id = 0,
    ITETable = HashMap(),
    ITE_step_Table = HashMap(),
    graphs_vis = Seq()
  )

  "getNode" should {
    "fail if expression has variables not found in ordering" in {
      val (c, n) = buildNode(And(Var("x20"), Var("x21"))).run(ctx).value
      n.left.value shouldBe a[DDException]
    }
  }

  "getDiagram" should {

    var context = ctx

    "build the diagram correctly for the expression" in {
      val (c, n) = buildNode(And(x2, x3)).run(context).value
      context = c
      getDiagram(n.value, c.ddCtx) shouldBe Diagram(
        high = Some(
          Diagram(
            high = Some(trueDiagram),
            node = Node(2, x3, -1, -2),
            low = Some(falseDiagram)
          )
        ),
        node = Node(3, x2, 2, -2),
        low = Some(falseDiagram)
      )
    }

    "return the TRUE diagram for a formula that is always satisfiable" in {
      val (c, n) = buildNode(Or(x1, Not(x1))).run(context).value
      context = c
      getDiagram(n.value, c.ddCtx) shouldBe trueDiagram
    }

    "return the FALSE diagram for a formula that is never satisfiable" in {
      val (c, n) = buildNode(And(x1, Not(x1))).run(context).value
      context = c
      getDiagram(n.value, c.ddCtx) shouldBe falseDiagram
    }

    "reuse existing sub-graphs" in {
      val (c, n) = buildNode(Or(x1, And(x2, x3))).run(context).value
      context = c
      def d = getDiagram(n.value, c.ddCtx)
      d.low.value.node.id shouldBe 3
    }
  }

  "two equivalent formulas should return the same node" in {
    val (c1, n1) = buildNode(Not(Or(x1, x2))).run(ctx).value
    val (c2, n2) = buildNode(And(Not(x1), Not(x2))).run(c1).value
    n1.value shouldEqual n2.value
    getDiagram(n1.value, c2.ddCtx) shouldEqual getDiagram(n2.value, c2.ddCtx)

    val (c3, n3) = buildNode(Not(Or(And(Not(x3), x1), x2))).run(ctx).value
    val (c4, n4) = buildNode(And(Not(And(Not(x3), x1)), Not(x2))).run(c3).value
    n3.value shouldEqual n4.value
    getDiagram(n3.value, c4.ddCtx) shouldEqual getDiagram(n4.value, c4.ddCtx)

    val (c5, n5) = buildNode(And(x4, x5)).run(ctx).value
    val (c6, n6) = buildNode(And(x5, x4)).run(c5).value
    n5.value shouldEqual n6.value
    getDiagram(n5.value, c6.ddCtx) shouldEqual getDiagram(n6.value, c6.ddCtx)
  }

  "size" should {
    "return 2 before any nodes are added" in {
      OBDD.size(ctx) shouldBe 2
    }
    "return correct size as nodes are added" in {
      val (c1, n1) = buildNode(And(x1, x2)).run(ctx).value
      OBDD.size(c1) shouldBe 5
      val (c2, n2) = buildNode(Not(x2)).run(c1).value
      OBDD.size(c2) shouldBe 6
    }
    "return correct size if diagram is specified" in {
      val (c, n) = buildNode(And(x1, x2)).run(ctx).value
      def d      = n.map(getDiagram(_, c.ddCtx))
      d.map(OBDD.size(_)).value shouldBe 4
    }

  }

  def p = new ExpressionParser

  "restrict" should {

    "replace given variable with given value" in {
      def res = for {
        i         <- p.parseInput("(x1 && x2 && x3 && x4) || x5")
        (c1, node) = buildNode(i).run(ctx).value
        n         <- node
        (c2, rn)   = obdd_restrict(n, x2, true).run(c1).value
        r         <- rn
        d          = getDiagram(r, c2.ddCtx)
      } yield d
      res.value.node.label shouldBe x1
      res.value.low.value.node.label shouldBe x5
      res.value.high.value.node.label shouldBe x3
    }

    "return reduced results" in {
      val (c, n) = buildNode(Or(x6, x7)).run(ctx).value
      for {
        node   <- n
        (c1, r) = obdd_restrict(node, x7, true).run(c).value
        rn     <- r
      } yield rn shouldBe TRUE
    }
  }

  "ITE" should {
    "do conjunction correctly with reduced results" in {
      for {
        i1        <- p.parseInput("(x1 || !x3)")
        (c1, n1n)  = buildNode(i1).run(ctx).value
        n1        <- n1n
        i2        <- p.parseInput("(x3 && !x5)")
        (c2, n2n)  = buildNode(i2).run(c1).value
        n2        <- n2n
        (c3, conn) = obdd_ITE(n1, n2, FALSE).run(c2).value
        con       <- conn
        diagram    = getDiagram(con, c3.ddCtx)
      } yield {
        diagram.node.label shouldBe x1
        diagram.low.value shouldBe falseDiagram
        diagram.high.value.node.label shouldBe x3
        diagram.high.value.high.value.node.label shouldBe x5
        diagram.high.value.high.value.high.value shouldBe falseDiagram
        diagram.high.value.high.value.low.value shouldBe trueDiagram
        diagram.high.value.low.value shouldBe falseDiagram
        diagram.low.value shouldBe falseDiagram
      }
    }

    "do disjunction correctly with reduced results" in {
      for {
        i1        <- p.parseInput("(x1 && x3) || (x2 && x3) || x4")
        (c1, n1n)  = buildNode(i1).run(ctx).value
        n1        <- n1n
        d1         = getDiagram(n1, c1.ddCtx)
        i2        <- p.parseInput("(x1 && x3) || x4")
        (c2, n2n)  = buildNode(i2).run(c1).value
        n2        <- n2n
        (c3, disn) = obdd_ITE(n1, TRUE, n2).run(c2).value
        dis       <- disn
        diagram    = getDiagram(dis, c3.ddCtx)
      } yield diagram shouldBe d1
    }

    "do negation correctly with reduced results" in {
      for {
        i         <- p.parseInput("(x1 && x2)")
        (c, node)  = buildNode(i).run(ctx).value
        n         <- node
        (c1, negn) = obdd_ITE(n, FALSE, TRUE).run(c).value
        neg       <- negn
        diagram    = getDiagram(neg, c1.ddCtx)
      } yield {
        diagram.low.value shouldBe trueDiagram
        diagram.high.value.high.value shouldBe falseDiagram
        diagram.high.value.low.value shouldBe trueDiagram
      }
    }
  }

  "var_swap" should {
    "swap adjacent variables correctly" in {
      for {
        i        <- p.parseInput("(x1 && x2)")
        (c, node) = buildNode(i).run(ctx).value
        n        <- node
        c1        = var_swap(0).runS(c).value
        nn       <- getNodeFromID(n.id, c1)
        d         = getDiagram(nn, c1.ddCtx)
      } yield {
        c1.ddCtx.ordering shouldBe Seq(Var("x2"), Var("x1"), Var("x3"), Var("x4"), Var("x5"), Var("x6"), Var("x7"))
        d.node.label.variable shouldBe "x2"
      }
    }
  }
}
