package obddlib.OBDDHttpApp.obdd

import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import obdd.api.OBDDLib.*
import org.http4s.circe.CirceEntityEncoder.circeEntityEncoder
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType

object StepRoutes {

  def ObddStepService =
    HttpRoutes
      .of[IO] {
        case req @ POST -> Root / "conjunction" / "step" =>
          req.as[BinaryRequest].flatMap { conReq =>
            val obdd = newOBDD(conReq.ordering)
            val (c1, n1) = getNodeByExpression(conReq.formula1).run(obdd).value
            val (c2, n2) = getNodeByExpression(conReq.formula2).run(c1).value
            (for {
              node1 <- n1
              node2 <- n2
              (c3, con_node) = conjunction_step(node1, node2).run(c2).value
            } yield con_node) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }
        case req @ POST -> Root / "disjunction" / "step" =>
          req.as[BinaryRequest].flatMap { disReq =>
            val obdd = newOBDD(disReq.ordering)
            val (c1, n1) = getNodeByExpression(disReq.formula1).run(obdd).value
            val (c2, n2) = getNodeByExpression(disReq.formula2).run(c1).value
            (for {
              node1 <- n1
              node2 <- n2
              (c3, dis_node) = disjunction_step(node1, node2).run(c2).value
            } yield dis_node) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }
        case req @ POST -> Root / "negation" / "step" =>
          req.as[UnaryRequest].flatMap { negReq =>
            val obdd = newOBDD(negReq.ordering)
            val (c, n) = getNodeByExpression(negReq.formula).run(obdd).value
            (for {
              node <- n
              (ctx, neg) = negation_step(node).run(c).value
            } yield neg) match
              case Left(value) => InternalServerError(value.getMessage())
              case Right(value) =>
                Ok(value).map(
                  _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
                )
          }
        case req @ POST -> Root / "sifting" / "step" => {
          req.as[UnaryRequest].flatMap { sr =>
            val sBDD = newOBDD(sr.ordering)
            val (c, n) = getNodeByExpression(sr.formula).run(sBDD).value
            (for {
              node <- n
            } yield sifting_reorder_step(node).runA(c).value) match
              case Left(value)  => InternalServerError(value.getMessage())
              case Right(value) => Ok(value)
          }
        }
      }
}
