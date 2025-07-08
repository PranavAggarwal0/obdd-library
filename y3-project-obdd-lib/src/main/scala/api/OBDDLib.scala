package obdd.api

import obdd.core.OBDD.*
import obdd.core.*
import obdd.parser.*
import obdd.visual.*
import scala.collection.immutable.HashMap
import cats.data.State

object OBDDLib {

  def newOBDD(ordering: Seq[String]): OBDDContext =
    OBDDContext(
      ddCtx = DDContext(
        ordering = ordering.map(Var(_)),
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

  def getNodeByExpression(expression: String): State[OBDDContext, Either[RuntimeException, Node]] =
    State { ctx =>
      (for {
        exp   <- expressionParser.parseInput(expression)
        (c, n) = buildNode(exp).run(ctx).value
        node  <- n
      } yield (c, node)) match
        case Left(value)    => (ctx, Left(value))
        case Right((ct, v)) => (ct, Right(v))
    }

  /**
   * Same as getNodeByExpression, but here the input should be a string in
   * DIMACS format.
   */
  def getNodeByDIMACS(dimacs: String): State[OBDDContext, Either[RuntimeException, Node]] =
    State { ctx =>
      (for {
        exp   <- dimacsParser.parseString(dimacs)
        (c, n) = buildNode(exp).run(ctx).value
        node  <- n
      } yield (c, node)) match
        case Left(value)    => (ctx, Left(value))
        case Right((ct, v)) => (ct, Right(v))
    }

  def getNodeByQDIMACS(qdimacs: String): State[OBDDContext, Either[RuntimeException, Node]] =
    State { ctx =>
      (for {
        quant <- qdimacsParser.parseString(qdimacs)
        (c, n) = solve_qbf(quant).run(ctx).value
        node  <- n
      } yield (c, node)) match
        case Left(value)    => (ctx, Left(value))
        case Right((ct, v)) => (ct, Right(v))
    }

  def getNodeByID(id: Int, ctx: OBDDContext): Either[RuntimeException, Node] = getNodeFromID(id, ctx)

  def getDiagramByNode(node: Node, ctx: OBDDContext): Diagram = getDiagram(node, ctx.ddCtx)

  /**
   * Returns the total number of nodes in the global DAG.
   */
  def getGlobalDAGSize(ctx: OBDDContext): Int = size(ctx)

  /**
   * Returns a set of all the nodes present in the global DAG
   *
   * @return
   *   The set of all nodes
   */
  def getGlobalDAG(ctx: OBDDContext): Set[Node] = getglobalDAG(ctx.ddCtx)

  /**
   * Returns the total number of nodes in the specified Diagram.
   *
   * @return
   *   The size of the diagram.
   */
  def getDiagramSize(diagram: Diagram): Int = size(diagram)

  /**
   * Returns a string in SVG format that should be written to a file, the SVG
   * returned is a visualisation of the specified Diagram
   *
   * @param diagram
   *   The diagram that should be visualised.
   * @return
   */
  def visualiseDiagram(diagram: Diagram, colours: Map[Int, String] = Map.empty): String = toSVG(diagram, colours)

  def visualise_global_DAG(ids: Map[Int, String] = Map(), showID: Boolean = false, ctx: OBDDContext) =
    visualiseGlobal(ids, showID, ctx)

  def restrict(node: Node, variable: String, value: Boolean): State[OBDDContext, Either[RuntimeException, Node]] =
    State { ctx =>
      obdd_restrict(node, Var(variable), value).run(ctx).value
    }

  def conjunction(node1: Node, node2: Node): State[OBDDContext, Either[RuntimeException, Node]] =
    State { ctx =>
      obdd_ITE(node1, node2, FALSE).run(ctx).value
    }

  def conjunction_step(node1: Node, node2: Node): State[OBDDContext, Seq[String]] =
    State { ctx =>
      IteStep(node1, node2, FALSE, Map(node1.id -> "blue", node2.id -> "blue")).run(ctx).value
    }

  def disjunction(node1: Node, node2: Node): State[OBDDContext, Either[RuntimeException, Node]] =
    State { ctx =>
      obdd_ITE(node1, TRUE, node2).run(ctx).value
    }

  def disjunction_step(node1: Node, node2: Node): State[OBDDContext, Seq[String]] =
    State { ctx =>
      IteStep(node1, TRUE, node2, Map(node1.id -> "blue", node2.id -> "blue")).run(ctx).value
    }

  def negation(node: Node): State[OBDDContext, Either[RuntimeException, Node]] =
    State { ctx =>
      obdd_ITE(node, FALSE, TRUE).run(ctx).value
    }

  def negation_step(node: Node): State[OBDDContext, Seq[String]] =
    State { ctx =>
      IteStep(node, FALSE, TRUE, Map(node.id -> "blue")).run(ctx).value
    }

  def exists(vars: Seq[String], nodes: Seq[Node]): State[OBDDContext, Either[RuntimeException, Node]] =
    State { ctx =>
      obdd_exists(vars.map(Var(_)), nodes).run(ctx).value
    }

  def forAll(vars: Seq[String], nodes: Seq[Node]): State[OBDDContext, Either[RuntimeException, Node]] =
    State { ctx =>
      obdd_forAll(vars.map(Var(_)), nodes).run(ctx).value
    }

  def ITE(node1: Node, node2: Node, node3: Node): State[OBDDContext, Either[RuntimeException, Node]] =
    State { ctx =>
      obdd_ITE(node1, node2, node3).run(ctx).value
    }

  def sifting_reorder(node: Node): State[OBDDContext, Unit] = sift(node)

  def sifting_reorder_step(node: Node): State[OBDDContext, Seq[String]] = sift_step(node)

  def window_permutation_reorder(node: Node): State[OBDDContext, Unit] = window_permutation(node)

  private val expressionParser = new ExpressionParser
  private val dimacsParser     = new DimacsParser
  private val qdimacsParser    = new QDimacsParser

}
