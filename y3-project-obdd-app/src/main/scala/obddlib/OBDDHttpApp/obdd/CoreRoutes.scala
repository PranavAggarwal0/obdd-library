package obddlib.OBDDHttpApp.obdd

import obdd.api.OBDDLib.*
import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType
import cats.syntax.show

object CoreRoutes {

  def ObddCoreService =
    HttpRoutes
      .of[IO] {
        case req @ POST -> Root / "build" / "expression" =>
          req.as[UnaryRequest].flatMap { dr =>
            val obdd = newOBDD(dr.ordering)
            val (c1, n) = getNodeByExpression(dr.formula).run(obdd).value
            (for {
              node <- n
              diagram = getDiagramByNode(node, c1)
            } yield visualiseDiagram(diagram)) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }
        case req @ POST -> Root / "build" / "dimacs" =>
          req.as[DimacsRequest].flatMap { dr =>
            val obdd =
              newOBDD(Seq.range(1, dr.vars + 1).map(i => "x" + i.toString()))
            val (c1, n) = getNodeByDIMACS(dr.formula).run(obdd).value
            (for {
              node <- n
              diagram = getDiagramByNode(node, c1)
            } yield visualiseDiagram(diagram)) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }
        case req @ POST -> Root / "build" / "qdimacs" =>
          req.as[DimacsRequest].flatMap { dr =>
            val obdd =
              newOBDD(Seq.range(1, dr.vars + 1).map(i => "x" + i.toString()))
            val (c1, n) = getNodeByQDIMACS(dr.formula).run(obdd).value
            (for {
              node <- n
              diagram = getDiagramByNode(node, c1)
            } yield visualiseDiagram(diagram)) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }
        case req @ POST -> Root / "conjunction" =>
          req.as[BinaryRequest].flatMap { conReq =>
            val obdd = newOBDD(conReq.ordering)
            val (c1, n1) = getNodeByExpression(conReq.formula1).run(obdd).value
            val (c2, n2) = getNodeByExpression(conReq.formula2).run(c1).value
            (for {
              node1 <- n1
              node2 <- n2
              (c3, con_node) = conjunction(node1, node2).run(c2).value
              con <- con_node
            } yield visualise_global_DAG(
              ids =
                Map(node1.id -> "blue", node2.id -> "blue", con.id -> "red"),
              showID = false,
              ctx = c3
            )) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }
        case req @ POST -> Root / "disjunction" =>
          req.as[BinaryRequest].flatMap { disReq =>
            val obdd = newOBDD(disReq.ordering)
            val (c1, n1) = getNodeByExpression(disReq.formula1).run(obdd).value
            val (c2, n2) = getNodeByExpression(disReq.formula2).run(c1).value
            (for {
              node1 <- n1
              node2 <- n2
              (c3, dis_node) = disjunction(node1, node2).run(c2).value
              dis <- dis_node
            } yield visualise_global_DAG(
              ids =
                Map(node1.id -> "blue", node2.id -> "blue", dis.id -> "red"),
              showID = false,
              ctx = c3
            )) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }
        case req @ POST -> Root / "negation" =>
          req.as[UnaryRequest].flatMap { negReq =>
            val obdd = newOBDD(negReq.ordering)
            val (c, n) = getNodeByExpression(negReq.formula).run(obdd).value
            (for {
              node <- n
              (c1, neg_node) = negation(node).run(c).value
              neg <- neg_node
            } yield visualise_global_DAG(
              ids = Map(node.id -> "blue", neg.id -> "red"),
              showID = false,
              ctx = c1
            )) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }
        case req @ POST -> Root / "sifting" =>
          req.as[UnaryRequest].flatMap { req =>
            val obdd = newOBDD(req.ordering)
            val (ctx, n) = getNodeByExpression(req.formula).run(obdd).value
            (for {
              node <- n
              c = sifting_reorder(node).runS(ctx).value
            } yield visualiseDiagram(getDiagramByNode(node, c))) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }
        case req @ POST -> Root / "window" =>
          req.as[UnaryRequest].flatMap { req =>
            val obdd = newOBDD(req.ordering)
            val (ctx, n) = getNodeByExpression(req.formula).run(obdd).value
            (for {
              node <- n
              c = window_permutation_reorder(node).runS(ctx).value
            } yield visualiseDiagram(getDiagramByNode(node, c))) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }

      }

}
