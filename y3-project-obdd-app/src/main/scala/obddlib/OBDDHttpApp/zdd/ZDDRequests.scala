package obddlib.OBDDHttpApp.zdd

import org.http4s.EntityDecoder
import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.circe.*

case class SetsRequest(sets: Set[Set[String]], ordering: Seq[String])
case class BinaryRequest(
    zdd1: Set[Set[String]],
    zdd2: Set[Set[String]],
    ordering: Seq[String]
)

implicit val unary_decoder: EntityDecoder[IO, SetsRequest] =
  jsonOf[IO, SetsRequest]

implicit val binary_decoder: EntityDecoder[IO, BinaryRequest] =
  jsonOf[IO, BinaryRequest]
