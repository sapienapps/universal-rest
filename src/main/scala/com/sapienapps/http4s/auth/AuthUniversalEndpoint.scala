package com.sapienapps.http4s.auth

import cats.effect.Async
import cats.implicits._
import com.sapienapps.http4s.{CrudEndpoint, ErrorHandler, ServiceEffects}
import io.circe.Encoder
import org.http4s.{EntityDecoder, HttpRoutes}
import org.log4s.{Logger, getLogger}
import tsec.authentication._

import scala.util.Try

case class AuthUniversalEndpoint[F[_] : Async, Auth, K, T, Error, Params, User, SessionType]
(toParams: (SecuredRequest[F, User, Auth]) => Either[String, Map[Params, _]],
 toSession: (Map[Params, _], User) => SessionType,
 errorHandler: ErrorHandler[F, Error],
 toId: (String) => K)
(implicit ed: EntityDecoder[F, T], encoder: Encoder[T])
  extends CrudEndpoint[F, K, T, AuthEndpoint[F, User, Auth], Error, Params, SessionType]
    with ServiceEffects[F] {

  private val log: Logger = getLogger

  def create(service: Service): AuthEndpoint[F, User, Auth] = {
    case req@POST -> Root asAuthed _ =>
      Try {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            jsonRequest[T](req.request, e => {
              implicit val session: SessionType = toSession(params, req.identity)
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

  def get(service: Service): AuthEndpoint[F, User, Auth] = {
    case req@GET -> Root / id asAuthed _ =>
      Try {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: SessionType = toSession(params, req.identity)
            service.get(toId(id)).value.flatMap(f => jsonResponse(f, errorHandler))
        }
      }.toEither match {
        case Left(err) =>
          log.error(err)("Unhandled Error")
          BadRequest("Unknown Error")
        case Right(r) => r
      }
  }

  def list(service: Service): AuthEndpoint[F, User, Auth] = {
    case req@GET -> Root asAuthed _ =>
      Try {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: SessionType = toSession(params, req.identity)
            service.list().value.flatMap(f => jsonResponse(f, errorHandler))
        }
      }.toEither match {
        case Left(err) =>
          log.error(err)("Unhandled Error")
          BadRequest("Unknown Error")
        case Right(r) => r
      }
  }

  def update(service: Service): AuthEndpoint[F, User, Auth] = {
    case req@PUT -> Root asAuthed _ =>
      Try {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            jsonRequest[T](req.request, e => {
              implicit val session: SessionType = toSession(params, req.identity)
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

  def delete(service: Service): AuthEndpoint[F, User, Auth] = {
    case req@DELETE -> Root / id asAuthed _ =>
      Try {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: SessionType = toSession(params, req.identity)
            service.delete(toId(id)).value.flatMap(f => jsonResponse(f, errorHandler))
        }
      }.toEither match {
        case Left(err) =>
          log.error(err)("Unhandled Error")
          BadRequest("Unknown Error")
        case Right(r) => r
      }
  }

  def count(service: Service): AuthEndpoint[F, User, Auth] = {
    case req@GET -> Root / "count" asAuthed _ =>
      Try {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: SessionType = toSession(params, req.identity)
            service.size.value.flatMap(f => jsonResponse(f, errorHandler))
        }
      }.toEither match {
        case Left(err) =>
          log.error(err)("Unhandled Error")
          BadRequest("Unknown Error")
        case Right(r) => r
      }
  }

  def endpoints(service: Service,
                auth: SecuredRequestHandler[F, String, User, Auth]): HttpRoutes[F] = {
    val authEndpoints: AuthService[F, User, Auth] = {
      val end = {
        count(service) orElse
          create(service) orElse
          get(service) orElse
          list(service) orElse
          update(service) orElse
          delete(service)
      }
      TSecAuthService(end)
    }
    auth.liftService(authEndpoints)
  }

  case class Count(count: Int)
}
