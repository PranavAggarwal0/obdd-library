package obddlib.OBDDHttpApp.obdd

import org.http4s.EntityDecoder
import cats.effect.IO
import io.circe.generic.auto._
import org.http4s.circe.*

case class GlobalUnaryRequest(formula: String)

case class GlobalUnaryRequestId(id: Int)

case class GlobalBinaryRequest(id1: Int, id2: Int)

implicit val gUnary_decoder: EntityDecoder[IO, GlobalUnaryRequest] =
  jsonOf[IO, GlobalUnaryRequest]
implicit val gUnaryID_decoder: EntityDecoder[IO, GlobalUnaryRequestId] =
  jsonOf[IO, GlobalUnaryRequestId]
implicit val gBinary_decoder: EntityDecoder[IO, GlobalBinaryRequest] =
  jsonOf[IO, GlobalBinaryRequest]
