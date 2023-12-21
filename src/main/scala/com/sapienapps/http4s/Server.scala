package com.sapienapps.http4s

import cats.effect.{Async, Temporal}
import com.comcast.ip4s.IpLiteralSyntax
import com.sapienapps.http4s.auth.{AuthUniversalEndpoint, authUserTest, customFailure}
import com.sapienapps.http4s.test.{TestErrorHandler, TestRepo}
import fs2.io.net.Network
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.{AuthMiddleware, Router}
import org.http4s.{AuthedRequest, EntityDecoder, EntityEncoder, HttpRoutes, Request}

case class Server() {

  def stream[F[_] : Async: Network](endpoints: List[(String, HttpRoutes[F])])
                          (implicit T: Temporal[F]) = {
    implicit val entityDecoder: EntityDecoder[F, String] = jsonOf
    implicit val errorEncoder: EntityEncoder[F, String] = jsonEncoderOf

    val toParams = (_: Request[F]) => Right(Map[String, Any]())
    val toSession = (_: Map[String, Any]) => "Session"
    val toId = (id: String) => Integer.parseInt(id)

    val toParams2 = (_: AuthedRequest[F, String]) => Right(Map[String, Any]())
    val toSession2 = (_: Map[String, Any], _: String) => "Session"

    val middleware = AuthMiddleware.noSpider(authUserTest, customFailure)

    val endpoint = List(
      "test" -> UniversalEndpoint(
        toParams,
        toSession,
        TestErrorHandler[F](),
        toId).endpoints(UniversalService(TestRepo[F, String]())),
      "test2" -> AuthUniversalEndpoint(
        toParams2,
        toSession2,
        TestErrorHandler[F](),
        toId).endpoints(UniversalService(TestRepo[F, String]()), middleware)
    )

    for {
      _ <- EmberClientBuilder.default[F].build

      // Base Routes:
      router = Router(
        endpoint: _*
      ).orNotFound

      server <- EmberServerBuilder
        .default[F]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(router)
        .build
    } yield server
  }
}
