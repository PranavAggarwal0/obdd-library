package obddlib.OBDDHttpApp.obdd

import org.http4s.EntityDecoder
import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.circe.*

case class UnaryRequest(formula: String, ordering: Seq[String])
case class BinaryRequest(
  formula1: String,
  formula2: String,
  ordering: Seq[String]
)
case class DimacsRequest(formula: String, vars: Int)

implicit val unary_decoder: EntityDecoder[IO, UnaryRequest] =
  jsonOf[IO, UnaryRequest]
implicit val binary_decoder: EntityDecoder[IO, BinaryRequest] =
  jsonOf[IO, BinaryRequest]
implicit val dimacs_decoder: EntityDecoder[IO, DimacsRequest] =
  jsonOf[IO, DimacsRequest]
