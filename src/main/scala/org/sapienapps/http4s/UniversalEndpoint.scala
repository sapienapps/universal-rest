package org.sapienapps.http4s

import cats.implicits._
import cats.{Defer, MonadError}
import io.circe.Encoder
import org.http4s.{EntityDecoder, HttpRoutes, Request}

trait UniversalEndpoint[F[_], K, T, Error, Params, U]
  extends CrudEndpoint[F, K, T, HttpRoutes[F], Error, Params, U]
    with ServiceEffects[F] {

  //implicit def M: Monad[F]

  implicit def A: App

  implicit def Error: MonadError[F, Throwable]

  implicit def entityDecoder: EntityDecoder[F, T]

  implicit def encoder: Encoder[T]

  implicit def defer: Defer[F]

  def toParamMap(request: Request[F]): Either[String, Map[Params, _]]

  def toId(request: String): K

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
