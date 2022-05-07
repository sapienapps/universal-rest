package com.sapienapps.http4s.authV3

import cats.effect.Async
import cats.implicits._
import com.sapienapps.http4s.{CrudEndpoint, ErrorHandler, ServiceEffects}
import io.circe.Encoder
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRequest, AuthedRoutes, EntityDecoder, HttpRoutes}
import org.log4s.{Logger, getLogger}

import scala.util.Try

/**
 * @param toParams - Param KV function
 * @param toSession - Kept for Backwards compatibility will be removed in V4
 * @param errorHandler - Error Function
 * @param toId - Id Function
 * @param async$F$0 - Effect
 * @param ed - Entity Decoder
 * @param encoder - Entity Encoder
 * @tparam F
 * @tparam Auth
 * @tparam K
 * @tparam T
 * @tparam Error
 * @tparam ParamName
 * @tparam ParamValue
 * @tparam Context
 */
case class AuthUniversalEndpointV3[F[_] : Async, Auth, K, T, Error, ParamName, ParamValue, Context]
(toParams: AuthedRequest[F, Context] => Either[String, Map[ParamName, ParamValue]],
 toSession: (Map[ParamName, ParamValue], Context) => Context,
 errorHandler: ErrorHandler[F, Error],
 toId: String => K)
(implicit ed: EntityDecoder[F, T], encoder: Encoder[T])
  extends CrudEndpoint[F, K, T, AuthEndpointV3[F, Context], Error, Map[ParamName, ParamValue], Context]
    with ServiceEffects[F] {

  private val log: Logger = getLogger

  def create(service: Service): AuthEndpointV3[F, Context] = {
    case req@POST -> Root asAuthed _ =>
      Try {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            jsonRequest[T](req.req, e => {
              implicit val session: Context = toSession(params, req.context)
              service.create(e).value.flatMap(f => jsonResponse(f, errorHandler))
            })
        }
      }.toEither match {
        case Left(err) =>
          log.error(err)("Unhandled Error")
          BadRequest("Unknown Error")
        case Right(r) => r
      }
  }

  def get(service: Service): AuthEndpointV3[F, Context] = {
    case req@GET -> Root / id asAuthed _ =>
      Try {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: Context = toSession(params, req.context)
            service.get(toId(id)).value.flatMap(f => jsonResponse(f, errorHandler))
        }
      }.toEither match {
        case Left(err) =>
          log.error(err)("Unhandled Error")
          BadRequest("Unknown Error")
        case Right(r) => r
      }
  }

  def list(service: Service): AuthEndpointV3[F, Context] = {
    case req@GET -> Root asAuthed _ =>
      Try {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: Context = toSession(params, req.context)
            service.list().value.flatMap(f => jsonResponse(f, errorHandler))
        }
      }.toEither match {
        case Left(err) =>
          log.error(err)("Unhandled Error")
          BadRequest("Unknown Error")
        case Right(r) => r
      }
  }

  def update(service: Service): AuthEndpointV3[F, Context] = {
    case req@PUT -> Root asAuthed _ =>
      Try {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            jsonRequest[T](req.req, e => {
              implicit val session: Context = toSession(params, req.context)
              service.update(e).value.flatMap(f => jsonResponse(f, errorHandler))
            })
        }
      }.toEither match {
        case Left(err) =>
          log.error(err)("Unhandled Error")
          BadRequest("Unknown Error")
        case Right(r) => r
      }
  }

  def delete(service: Service): AuthEndpointV3[F, Context] = {
    case req@DELETE -> Root / id asAuthed _ =>
      Try {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: Context = toSession(params, req.context)
            service.delete(toId(id)).value.flatMap(f => jsonResponse(f, errorHandler))
        }
      }.toEither match {
        case Left(err) =>
          log.error(err)("Unhandled Error")
          BadRequest("Unknown Error")
        case Right(r) => r
      }
  }

  def count(service: Service): AuthEndpointV3[F, Context] = {
    case req@GET -> Root / "count" asAuthed _ =>
      Try {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: Context = toSession(params, req.context)
            service.size().value.flatMap(f => jsonResponse(f, errorHandler))
        }
      }.toEither match {
        case Left(err) =>
          log.error(err)("Unhandled Error")
          BadRequest("Unknown Error")
        case Right(r) => r
      }
  }

  def endpoints(service: Service,
                auth: AuthMiddleware[F, Context]): HttpRoutes[F] = {
    val routes = AuthedRoutes.of {
      val end = {
        count(service) orElse
          create(service) orElse
          get(service) orElse
          list(service) orElse
          update(service) orElse
          delete(service)
      }
      end
    }
    auth(routes)
  }

  case class Count(count: Int)
}
