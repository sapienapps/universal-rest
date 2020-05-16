package org.sapienapps.http4s

import cats.effect.{ConcurrentEffect, ContextShift, ExitCode, IO, IOApp, Timer}
import fs2.Stream
import org.http4s.HttpRoutes
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.server.Router
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext.global

case class Server() {

  def stream[F[_] : ConcurrentEffect](endpoints: List[(String, HttpRoutes[F])])
                                     (implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    for {
      _ <- BlazeClientBuilder[F](global).stream

      // Base Routes:
      router = Router(
        endpoints: _*
      ).orNotFound

      server <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(router)
        .serve
    } yield server
  }.drain

}
