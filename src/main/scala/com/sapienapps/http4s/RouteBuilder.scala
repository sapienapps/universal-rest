package com.sapienapps.http4s

import cats.effect.Async
import com.sapienapps.http4s.auth.AuthUniversalEndpoint
import com.sapienapps.http4s.open.UniversalEndpoint
import io.circe.Encoder
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.CORS
import org.http4s.{AuthedRequest, EntityDecoder, HttpRoutes, Request}

case class RouteBuilder[F[_]: Async, K, T, Error, Context, RouteName](
  route: RouteName,
  repo: CrudRepository[F, K, T, Error, Context],
)(implicit ed: EntityDecoder[F, T], encoder: Encoder[T]) {

  def build[ParamNames, ParamValue](
    paramMapper: AuthedRequest[F, Context] => Either[String, Map[ParamNames, ParamValue]],
    toContext: (Map[ParamNames, ParamValue], Context) => Context,
    errorHandler: ErrorHandler[F, Error],
    toId: String => K,
  )(implicit auth: AuthMiddleware[F, Context]): HttpRoutes[F] =
    CORS.policy(
      AuthUniversalEndpoint[F, K, T, Error, ParamNames, ParamValue, Context](
        paramMapper,
        toContext,
        errorHandler,
        toId,
      ).endpoints(UniversalService(repo), auth),
    )

  def build[ParamNames, ParamValue](
    paramMapper: Request[F] => Either[String, Map[ParamNames, ParamValue]],
    toSession: Map[ParamNames, ParamValue] => Context,
    errorHandler: ErrorHandler[F, Error],
    toId: String => K,
  ): HttpRoutes[F] =
    CORS.policy(
      UniversalEndpoint[F, K, T, Error, ParamNames, ParamValue, Context](
        paramMapper,
        toSession,
        errorHandler,
        toId,
      ).endpoints(UniversalService(repo)),
    )

}
