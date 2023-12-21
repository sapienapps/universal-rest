package com.sapienapps.http4s.auth

import cats.data.OptionT
import cats.effect.Async
import com.sapienapps.http4s.{CrudRepository, ErrorHandler, UniversalService}
import io.circe.Encoder
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.CORS
import org.http4s.{AuthedRequest, EntityDecoder, Http}

case class AuthUniversalBuilder[F[_]: Async, K, T, Error, Context, RouteName](
  route: RouteName,
  repo: CrudRepository[F, K, T, Error, Context],
)(implicit ed: EntityDecoder[F, T], encoder: Encoder[T]) {

  def build[ParamNames, ParamValue](
    paramMapper: AuthedRequest[F, Context] => Either[String, Map[ParamNames, ParamValue]],
    toContext: (Map[ParamNames, ParamValue], Context) => Context,
    errorHandler: ErrorHandler[F, Error],
    toId: String => K,
  )(implicit auth: AuthMiddleware[F, Context]): Http[OptionT[F, *], F] =
    CORS.policy(
      AuthUniversalEndpoint[F, K, T, Error, ParamNames, ParamValue, Context](
        paramMapper,
        toContext,
        errorHandler,
        toId,
      ).endpoints(UniversalService(repo), auth),
    )

}
