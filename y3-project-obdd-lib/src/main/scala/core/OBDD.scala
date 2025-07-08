package obdd.core

import obdd.visual.*
import cats.implicits.*
import obdd.parser.QDimacsParser
import cats.data.State
import scala.collection.immutable.HashMap

object OBDD extends DD {

  private[obdd] def obdd_restrict(
    node: Node,
    variable: Var,
    value: Boolean
  ): State[OBDDContext, Either[DDException, Node]] =
    State { ctx =>
      node.label.variable match
        case "true"  => (ctx, Right(TRUE))
        case "false" => (ctx, Right(FALSE))
        case v =>
          if (v == variable.variable) {
            value match
              case true  => (ctx, getHigh(node, ctx.ddCtx))
              case false => (ctx, getLow(node, ctx.ddCtx))
          } else {
            (for {
              high    <- getHigh(node, ctx.ddCtx)
              low     <- getLow(node, ctx.ddCtx)
              (c1, hi) = obdd_restrict(high, variable, value).run(ctx).value
              (c2, lo) = obdd_restrict(low, variable, value).run(c1).value
              h       <- hi
              l       <- lo
            } yield getNode(node.label, h, l).run(c2).value) match {
              case Left(e)     => (ctx, Left(e))
              case Right(c, n) => (c, Right(n))
            }
          }
    }

  private[obdd] def obdd_ITE(f: Node, g: Node, h: Node): State[OBDDContext, Either[DDException, Node]] =
    State { ctx =>
      (f, g, h) match
        case (TRUE, f, g)     => (ctx, Right(f))
        case (FALSE, f, g)    => (ctx, Right(g))
        case (f, TRUE, FALSE) => (ctx, Right(f))
        case (f, g, h) =>
          if (g == h) (ctx, Right(g))
          else {
            ctx.ITETable.get((f, g, h)) match {
              case Some(v) => (ctx, v)
              case None =>
                (for {
                  biggestVar <- ctx.ddCtx.ordering
                                  .filter(o => o == f.label || o == g.label || o == h.label)
                                  .headOption
                                  .toRight(DDException("Node does not exist"))
                  (c1, ft) = obdd_restrict(f, biggestVar, true).run(ctx).value
                  (c2, gt) = obdd_restrict(g, biggestVar, true).run(c1).value
                  (c3, ht) = obdd_restrict(h, biggestVar, true).run(c2).value
                  (c4, ff) = obdd_restrict(f, biggestVar, false).run(c3).value
                  (c5, gf) = obdd_restrict(g, biggestVar, false).run(c4).value
                  (c6, hf) = obdd_restrict(h, biggestVar, false).run(c5).value
                  ft_node <- ft
                  gt_node <- gt
                  ht_node <- ht
                  ff_node <- ff
                  gf_node <- gf
                  hf_node <- hf
                  (c7, t)  = obdd_ITE(ft_node, gt_node, ht_node).run(c6).value
                  (c8, e)  = obdd_ITE(ff_node, gf_node, hf_node).run(c7).value
                  t_node  <- t
                  e_node  <- e
                  (c9, n)  = getNode(biggestVar, t_node, e_node).run(c8).value
                } yield (c9, n)) match {
                  case Left(e)       => (ctx, Left(e))
                  case Right((c, n)) => (c, Right(n))
                }
            }
          }
    }

  private[obdd] def visualiseGlobal(ids: Map[Int, String], showID: Boolean = false, ctx: OBDDContext) =
    nodesToSVG(getglobalDAG(ctx.ddCtx), ids, showID, ctx.ddCtx)

  private def ITE_step(f: Node, g: Node, h: Node): State[OBDDContext, Either[DDException, Node]] =
    State { ctx =>
      (f, g, h) match
        case (TRUE, f, g) =>
          (
            ctx.copy(graphs_vis =
              ctx.graphs_vis ++ Seq(nodesToSVG(getglobalDAG(ctx.ddCtx), Map(f.id -> "red"), ctx = ctx.ddCtx))
            ),
            Right(f)
          )
        case (FALSE, f, g) =>
          (
            ctx.copy(graphs_vis =
              ctx.graphs_vis ++ Seq(nodesToSVG(getglobalDAG(ctx.ddCtx), Map(g.id -> "red"), ctx = ctx.ddCtx))
            ),
            Right(g)
          )
        case (f, TRUE, FALSE) =>
          (
            ctx.copy(graphs_vis =
              ctx.graphs_vis ++ Seq(nodesToSVG(getglobalDAG(ctx.ddCtx), Map(f.id -> "red"), ctx = ctx.ddCtx))
            ),
            Right(f)
          )
        case (f, g, h) =>
          if (g == h) {
            (
              ctx.copy(graphs_vis =
                ctx.graphs_vis ++ Seq(nodesToSVG(getglobalDAG(ctx.ddCtx), Map(g.id -> "red"), ctx = ctx.ddCtx))
              ),
              Right(g)
            )
          } else {
            ctx.ITE_step_Table.get((f, g, h)) match
              case Some(value) => (ctx, value)
              case None =>
                (for {
                  biggestVar <- ctx.ddCtx.ordering
                                  .filter(o => o == f.label || o == g.label || o == h.label)
                                  .headOption
                                  .toRight(DDException("Node does not exist"))
                  (c1, ft) = obdd_restrict(f, biggestVar, true).run(ctx).value
                  (c2, gt) = obdd_restrict(g, biggestVar, true).run(c1).value
                  (c3, ht) = obdd_restrict(h, biggestVar, true).run(c2).value
                  (c4, ff) = obdd_restrict(f, biggestVar, false).run(c3).value
                  (c5, gf) = obdd_restrict(g, biggestVar, false).run(c4).value
                  (c6, hf) = obdd_restrict(h, biggestVar, false).run(c5).value
                  ft_node <- ft
                  gt_node <- gt
                  ht_node <- ht
                  ff_node <- ff
                  gf_node <- gf
                  hf_node <- hf
                  (c7, t)  = ITE_step(ft_node, gt_node, ht_node).run(c6).value
                  (c8, e)  = ITE_step(ff_node, gf_node, hf_node).run(c7).value
                  t_node  <- t
                  e_node  <- e
                  (c9, n)  = getNode(biggestVar, t_node, e_node).run(c8).value
                } yield (c9, n)) match {
                  case Left(e) => (ctx, Left(e))
                  case Right((c, n)) =>
                    (
                      c.copy(graphs_vis =
                        c.graphs_vis ++ Seq(nodesToSVG(getglobalDAG(c.ddCtx), Map(n.id -> "red"), ctx = c.ddCtx))
                      ),
                      Right(n)
                    )
                }
          }
    }

  private[obdd] def IteStep(
    f: Node,
    g: Node,
    h: Node,
    ids: Map[Int, String] = Map()
  ): State[OBDDContext, Seq[String]] =
    State { ctx =>
      val (c, n) = ITE_step(f, g, h).run(ctx.copy(graphs_vis = Seq(visualiseGlobal(ids, ctx = ctx)))).value
      (c.copy(graphs_vis = c.graphs_vis.distinct), c.graphs_vis.distinct)
    }

  private[obdd] def sift_step(node: Node): State[OBDDContext, Seq[String]] =
    State { ctx =>
      def diagram = getDiagram(node, ctx.ddCtx)
      def seq_res = Seq(toSVG(diagram), "Initial OBDD")
      val var_sorted = diagramToNodeSet(diagram).toSeq
        .map(_.label)
        .groupBy(identity)
        .mapValues(_.size)
        .toSeq
        .sortBy(_._2)
        .reverse
        .map(_._1)
        .filter(_ != Var("true"))
        .filter(_ != Var("false"))

      var_sorted.foldLeft[(OBDDContext, Seq[String])](ctx, seq_res) { case ((ct, se), curVar) =>
        def i = ct.ddCtx.ordering.indexOf(curVar)
        val (c1, l1, v1) =
          Seq.range(i, var_sorted.size - 1).foldLeft[(OBDDContext, Seq[(Int, Int)], Seq[String])](ct, Seq(), se) {
            case ((ctx_acc, res_acc, vis_acc), cur_ind) =>
              val cs        = var_swap(cur_ind).runS(ctx_acc).value
              def colourMap = cs.ddCtx.nodes.filter(e => e._1._1 == curVar).map(e => (e._2 -> "red"))
              def tn        = getNodeFromID(node.id, cs).toOption.get
              def di        = getDiagram(tn, cs.ddCtx)
              (
                cs,
                res_acc ++ Seq((cur_ind, size(di))),
                vis_acc ++ Seq(toSVG(di, colourMap)) ++ Seq(s"Sifting $curVar downwards")
              )
          }
        val (c2, l2, v2) =
          Seq.range(var_sorted.size - 2, -1, -1).foldLeft[(OBDDContext, Seq[(Int, Int)], Seq[String])](c1, l1, v1) {
            case ((ctx_acc, res_acc, vis_acc), cur_ind) =>
              val cs        = var_swap(cur_ind).runS(ctx_acc).value
              def colourMap = cs.ddCtx.nodes.filter(e => e._1._1 == curVar).map(e => (e._2 -> "red"))
              def tn        = getNodeFromID(node.id, cs).toOption.get
              def di        = getDiagram(tn, cs.ddCtx)
              (
                cs,
                res_acc ++ Seq((cur_ind, size(di))),
                vis_acc ++ Seq(toSVG(di, colourMap)) ++ Seq(s"Sifting $curVar upwards")
              )
          }
        def best_index = (l2).minBy(_._2)._1
        Seq.range(0, best_index).foldLeft[(OBDDContext, Seq[String])](c2, v2) { case ((ctx_acc, vis_acc), cur_ind) =>
          val cs        = var_swap(cur_ind).runS(ctx_acc).value
          def colourMap = cs.ddCtx.nodes.filter(e => e._1._1 == curVar).map(e => (e._2 -> "red"))
          def tn        = getNodeFromID(node.id, cs).toOption.get
          def di        = getDiagram(tn, cs.ddCtx)
          (cs, vis_acc ++ Seq(toSVG(di, colourMap)) ++ Seq(s"Sifting $curVar to optimal position"))
        }
      }

    }

  private[obdd] def sift(node: Node): State[OBDDContext, Unit] =
    State { ctx =>
      def diagram = getDiagram(node, ctx.ddCtx)
      val var_sorted = diagramToNodeSet(diagram).toSeq
        .map(_.label)
        .groupBy(identity)
        .mapValues(_.size)
        .toSeq
        .sortBy(_._2)
        .reverse
        .map(_._1)
        .filter(_ != Var("true"))
        .filter(_ != Var("false"))
      (
        var_sorted.foldLeft(ctx) { case (ct, curVar) =>
          def i = ct.ddCtx.ordering.indexOf(curVar)
          val (c1, l1) = Seq.range(i, var_sorted.size - 1).foldLeft[(OBDDContext, Seq[(Int, Int)])](ct, Seq()) {
            case ((ctx_acc, res_acc), cur_ind) =>
              val cs = var_swap(cur_ind).runS(ctx_acc).value
              def tn = getNodeFromID(node.id, cs).toOption.get
              def di = getDiagram(tn, cs.ddCtx)
              (cs, res_acc ++ Seq((cur_ind, size(di))))
          }
          val (c2, l2) = Seq.range(var_sorted.size - 2, -1, -1).foldLeft[(OBDDContext, Seq[(Int, Int)])](c1, l1) {
            case ((ctx_acc, res_acc), cur_ind) =>
              val cs = var_swap(cur_ind).runS(ctx_acc).value
              def tn = getNodeFromID(node.id, cs).toOption.get
              def di = getDiagram(tn, cs.ddCtx)
              (cs, res_acc ++ Seq((cur_ind, size(di))))
          }
          def best_index = (l2).minBy(_._2)._1
          Seq.range(0, best_index).foldLeft[OBDDContext](c2) { case (ctx_acc, cur_ind) =>
            var_swap(cur_ind).runS(ctx_acc).value
          }
        },
        ()
      )
    }

  private def restore(a: Seq[Var], b: Seq[Var]): State[OBDDContext, Unit] =
    State { ctx =>
      var aa      = a
      var swapped = false
      val cs = Seq.range(0, aa.length - 1).foldLeft(ctx) { (ct, i) =>
        if (b.indexOf(aa(i + 1)) < b.indexOf(aa(i))) {
          aa = aa.updated(i, aa(i + 1)).updated(i + 1, aa(i))
          swapped = true
          var_swap(i).runS(ct).value
        } else { ct }
      }
      if (swapped)
        restore(aa, b).run(cs).value
      else
        (cs, ())
    }

  private[obdd] def window_permutation(node: Node): State[OBDDContext, Unit] =
    State { ctx =>
      def diagram = getDiagram(node, ctx.ddCtx)
      def vars = diagramToNodeSet(diagram).toSeq
        .map(_.label)
        .filter(_ != Var("true"))
        .filter(_ != Var("false"))
        .distinct
      (
        Seq.range(0, vars.size - 2).foldLeft(ctx) { case (ct, i) =>
          def cur_permutation = vars.slice(i, i + 3)
          val c1              = var_swap(i).runS(ct).value
          val s1              = size(getDiagram(node, c1.ddCtx))
          val p1              = cur_permutation.updated(0, cur_permutation(1)).updated(1, cur_permutation(0))
          val c2              = var_swap(i + 1).runS(c1).value
          val s2              = size(getDiagram(node, c2.ddCtx))
          val p2              = p1.updated(1, cur_permutation(2)).updated(2, cur_permutation(1))
          val c3              = var_swap(i).runS(c2).value
          val s3              = size(getDiagram(node, c3.ddCtx))
          val p3              = p2.updated(0, cur_permutation(1)).updated(1, cur_permutation(0))
          val c4              = var_swap(i + 1).runS(c3).value
          val s4              = size(getDiagram(node, c4.ddCtx))
          val p4              = p3.updated(1, cur_permutation(2)).updated(2, cur_permutation(1))
          val c5              = var_swap(i).runS(c4).value
          val s5              = size(getDiagram(node, c5.ddCtx))
          val p5              = p4.updated(0, cur_permutation(1)).updated(1, cur_permutation(0))
          def best_permutation = Seq(s1, s2, s3, s4, s5).max match {
            case x if x == s1 => p1
            case x if x == s2 => p2
            case x if x == s3 => p3
            case x if x == s4 => p4
            case x if x == s5 => p5
          }
          restore(p5, best_permutation).runS(c5).value
        },
        ()
      )
    }

  private def get_new_node_ids(node: Node, x_j: Var): State[OBDDContext, Either[DDException, (Int, Int)]] =
    State { ctx =>

      val f1_co = getHigh(node, ctx.ddCtx) match
        case Left(value) => Left(DDException("high node not found"))
        case Right(value) =>
          value match
            case TRUE  => Right(TRUE.id, TRUE.id)
            case FALSE => Right(FALSE.id, FALSE.id)
            case n =>
              for {
                f11 <- getHigh(n, ctx.ddCtx)
                f10 <- getLow(n, ctx.ddCtx)
              } yield
                if (n.label == x_j) (f11.id, f10.id)
                else (n.id, n.id)

      val f0_co = getLow(node, ctx.ddCtx) match
        case Left(value) => Left(DDException("low node not found"))
        case Right(value) =>
          value match
            case TRUE  => Right(TRUE.id, TRUE.id)
            case FALSE => Right(FALSE.id, FALSE.id)
            case n =>
              for {
                f01 <- getHigh(n, ctx.ddCtx)
                f00 <- getLow(n, ctx.ddCtx)
              } yield
                if (n.label == x_j) (f01.id, f00.id)
                else (n.id, n.id)

      (for {
        f1co <- f1_co
        f0co <- f0_co
      } yield {
        val (c1, newHi) = createNode(node.label, f1co._1, f0co._1).run(ctx).value
        val (c2, newLo) = createNode(node.label, f1co._2, f0co._2).run(c1).value
        (c2, newHi, newLo)
      }) match {
        case Left(e)          => (ctx, Left(e))
        case Right((c, i, j)) => (c, Right(i, j))
      }
    }

  def var_swap(i: Int): State[OBDDContext, Either[DDException, Unit]] =
    State { ctx =>
      val x_i = ctx.ddCtx.ordering(i)
      val x_j = ctx.ddCtx.ordering(i + 1)
      val c = ctx.ddCtx.nodes.foldLeft[OBDDContext](ctx) { case (ctx_acc, (k, v)) =>
        if (k._1 == x_i) {
          val (c, n) = get_new_node_ids(entryToNode(k, v), x_j).run(ctx_acc).value
          n match {
            case Left(e) => ctx_acc
            case Right(ids) =>
              c.ddCtx.nodes.get((x_j, ids._1, ids._2)) match {
                case Some(value) =>
                  c.copy(ddCtx = c.ddCtx.copy(nodes = c.ddCtx.nodes.removed(k).updated((x_j, ids._1, ids._2), v)))
                case None =>
                  if (ids._1 == ids._2) {
                    val n = c.ddCtx.nodes.find(_._2 == ids._1).map(entryToNode).get
                    c.copy(ddCtx = c.ddCtx.copy(nodes = c.ddCtx.nodes.removed(k).updated((n.label, n.high, n.low), v)))
                  } else {
                    c.copy(ddCtx = c.ddCtx.copy(nodes = c.ddCtx.nodes.removed(k).updated((x_j, ids._1, ids._2), v)))
                  }
              }
          }
        } else {
          ctx_acc
        }
      }
      (
        c.copy(ddCtx = c.ddCtx.copy(ordering = ctx.ddCtx.ordering.updated(i, x_j).updated(i + 1, x_i))),
        Right(())
      )
    }

  private[obdd] def obdd_exists(vars: Seq[Var], nodes: Seq[Node]): State[OBDDContext, Either[DDException, Node]] =
    State { ctx =>
      if (nodes.size == 0) (ctx, Right(FALSE))
      else if (nodes.contains(TRUE)) (ctx, Right(TRUE))
      else if (nodes.contains(FALSE)) obdd_exists(vars, nodes.filter(_ != FALSE)).run(ctx).value
      else {
        (ctx.ddCtx.ordering
          .filter(nodes.map(_.label).contains(_))
          .headOption
          .toRight(DDException("node not found"))
          .flatMap { biggestVar =>
            def lr = nodes.map { n =>
              for {
                hi <- getHigh(n, ctx.ddCtx)
                lo <- getLow(n, ctx.ddCtx)
              } yield
                if (n.label == biggestVar) {
                  (lo, hi)
                } else {
                  (n, n)
                }
            }.traverse(identity)

            lr.flatMap { nodes =>
              def lrr = nodes.unzip
              if (vars.contains(biggestVar)) {
                obdd_exists(vars.filter(_ != biggestVar), lrr._1 ++ lrr._2).run(ctx).value.traverse(identity)
              } else {
                val (c1, k1) = obdd_exists(vars, lrr._1).run(ctx).value
                val (c2, k2) = obdd_exists(vars, lrr._2).run(c1).value
                for {
                  k11          <- k1
                  k22          <- k2
                  (c3, newnode) = createNode(biggestVar, k22.id, k11.id).run(c2).value
                  node         <- getNodeFromID(newnode, c3)
                } yield ((c3, node))
              }
            }
          }) match {
          case Left(e)       => (ctx, Left(e))
          case Right((c, n)) => (c, Right(n))
        }
      }
    }

  private[obdd] def obdd_forAll(vars: Seq[Var], nodes: Seq[Node]): State[OBDDContext, Either[DDException, Node]] =
    State { ctx =>
      if (nodes.size == 0) (ctx, Right(TRUE))
      else if (nodes.contains(FALSE)) (ctx, Right(FALSE))
      else if (nodes.contains(TRUE)) obdd_forAll(vars, nodes.filter(_ != TRUE)).run(ctx).value
      else {
        (ctx.ddCtx.ordering
          .filter(nodes.map(_.label).contains(_))
          .headOption
          .toRight(DDException("node not found"))
          .flatMap { biggestVar =>
            def lr = nodes.map { n =>
              for {
                hi <- getHigh(n, ctx.ddCtx)
                lo <- getLow(n, ctx.ddCtx)
              } yield
                if (n.label == biggestVar) {
                  (lo, hi)
                } else {
                  (n, n)
                }
            }.traverse(identity)

            lr.flatMap { nodes =>
              def lrr = nodes.unzip
              if (vars.contains(biggestVar)) {
                obdd_forAll(vars.filter(_ != biggestVar), lrr._1 ++ lrr._2).run(ctx).value.traverse(identity)
              } else {
                val (c1, k1) = obdd_forAll(vars, lrr._1).run(ctx).value
                val (c2, k2) = obdd_forAll(vars, lrr._2).run(c1).value
                for {
                  k11          <- k1
                  k22          <- k2
                  (c3, newnode) = createNode(biggestVar, k22.id, k11.id).run(c2).value
                  node         <- getNodeFromID(newnode, c3)
                } yield ((c3, node))
              }
            }
          }) match {
          case Left(e)       => (ctx, Left(e))
          case Right((c, n)) => (c, Right(n))
        }
      }
    }

  private def getNodeFromQuant(q: QUANT): State[OBDDContext, Either[DDException, Node]] =
    State { ctx =>
      q match
        case EXISTS(vars, formula) =>
          val (c, node) = formula match
            case b: BooleanExpression => buildNode(b).run(ctx).value
            case quant: QUANT         => getNodeFromQuant(quant).run(ctx).value
          (for {
            n      <- node
            (c1, e) = obdd_exists(vars, Seq(n)).run(c).value
            ex     <- e
          } yield (c1, ex)) match {
            case Left(e)     => (ctx, Left(e))
            case Right(c, v) => (c, Right(v))
          }

        case FORALL(vars, formula) =>
          val (c, node) = formula match
            case b: BooleanExpression => buildNode(b).run(ctx).value
            case quant: QUANT         => getNodeFromQuant(quant).run(ctx).value
          (for {
            n      <- node
            (c1, e) = obdd_forAll(vars, Seq(n)).run(c).value
            ex     <- e
          } yield (c1, ex)) match {
            case Left(e)     => (ctx, Left(e))
            case Right(c, v) => (c, Right(v))
          }
    }

  private[obdd] def solve_qbf(qdimacs: QUANT | BooleanExpression): State[OBDDContext, Either[DDException, Node]] =
    State { ctx =>
      qdimacs match
        case q: QUANT             => getNodeFromQuant(q).run(ctx).value
        case b: BooleanExpression => buildNode(b).run(ctx).value
    }

  def buildNode(exp: BooleanExpression): State[OBDDContext, Either[DDException, Node]] =
    State { ctx =>
      if (!validateOrdering(ctx.ddCtx.ordering)) {
        (ctx, Left(DDException("the ordering contains duplicates")))
      } else if (!validateExpression(exp, ctx.ddCtx.ordering)) {
        (ctx, Left(DDException("expression contains variables not found in ordering")))
      } else {
        build(exp).run(ctx).value
      }
    }

  private[obdd] def build(exp: BooleanType): State[OBDDContext, Either[DDException, Node]] =
    State { ctx =>
      exp match
        case b: Boolean => (ctx, Right(boolToNode(b)))

        case v: Var =>
          val (c, n) = getNode(v, TRUE, FALSE).run(ctx).value
          (c, Right(n))

        case And(exps*) =>
          exps
            .map(build)
            .reduce { (a, b) =>
              State { ct =>
                val (c1, aa) = a.run(ct).value
                val (c2, bb) = b.run(c1).value
                (for {
                  aa_node  <- aa
                  bb_node  <- bb
                  (c, ite)  = (obdd_ITE(aa_node, bb_node, FALSE).run(c2).value)
                  ite_node <- ite
                } yield (c, ite_node)) match {
                  case Left(e)       => (ct, Left(e))
                  case Right((c, v)) => (c, Right(v))
                }
              }
            }
            .run(ctx)
            .value

        case Or(exps*) =>
          exps
            .map(build)
            .reduce { (a, b) =>
              State { ct =>
                val (c1, aa) = a.run(ct).value
                val (c2, bb) = b.run(c1).value
                (for {
                  aa_node  <- aa
                  bb_node  <- bb
                  (c, ite)  = (obdd_ITE(aa_node, TRUE, bb_node).run(c2).value)
                  ite_node <- ite
                } yield (c, ite_node)) match {
                  case Left(e)       => (ct, Left(e))
                  case Right((c, v)) => (c, Right(v))
                }
              }
            }
            .run(ctx)
            .value

        case Implication(exp1, exp2) =>
          val (c1, e1) = build(exp1).run(ctx).value
          val (c2, e2) = build(exp2).run(c1).value
          (for {
            e1_node  <- e1
            e2_node  <- e2
            (c, ite)  = obdd_ITE(e1_node, e2_node, TRUE).run(c2).value
            ite_node <- ite
          } yield (c, ite_node)) match {
            case Left(e)       => (ctx, Left(e))
            case Right((c, v)) => (c, Right(v))
          }

        case Not(exp) =>
          val (c1, b) = build(exp).run(ctx).value
          (for {
            b_node   <- b
            (c2, ite) = obdd_ITE(b_node, FALSE, TRUE).run(c1).value
            ite_node <- ite
          } yield (c2, ite_node)) match {
            case Left(e)       => (ctx, Left(e))
            case Right((c, v)) => (c, Right(v))
          }
    }

  private def diagramToNodeSet(d: Diagram): Set[Node] =
    Set(d.node) ++ {
      (d.high, d.low) match
        case (Some(hi), Some(lo)) => diagramToNodeSet(hi) ++ diagramToNodeSet(lo)
        case _                    => Set()
    }

  private[obdd] def size(d: Diagram): Int =
    diagramToNodeSet(d).size

  private[obdd] def size(ctx: OBDDContext) = ctx.ddCtx.nodes.size

  private def createNode(v: Var, hi: Int, lo: Int): State[OBDDContext, Int] =
    State { ctx =>
      if (hi == lo) (ctx, hi)
      else {
        ctx.ddCtx.nodes.get((v, hi, lo)) match {
          case Some(n) => (ctx, n)
          case None =>
            (
              ctx.copy(
                ddCtx = ctx.ddCtx.copy(
                  nodes = ctx.ddCtx.nodes + ((v, hi, lo) -> (ctx.id + 1))
                ),
                id = ctx.id + 1
              ),
              ctx.id + 1
            )
        }
      }
    }

  private[obdd] def getNode(v: Var, hi: Node, lo: Node): State[OBDDContext, Node] =
    State { ctx =>
      val (newCtx, nodeId) = createNode(v, hi.id, lo.id).run(ctx).value
      (newCtx, newCtx.ddCtx.nodes.find(_._2 == nodeId).map(entryToNode).get)
    }

  private[obdd] def getNodeFromID(id: Int, ctx: OBDDContext): Either[DDException, Node] =
    ctx.ddCtx.nodes.find(_._2 == id).map(entryToNode).toRight(DDException("ID does not exist"))

}

private[obdd] def entryToNode(key: (Var, Int, Int), value: Int): Node =
  Node(value, key._1, key._2, key._3)
