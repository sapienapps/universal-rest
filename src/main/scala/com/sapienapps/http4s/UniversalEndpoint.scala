package com.sapienapps.http4s

import cats.effect.Async
import cats.implicits._
import io.circe.Encoder
import org.http4s.{EntityDecoder, HttpRoutes, Request}
import org.log4s.{Logger, getLogger}

import scala.util.Try

case class UniversalEndpoint[F[_] : Async, K, T, Error, Params, SessionType]
(toParams: (Request[F]) => Either[String, Map[Params, _]],
 toSession: (Map[Params, _]) => SessionType,
 errorHandler: ErrorHandler[F, Error],
 toId: (String) => K)
(implicit ed: EntityDecoder[F, T], encoder: Encoder[T])
  extends CrudEndpoint[F, K, T, HttpRoutes[F], Error, Params, SessionType]
    with ServiceEffects[F] {

  private val log: Logger = getLogger

  def create(service: Service): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@POST -> Root =>
        Try {
          toParams(req) match {
            case Left(e) => BadRequest(e)
            case Right(params) =>
              jsonRequest[T](req, e => {
                implicit val session: SessionType = toSession(params)
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
  }

  def get(service: Service): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@GET -> Root / id =>
        Try {
          toParams(req) match {
            case Left(e) => BadRequest(e)
            case Right(params) =>
              implicit val session: SessionType = toSession(params)
              service.get(toId(id)).value.flatMap(f => jsonResponse(f, errorHandler))
          }
        }.toEither match {
          case Left(err) =>
            log.error(err)("Unhandled Error")
            BadRequest("Unknown Error")
          case Right(r) => r
        }
    }
  }

  def list(service: Service): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@GET -> Root =>
        Try {
          toParams(req) match {
            case Left(e) => BadRequest(e)
            case Right(params) =>
              implicit val session: SessionType = toSession(params)
              service.list().value.flatMap(f => jsonResponse(f, errorHandler))
          }
        }.toEither match {
          case Left(err) =>
            log.error(err)("Unhandled Error")
            BadRequest("Unknown Error")
          case Right(r) => r
        }
    }
  }

  def update(service: Service): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@PUT -> Root =>
        Try {
          toParams(req) match {
            case Left(e) => BadRequest(e)
            case Right(params) =>
              jsonRequest[T](req, e => {
                implicit val session: SessionType = toSession(params)
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
  }

  def delete(service: Service): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@DELETE -> Root / id =>
        Try {
          toParams(req) match {
            case Left(e) => BadRequest(e)
            case Right(params) =>
              implicit val session: SessionType = toSession(params)
              service.delete(toId(id)).value.flatMap(f => jsonResponse(f, errorHandler))
          }
        }.toEither match {
          case Left(err) =>
            log.error(err)("Unhandled Error")
            BadRequest("Unknown Error")
          case Right(r) => r
        }
    }
  }

  def count(service: Service): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@GET -> Root / "count" =>
        Try {
          toParams(req) match {
            case Left(e) => BadRequest(e)
            case Right(params) =>
              implicit val session: SessionType = toSession(params)
              service.size.value.flatMap(f => jsonResponse(f, errorHandler))
          }
        }.toEither match {
          case Left(err) =>
            log.error(err)("Unhandled Error")
            BadRequest("Unknown Error")
          case Right(r) => r
        }
    }
  }

  def endpoints(service: Service): HttpRoutes[F] = {
    count(service) <+>
      create(service) <+>
      get(service) <+>
      list(service) <+>
      update(service) <+>
      delete(service)
  }

  case class Count(count: Int)

}

/*
object StringUniversalEndpoint {

  def endpoints[F[_] : Sync, T](service: CrudService[F, String, T, Any, Any, Any])
                               (implicit ed: EntityDecoder[F, T], e: Encoder[T]): HttpRoutes[F] =
    new StringUniversalEndpoint[F, T](ed, e).endpoints(service)
}
*/
