package obddlib.OBDDHttpApp

import cats.effect._
import com.comcast.ip4s._
import org.http4s.ember.server._
import org.http4s.server.middleware._
import org.http4s.headers.Origin
import org.http4s.Uri
import obddlib.OBDDHttpApp.obdd.CoreRoutes.ObddCoreService
import obddlib.OBDDHttpApp.obdd.StepRoutes.ObddStepService
import obddlib.OBDDHttpApp.obdd.GlobalRoutes.ObddGlobalService
import obddlib.OBDDHttpApp.zdd.ZDDRoutes.ZDDService
import cats.implicits.*

object App extends IOApp {

  def dd_service =
    (ObddCoreService <+> ObddStepService <+> ObddGlobalService <+> ZDDService).orNotFound

  val corsObddService = CORS.policy
    .withAllowOriginHost(
      Set(Origin.Host(Uri.Scheme.http, Uri.RegName("localhost"), Some(5173)))
    )
    .apply(dd_service)

  def run(args: List[String]): IO[ExitCode] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(corsObddService)
      .build
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
