package com.sapienapps.http4s

import cats.effect.{Async, Temporal}
import com.sapienapps.http4s.authV3.{AuthUniversalEndpointV3, authUserV3Test, customFailure}
import com.sapienapps.http4s.test.{TestErrorHandler, TestRepo}
import fs2.Stream
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.implicits._
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRequest, EntityDecoder, EntityEncoder, HttpRoutes, Request}

case class Server() {

  def stream[F[_] : Async](endpoints: List[(String, HttpRoutes[F])])
                          (implicit T: Temporal[F]): Stream[F, Nothing] = {
    implicit val entityDecoder: EntityDecoder[F, String] = jsonOf
    implicit val errorEncoder: EntityEncoder[F, String] = jsonEncoderOf

    val toParams = (_: Request[F]) => Right(Map[String, Any]())
    val toSession = (_: Map[String, Any]) => "Session"
    val toId = (id: String) => Integer.parseInt(id)

    val toParams2 = (_: AuthedRequest[F, String]) => Right(Map[String, Any]())
    val toSession2 = (_: Map[String, Any], _: String) => "Session"

    val middleware = AuthMiddleware.noSpider(authUserV3Test, customFailure)

    val endpoint = List(
      "test" -> UniversalEndpoint(
        toParams,
        toSession,
        TestErrorHandler[F](),
        toId).endpoints(UniversalService(TestRepo[F, String]())),
      "test2" -> AuthUniversalEndpointV3(
        toParams2,
        toSession2,
        TestErrorHandler[F](),
        toId).endpoints(UniversalService(TestRepo[F, String]()), middleware)
    )

    for {
      _ <- BlazeClientBuilder[F].stream

      // Base Routes:
      router = Router(
        endpoint: _*
      ).orNotFound

      server <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(router)
        .serve
    } yield server
  }.drain
}
