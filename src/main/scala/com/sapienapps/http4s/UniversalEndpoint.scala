package com.sapienapps.http4s

import cats.effect.Sync
import cats.implicits._
import io.circe.Encoder
import org.http4s.{EntityDecoder, HttpRoutes, Request}
import com.sapienapps.http4s.session.Session

case class UniversalEndpoint[F[_] : Sync, K, T, Error, Params, U]
(paramMapper: (Request[F]) => Either[String, Map[Params, _]],
 id: (String) => K)
(implicit ed: EntityDecoder[F, T], encoder: Encoder[T])
  extends CrudEndpoint[F, K, T, HttpRoutes[F], Error, Params, U]
    with ServiceEffects[F] {

  def toParamMap(request: Request[F]): Either[String, Map[Params, _]] = paramMapper(request)

  def toId(request: String): K = id(request)

  def create(service: Service): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@POST -> Root =>
        toParamMap(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            jsonRequest[T](req, e => {
              implicit val session: Session[Params, U] = Session(params)
              service.create(e).value.flatMap(f => jsonResponse(f))
            })
        }
    }
  }

  def get(service: Service): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@GET -> Root / id =>
        toParamMap(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: Session[Params, U] = Session(params)
            service.get(toId(id)).value.flatMap(f => jsonResponse(f))
        }
    }
  }

  def list(service: Service): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@GET -> Root =>
        toParamMap(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: Session[Params, U] = Session(params)
            service.list().value.flatMap(f => jsonResponse(f))
        }
    }
  }

  def update(service: Service): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@PUT -> Root =>
        toParamMap(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            jsonRequest[T](req, e => {
              implicit val session: Session[Params, U] = Session(params)
              service.update(e).value.flatMap(f => jsonResponse(f))
            })
        }
    }
  }

  def delete(service: Service): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@DELETE -> Root / id =>
        toParamMap(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: Session[Params, U] = Session(params)
            service.delete(toId(id)).value.flatMap(f => jsonResponse(f))
        }
    }
  }

  def count(service: Service): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case req@GET -> Root / "count" =>
        toParamMap(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: Session[Params, U] = Session(params)
            service.size.value.flatMap(f => jsonResponse(f))
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
