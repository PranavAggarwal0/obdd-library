package obdd.example

import obdd.core.{DDException, Node}
import obdd.core.ZDD.*
import obdd.api.ZDDLib.*
import obdd.core.ZDDContext
import cats.data.State

class ZDDNQueens(n: Int) {

  def ordering = for {
    i <- Seq.range(1, n + 1)
    j <- Seq.range(1, n + 1)
  } yield "x" + i.toString + j.toString

  val zdd_ctx = newZDD(ordering ++ Seq("true", "false"))

  private def getNode(i: Int, j: Int) = set(Set(s"x$i$j"))

  private def getAttackers(i: Int, j: Int): Seq[(Int, Int)] =
    (getAttackersL(i, j) ++ getAttackersU(i, j) ++ getAttackersR(i, j)).toSeq.sortBy(_._1)

  private def getAttackersL(i: Int, j: Int): Set[(Int, Int)] =
    if (i == 1) Set()
    else if (j == 1) Set()
    else Set((i - 1, j - 1)) ++ getAttackersL(i - 1, j - 1)

  private def getAttackersU(i: Int, j: Int): Set[(Int, Int)] =
    if (i == 1) Set()
    else Set((i - 1, j)) ++ getAttackersU(i - 1, j)

  private def getAttackersR(i: Int, j: Int): Set[(Int, Int)] =
    if (i == 1) Set()
    else if (j == n) Set()
    else Set((i - 1, j + 1)) ++ getAttackersR(i - 1, j + 1)

  private def solveRec(i: Int): State[ZDDContext, Either[DDException, Node]] =
    State { ctx =>
      if (i == 1) family(Seq.range(1, n + 1).map(i => (s"x1$i")).map(Set(_)).toSet).run(ctx).value
      else {
        def attackers = Seq.range(1, n + 1).map(a => (a, getAttackers(i, a)))
        val (c1, subsets) =
          attackers.foldLeft[(ZDDContext, Seq[(Int, Either[DDException, Node])])]((ctx, Seq())) {
            case ((ctx_acc, res_acc), att) =>
              val t =
                (att._2.foldLeft[(ZDDContext, Either[DDException, Node])](solveRec(i - 1).run(ctx_acc).value) {
                  case ((acc_ct, acc_res), ind) =>
                    (for {
                      node   <- acc_res
                      (c, sn) = subset0(node, s"x${ind._1}${ind._2}").run(acc_ct).value
                      n      <- sn
                    } yield (c, n)) match {
                      case Left(e)       => (acc_ct, (Left(e)))
                      case Right((c, n)) => (c, (Right(n)))
                    }
                })
              (t._1, res_acc ++ Seq((att._1, t._2)))
          }
        val (c2, muls) = subsets.foldLeft[(ZDDContext, Seq[Either[DDException, Node]])](c1, Seq()) {
          case ((ctx_acc, res_acc), node) =>
            (for {
              n1       <- node._2
              (c3, n2n) = getNode(i, node._1).run(ctx_acc).value
              n2       <- n2n
              (c, mn)   = mul(n1, n2).run(c3).value
              m        <- mn
            } yield (c, m)) match {
              case Left(e)       => (ctx_acc, res_acc ++ Seq(Left(e)))
              case Right((c, n)) => (c, res_acc ++ Seq(Right(n)))
            }
        }
        if (muls.size == 1) {
          (c2, muls.head)
        } else {
          muls.tail.foldLeft[(ZDDContext, Either[DDException, Node])](c2, muls.head) {
            case ((ctx_acc, res_acc), node) =>
              (for {
                n1    <- res_acc
                n2    <- node
                (c, u) = union(n1, n2).run(ctx_acc).value
                un    <- u
              } yield (c, un)) match
                case Left(value)   => (ctx_acc, Left(value))
                case Right((c, u)) => (c, Right(u))
          }
        }
      }
    }

  def solve = {
    val startTimeMillis = System.currentTimeMillis()
    val (ct, node)      = solveRec(n).run(zdd_ctx).value
    val endTimeMillis   = System.currentTimeMillis()
    println(s"Time taken for $n queens: ${(endTimeMillis - startTimeMillis) / 1000}")
    println(s"Number of solutions: ${node.flatMap(zdd_count(_, ct))}")
    println(s"Number of nodes in diagram: ${node.map(zdd_size(_, ct))}")
  }
}
