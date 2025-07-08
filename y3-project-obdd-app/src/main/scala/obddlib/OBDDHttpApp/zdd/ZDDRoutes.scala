package obddlib.OBDDHttpApp.zdd

import obdd.api.ZDDLib.*
import cats.effect._
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.headers.`Content-Type`
import org.http4s.MediaType

object ZDDRoutes {

  def ZDDService = HttpRoutes
    .of[IO] {
      case req @ POST -> Root / "zdd" / "build" => {
        req.as[SetsRequest].flatMap { sr =>
          def ctx = newZDD(sr.ordering)
          val (c, n) = family(sr.sets).run(ctx).value
          (for {
            node <- n
            diagram = getDiagramByNode(node, c)
          } yield visualiseDiagram(diagram)) match
            case Left(value) => InternalServerError(value.getMessage())
            case Right(value) =>
              Ok(value).map(
                _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
              )
        }
      }
      case req @ POST -> Root / "zdd" / "union" => {
        req.as[BinaryRequest].flatMap { biReq =>
          def ctx = newZDD(biReq.ordering)
          val (c1, n1) = family(biReq.zdd1).run(ctx).value
          (for {
            node1 <- n1
            (c2, n2) = family(biReq.zdd2).run(c1).value
            node2 <- n2
            (c3, u) = union(node1, node2).run(c2).value
            union <- u
            diagram = getDiagramByNode(union, c3)
          } yield visualiseDiagram(diagram)) match
            case Left(value) => InternalServerError(value.getMessage())
            case Right(value) =>
              Ok(value).map(
                _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
              )
        }
      }
      case req @ POST -> Root / "zdd" / "intersection" => {
        req.as[BinaryRequest].flatMap { biReq =>
          def ctx = newZDD(biReq.ordering)
          val (c1, n1) = family(biReq.zdd1).run(ctx).value
          (for {
            node1 <- n1
            (c2, n2) = family(biReq.zdd2).run(c1).value
            node2 <- n2
            (c3, i) = intersection(node1, node2).run(c2).value
            intersection <- i
            diagram = getDiagramByNode(intersection, c3)
          } yield visualiseDiagram(diagram)) match
            case Left(value) => InternalServerError(value.getMessage())
            case Right(value) =>
              Ok(value).map(
                _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
              )
        }
      }
      case req @ POST -> Root / "zdd" / "difference" => {
        req.as[BinaryRequest].flatMap { biReq =>
          def ctx = newZDD(biReq.ordering)
          val (c1, n1) = family(biReq.zdd1).run(ctx).value
          (for {
            node1 <- n1
            (c2, n2) = family(biReq.zdd2).run(c1).value
            node2 <- n2
            (c3, d) = difference(node1, node2).run(c2).value
            diff <- d
            diagram = getDiagramByNode(diff, c3)
          } yield visualiseDiagram(diagram)) match
            case Left(value) => InternalServerError(value.getMessage())
            case Right(value) =>
              Ok(value).map(
                _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
              )
        }
      }
      case req @ POST -> Root / "zdd" / "multiply" => {
        req.as[BinaryRequest].flatMap { biReq =>
          def ctx = newZDD(biReq.ordering)
          val (c1, n1) = family(biReq.zdd1).run(ctx).value
          (for {
            node1 <- n1
            (c2, n2) = family(biReq.zdd2).run(c1).value
            node2 <- n2
            (c3, m) = mul(node1, node2).run(c2).value
            mul <- m
            diagram = getDiagramByNode(mul, c3)
          } yield visualiseDiagram(diagram)) match
            case Left(value) => InternalServerError(value.getMessage())
            case Right(value) =>
              Ok(value).map(
                _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
              )
        }
      }
      case req @ POST -> Root / "zdd" / "divide" => {
        req.as[BinaryRequest].flatMap { biReq =>
          def ctx = newZDD(biReq.ordering)
          val (c1, n1) = family(biReq.zdd1).run(ctx).value
          (for {
            node1 <- n1
            (c2, n2) = family(biReq.zdd2).run(c1).value
            node2 <- n2
            (c3, d) = div(node1, node2).run(c2).value
            div <- d
            diagram = getDiagramByNode(div, c3)
          } yield visualiseDiagram(diagram)) match
            case Left(value) => InternalServerError(value.getMessage())
            case Right(value) =>
              Ok(value).map(
                _.withContentType(`Content-Type`(MediaType.image.`svg+xml`))
              )
        }
      }
    }

}
