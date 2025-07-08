package obddlib.OBDDHttpApp.obdd

import obdd.api.OBDDLib.*
import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType

object GlobalRoutes {

  def ObddGlobalService =
    HttpRoutes
      .of[IO] {

        case req @ POST -> Root / "global" / "build" =>
          req.as[GlobalUnaryRequest].flatMap { r =>
            val (c, n) = getNodeByExpression(r.formula).run(obdd).value
            obdd = c
            (for {
              node <- n
            } yield visualise_global_DAG(
              ids = Map(node.id -> "red"),
              showID = true,
              ctx = c
            )) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }

        case req @ POST -> Root / "global" / "conjunction" =>
          req.as[GlobalBinaryRequest].flatMap { conReq =>
            val res = (for {
              node1 <- getNodeByID(conReq.id1, obdd)
              node2 <- getNodeByID(conReq.id2, obdd)
              (ct, con_node) = conjunction(node1, node2).run(obdd).value
              con <- con_node
            } yield (
              ct,
              visualise_global_DAG(
                ids = Map(con.id -> "red"),
                showID = true,
                ctx = ct
              )
            ))
            res match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                obdd = value._1
                Ok(value._2).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }
        case req @ POST -> Root / "global" / "disjunction" =>
          req.as[GlobalBinaryRequest].flatMap { disReq =>
            val res = (for {
              node1 <- getNodeByID(disReq.id1, obdd)
              node2 <- getNodeByID(disReq.id2, obdd)
              (ct, dis_node) = disjunction(node1, node2).run(obdd).value
              dis <- dis_node
            } yield (
              ct,
              visualise_global_DAG(
                ids = Map(dis.id -> "red"),
                showID = true,
                ctx = ct
              )
            ))
            res match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                obdd = value._1
                Ok(value._2).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }
        case req @ POST -> Root / "global" / "negation" =>
          req.as[GlobalUnaryRequestId].flatMap { negReq =>
            (for {
              node <- getNodeByID(negReq.id, obdd)
              (c, neg_node) = negation(node).run(obdd).value
              neg <- neg_node
            } yield visualise_global_DAG(
              ids = Map(neg.id -> "red"),
              showID = true,
              ctx = c
            )) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }

      }

  private var obdd = newOBDD(Seq.range(1, 500).map(i => s"x$i"))

}
