package obdd.core

import obdd.core.DDException
import cats.data.State

object ZDD extends DD {

  private[obdd] def empty() = FALSE
  private[obdd] def base()  = TRUE

  private[obdd] def zdd_subset1(p: Node, v: Var): State[ZDDContext, Either[DDException, Node]] =
    State { ctx =>
      ctx.subset1cache.get((p, v)) match
        case Some(v) => (ctx, v)
        case None =>
          if (p.label == v)
            (ctx, ctx.ddCtx.nodes.find(_._2 == p.high).map(entryToNode).toRight(DDException("node not found")))
          else {
            def l = ctx.ddCtx.ordering.filter(o => o == v || o == p.label)
            if (l.size < 2) (ctx, Left(DDException("variable not found in ordering")))
            else {
              if (l.head == v) (ctx, Right(FALSE))
              else {
                (for {
                  p1      <- getHigh(p, ctx.ddCtx)
                  p0      <- getLow(p, ctx.ddCtx)
                  (c1, hi) = zdd_subset1(p1, v).run(ctx).value
                  (c2, lo) = zdd_subset1(p0, v).run(c1).value
                  hiNode  <- hi
                  loNode  <- lo
                  (c3, n)  = getNode(p.label, hiNode, loNode).run(c2).value
                } yield (c3, n)) match
                  case Left(value)   => (ctx, Left(value))
                  case Right((c, n)) => (c.copy(subset1cache = c.subset1cache + ((p, v) -> Right(n))), Right(n))
              }
            }
          }
    }

  private[obdd] def zdd_subset0(p: Node, v: Var): State[ZDDContext, Either[DDException, Node]] =
    State { ctx =>
      ctx.subset0cache.get((p, v)) match
        case Some(v) => (ctx, v)
        case None =>
          if (p.label == v)
            (ctx, ctx.ddCtx.nodes.find(_._2 == p.low).map(entryToNode).toRight(DDException("node not found")))
          else {
            def l = ctx.ddCtx.ordering.filter(o => o == v || o == p.label)
            if (l.size < 2) (ctx, Left(DDException("variable not found in ordering")))
            else {
              if (l.head == v) (ctx, Right(p))
              else {
                (for {
                  p1      <- getHigh(p, ctx.ddCtx)
                  p0      <- getLow(p, ctx.ddCtx)
                  (c1, hi) = zdd_subset0(p1, v).run(ctx).value
                  (c2, lo) = zdd_subset0(p0, v).run(c1).value
                  hiNode  <- hi
                  loNode  <- lo
                  (c3, n)  = getNode(p.label, hiNode, loNode).run(c2).value
                } yield (c3, n)) match
                  case Left(value)   => (ctx, Left(value))
                  case Right((c, n)) => (c.copy(subset0cache = c.subset0cache + ((p, v) -> Right(n))), Right(n))
              }
            }
          }
    }

  private[obdd] def zdd_set(v: Set[Var]): State[ZDDContext, Either[DDException, Node]] =
    State { ctx =>
      if (v.isEmpty) (ctx, Right(TRUE))
      else {
        (for {
          biggestVar <-
            ctx.ddCtx.ordering.filter(v.contains(_)).headOption.toRight(DDException("var not found in ordering"))
          (c, hiNode) = zdd_set(v - biggestVar).run(ctx).value
          hi         <- hiNode
        } yield (biggestVar, c, hi)) match
          case Left(e) => (ctx, Left(e))
          case Right((b, c, n)) =>
            if (b.variable == "true") (c, Right(TRUE))
            else if (b.variable == "false") (c, Right(FALSE))
            else {
              val node = getNode(b, n, FALSE).run(c).value
              node.copy(_2 = Right(node._2))
            }
      }
    }

  private[obdd] def zdd_family(v: Set[Set[Var]]): State[ZDDContext, Either[DDException, Node]] =
    State { ctx =>
      val (nc, s) = v.foldLeft[(ZDDContext, Set[Either[DDException, Node]])](ctx, Set()) { case ((acc, se), s) =>
        val (ct, st) = zdd_set(s).run(acc).value
        (ct, se + st)
      }
      if (s.size <= 1) {
        (nc, s.head)
      } else {
        val se = s.toSeq
        val (fc, un) = (for {
          n1    <- se(0)
          n2    <- se(1)
          (c, u) = zdd_union(n1, n2).run(nc).value
          un    <- u
        } yield (c, un)) match
          case Left(value)   => (nc, Left(value))
          case Right((c, u)) => (c, Right(u))
        if (se.size == 2) {
          (fc, un)
        } else {
          s.drop(2).foldLeft[(ZDDContext, Either[DDException, Node])](fc, un) { case ((acc, res), cur) =>
            (for {
              n1     <- res
              n2     <- cur
              (c, un) = zdd_union(n1, n2).run(acc).value
              u      <- un
            } yield (c, u)) match
              case Left(e)       => (acc, Left(e))
              case Right((c, u)) => (c, Right(u))
          }
        }
      }

    }

  private[obdd] def zdd_change(p: Node, v: Var): State[ZDDContext, Either[DDException, Node]] =
    State { ctx =>
      ctx.changeCache.get((p, v)) match
        case Some(v) => (ctx, v)
        case None =>
          if (p.label == v) {
            val (c, n) = createNode(v, p.low, p.high).run(ctx).value
            (c, c.ddCtx.nodes.find(_._2 == n).map(entryToNode).toRight(DDException("node not found")))
          } else {
            def l = ctx.ddCtx.ordering.filter(o => o == v || o == p.label)
            if (l.size < 2) (ctx, Left(DDException("variable not found in ordering")))
            else {
              if (l.head == v) {
                val (c, n) = getNode(v, p, FALSE).run(ctx).value
                (c.copy(changeCache = c.changeCache + ((p, v) -> Right(n))), Right(n))
              } else {
                (for {
                  p1      <- getHigh(p, ctx.ddCtx)
                  p0      <- getLow(p, ctx.ddCtx)
                  (c1, hi) = zdd_change(p1, v).run(ctx).value
                  hiNode  <- hi
                  (c2, lo) = zdd_change(p0, v).run(c1).value
                  loNode  <- lo
                  (c3, n)  = getNode(p.label, hiNode, loNode).run(c2).value
                } yield (c3, n)) match
                  case Left(value)   => (ctx, Left(value))
                  case Right((c, n)) => (c.copy(changeCache = c.changeCache + ((p, v) -> Right(n))), Right(n))
              }
            }
          }
    }

  private[obdd] def zdd_union(p: Node, q: Node): State[ZDDContext, Either[DDException, Node]] =
    State { ctx =>
      if (isBigger(p.label, q.label, ctx.ddCtx)) zdd_union(q, p).run(ctx).value
      else if (p == FALSE) (ctx, Right(q))
      else if (q == FALSE || q == p) (ctx, Right(p))
      else {
        ctx.unionCache.get((p, q)) match
          case Some(v) => (ctx, v)
          case None =>
            if (isBigger(q.label, p.label, ctx.ddCtx)) {
              (for {
                q0      <- getLow(q, ctx.ddCtx)
                q1      <- getHigh(q, ctx.ddCtx)
                (c1, un) = zdd_union(p, q0).run(ctx).value
                u       <- un
                (c2, n)  = getNode(q.label, q1, u).run(c1).value
              } yield (c2, n)) match
                case Left(value)   => (ctx, Left(value))
                case Right((c, n)) => (c.copy(unionCache = c.unionCache + ((p, q) -> Right(n))), Right(n))
            } else {
              (for {
                p0      <- getLow(p, ctx.ddCtx)
                p1      <- getHigh(p, ctx.ddCtx)
                q0      <- getLow(q, ctx.ddCtx)
                q1      <- getHigh(q, ctx.ddCtx)
                (c1, hn) = zdd_union(p1, q1).run(ctx).value
                hi      <- hn
                (c2, ln) = zdd_union(p0, q0).run(c1).value
                lo      <- ln
                (c3, n)  = getNode(p.label, hi, lo).run(c2).value
              } yield (c3, n)) match
                case Left(value)   => (ctx, Left(value))
                case Right((c, n)) => (c.copy(unionCache = c.unionCache + ((p, q) -> Right(n))), Right(n))
            }
      }
    }

  private[obdd] def zdd_intersection(p: Node, q: Node): State[ZDDContext, Either[DDException, Node]] =
    State { ctx =>
      if (p == FALSE || q == FALSE) (ctx, Right(FALSE))
      else if (p == q) (ctx, Right(p))
      else {
        ctx.intersectionCache.get((p, q)) match
          case Some(v) => (ctx, v)
          case None =>
            if (isBigger(p.label, q.label, ctx.ddCtx)) {
              (for {
                lo      <- getLow(p, ctx.ddCtx)
                (c, int) = zdd_intersection(lo, q).run(ctx).value
                i       <- int
              } yield (c, i)) match
                case Left(e)       => (ctx, Left(e))
                case Right((c, n)) => (c.copy(intersectionCache = c.intersectionCache + ((p, q) -> Right(n))), Right(n))
            } else if (isBigger(q.label, q.label, ctx.ddCtx)) {
              (for {
                lo      <- getLow(q, ctx.ddCtx)
                (c, int) = zdd_intersection(p, lo).run(ctx).value
                i       <- int
              } yield (c, i)) match
                case Left(e)       => (ctx, Left(e))
                case Right((c, n)) => (c.copy(intersectionCache = c.intersectionCache + ((p, q) -> Right(n))), Right(n))
            } else {
              (for {
                p0      <- getLow(p, ctx.ddCtx)
                p1      <- getHigh(p, ctx.ddCtx)
                q0      <- getLow(q, ctx.ddCtx)
                q1      <- getHigh(q, ctx.ddCtx)
                (c1, hn) = zdd_intersection(p1, q1).run(ctx).value
                hi      <- hn
                (c2, ln) = zdd_intersection(p0, q0).run(c1).value
                lo      <- ln
                (c3, n)  = getNode(p.label, hi, lo).run(c2).value
              } yield (c3, n)) match
                case Left(e)       => (ctx, Left(e))
                case Right((c, n)) => (c.copy(intersectionCache = c.intersectionCache + ((p, q) -> Right(n))), Right(n))
            }
      }
    }

  private[obdd] def zdd_difference(p: Node, q: Node): State[ZDDContext, Either[DDException, Node]] =
    State { ctx =>
      if (p == FALSE || p == q) (ctx, Right(FALSE))
      else if (q == FALSE) (ctx, Right(p))
      else {
        ctx.differenceCache.get((p, q)) match
          case Some(v) => (ctx, v)
          case None =>
            if (isBigger(q.label, p.label, ctx.ddCtx)) {
              (for {
                lo    <- getLow(q, ctx.ddCtx)
                (c, d) = zdd_difference(p, lo).run(ctx).value
                dif   <- d
              } yield (c, dif)) match
                case Left(e)       => (ctx, Left(e))
                case Right((c, n)) => (c.copy(differenceCache = c.differenceCache + ((p, q) -> Right(n))), Right(n))
            } else if (isBigger(p.label, q.label, ctx.ddCtx)) {
              (for {
                p1      <- getHigh(p, ctx.ddCtx)
                p0      <- getLow(p, ctx.ddCtx)
                (c1, ln) = zdd_difference(p0, q).run(ctx).value
                lo      <- ln
                (c2, n)  = getNode(p.label, p1, lo).run(c1).value
              } yield (c2, n)) match
                case Left(e)       => (ctx, Left(e))
                case Right((c, n)) => (c.copy(differenceCache = c.differenceCache + ((p, q) -> Right(n))), Right(n))
            } else {
              (for {
                p0      <- getLow(p, ctx.ddCtx)
                p1      <- getHigh(p, ctx.ddCtx)
                q0      <- getLow(q, ctx.ddCtx)
                q1      <- getHigh(q, ctx.ddCtx)
                (c1, hn) = zdd_difference(p1, q1).run(ctx).value
                hi      <- hn
                (c2, ln) = zdd_difference(p0, q0).run(c1).value
                lo      <- ln
                (c3, n)  = getNode(p.label, hi, lo).run(c2).value
              } yield (c3, n)) match
                case Left(e)       => (ctx, Left(e))
                case Right((c, n)) => (c.copy(differenceCache = c.differenceCache + ((p, q) -> Right(n))), Right(n))
            }
      }
    }

  private[obdd] def zdd_mul(p: Node, q: Node): State[ZDDContext, Either[DDException, Node]] =
    State { ctx =>
      if (p == FALSE || q == FALSE) (ctx, Right(FALSE))
      else if (p == TRUE) (ctx, Right(q))
      else if (q == TRUE) (ctx, Right(p))
      else if (isBigger(p.label, q.label, ctx.ddCtx)) zdd_mul(q, p).run(ctx).value
      else {
        ctx.mulCache.get((p, q)) match
          case Some(v) => (ctx, v)
          case None =>
            if (isBigger(q.label, p.label, ctx.ddCtx)) {
              (for {
                q1       <- getHigh(q, ctx.ddCtx)
                q0       <- getLow(q, ctx.ddCtx)
                (c1, m1n) = zdd_mul(p, q1).run(ctx).value
                m1       <- m1n
                (c2, m2n) = zdd_mul(p, q0).run(c1).value
                m2       <- m2n
                (c3, n)   = getNode(q.label, m1, m2).run(c2).value
              } yield (c3, n)) match
                case Left(e)       => (ctx, Left(e))
                case Right((c, n)) => (c.copy(mulCache = c.mulCache + ((p, q) -> Right(n))), Right(n))
            } else {
              (for {
                p0       <- getLow(p, ctx.ddCtx)
                p1       <- getHigh(p, ctx.ddCtx)
                q0       <- getLow(q, ctx.ddCtx)
                q1       <- getHigh(q, ctx.ddCtx)
                (c1, m1n) = zdd_mul(p1, q1).run(ctx).value
                m1       <- m1n
                (c2, m2n) = zdd_mul(p1, q0).run(c1).value
                m2       <- m2n
                (c3, u1n) = zdd_union(m1, m2).run(c2).value
                u1       <- u1n
                (c4, m3n) = zdd_mul(p0, q1).run(c3).value
                m3       <- m3n
                (c5, u2n) = zdd_union(u1, m3).run(c4).value
                u2       <- u2n
                (c6, m4n) = zdd_mul(p0, q0).run(c5).value
                m4       <- m4n
                (c7, n)   = getNode(p.label, u2, m4).run(c6).value
              } yield (c7, n)) match
                case Left(e)       => (ctx, Left(e))
                case Right((c, n)) => (c.copy(mulCache = c.mulCache + ((p, q) -> Right(n))), Right(n))
            }
      }
    }

  private[obdd] def zdd_div(p: Node, q: Node): State[ZDDContext, Either[DDException, Node]] =
    State { ctx =>
      if (p == TRUE || p == FALSE) (ctx, Right(FALSE))
      else if (p == q) (ctx, Right(TRUE))
      else if (q == TRUE) (ctx, Right(p))
      else if (isBigger(q.label, p.label, ctx.ddCtx)) (ctx, Right(FALSE))
      else {
        ctx.divCache.get((p, q)) match {
          case Some(v) => (ctx, v)
          case None =>
            if (isBigger(p.label, q.label, ctx.ddCtx)) {
              (for {
                p0       <- getLow(p, ctx.ddCtx)
                p1       <- getHigh(p, ctx.ddCtx)
                (c1, d1n) = zdd_div(p0, q).run(ctx).value
                d1       <- d1n
                (c2, d2n) = zdd_div(p1, q).run(c1).value
                d2       <- d2n
                (c3, n)   = getNode(p.label, d2, d1).run(c2).value
              } yield (c3, n)) match
                case Left(e)       => (ctx, Left(e))
                case Right((c, n)) => (c.copy(divCache = c.divCache + ((p, q) -> Right(n))), Right(n))
            } else {
              (for {
                p1       <- getHigh(p, ctx.ddCtx)
                q1       <- getHigh(q, ctx.ddCtx)
                p0       <- getLow(p, ctx.ddCtx)
                q0       <- getLow(q, ctx.ddCtx)
                (c1, d1n) = zdd_div(p1, q1).run(ctx).value
                d1       <- d1n
              } yield (c1, d1, p0, q0)) match
                case Left(e) => (ctx, Left(e))
                case Right((c, di, plo, qlo)) =>
                  if (di != FALSE && qlo != FALSE) {
                    val (cd, res) = zdd_div(plo, qlo).run(c).value
                    (for {
                      r       <- res
                      (ci, in) = zdd_intersection(di, r).run(cd).value
                      i       <- in
                    } yield (ci, i)) match
                      case Left(e)       => (ctx, Left(e))
                      case Right((c, n)) => (c.copy(divCache = c.divCache + ((p, q) -> Right(n))), Right(n))
                  } else {
                    (c.copy(divCache = c.divCache + ((p, q) -> Right(di))), Right(di))
                  }
            }
        }
      }
    }

  private[obdd] def zdd_remainder(p: Node, q: Node): State[ZDDContext, Either[DDException, Node]] =
    State { ctx =>
      val (cd, dn) = zdd_div(p, q).run(ctx).value
      (for {
        d       <- dn
        (cm, mn) = zdd_mul(q, d).run(cd).value
        m       <- mn
        (c, din) = zdd_difference(p, m).run(cm).value
        di      <- din
      } yield (c, di)) match
        case Left(value)   => (ctx, Left(value))
        case Right((c, n)) => (c, Right(n))
    }

  private[obdd] def zdd_count(n: Node, ctx: ZDDContext): Either[DDException, Int] =
    if (n == FALSE) Right(0)
    else if (n == TRUE) Right(1)
    else
      for {
        hi <- getHigh(n, ctx.ddCtx)
        ch <- zdd_count(hi, ctx)
        lo <- getLow(n, ctx.ddCtx)
        cl <- zdd_count(lo, ctx)
      } yield ch + cl

  private def zdd_ids(n: Node, ctx: ZDDContext): Set[Int] = {
    if (n == FALSE || n == TRUE) Set(n.id)
    (getHigh(n, ctx.ddCtx), getLow(n, ctx.ddCtx)) match
      case (Right(hi), Right(lo)) => Set(n.id) ++ zdd_ids(hi, ctx) ++ zdd_ids(lo, ctx)
      case _                      => Set()
  }

  private[obdd] def zdd_size(n: Node, ctx: ZDDContext): Int =
    zdd_ids(n, ctx).size

  private[obdd] def createNode(v: Var, hi: Int, lo: Int): State[ZDDContext, Int] =
    State { ctx =>
      if (hi == -2) (ctx, lo)
      else {
        ctx.ddCtx.nodes.get((v, hi, lo)) match
          case Some(i) => (ctx, i)
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

  private[obdd] def getNode(v: Var, hi: Node, lo: Node): State[ZDDContext, Node] =
    State { ctx =>
      val (c, n) = createNode(v, hi.id, lo.id).run(ctx).value
      (c, c.ddCtx.nodes.find(_._2 == n).map(entryToNode).get)
    }

}
