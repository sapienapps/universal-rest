package com.sapienapps.http4s

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import fs2.Stream
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes, Request}

import scala.concurrent.ExecutionContext.global

case class Server() {

  def stream[F[_] : ConcurrentEffect](endpoints: List[(String, HttpRoutes[F])])
                                     (implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
    implicit val entityDecoder: EntityDecoder[F, String] = jsonOf
    implicit val errorEncoder: EntityEncoder[F, String] = jsonEncoderOf

    val toParams = (_: Request[F]) => Right(Map[String, Any]())
    val toSession = (_: Map[String, Any]) => "Session"
    val toId = (id: String) => Integer.parseInt(id)

    val endpoint = List(
      "test" -> UniversalEndpoint(toParams, toSession, toId).endpoints(UniversalService(TestRepo[F,String]()))
    )

    for {
      _ <- BlazeClientBuilder[F](global).stream

      // Base Routes:
      router = Router(
        endpoint: _*
      ).orNotFound

      server <- BlazeServerBuilder[F](global)
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(router)
        .serve
    } yield server
  }.drain

}
