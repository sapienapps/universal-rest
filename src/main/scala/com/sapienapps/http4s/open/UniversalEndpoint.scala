package com.sapienapps.http4s.open

import cats.effect.Async
import cats.implicits._
import com.sapienapps.http4s._
import io.circe.Encoder
import org.http4s.{EntityDecoder, HttpRoutes, Request}

case class UniversalEndpoint[F[_]: Async, K, T, Error, ParamName, ParamValue, SessionType](
  toParams: (Request[F]) => Either[String, Map[ParamName, ParamValue]],
  toSession: (Map[ParamName, ParamValue]) => SessionType,
  errorHandler: ErrorHandler[F, Error],
  toId: (String) => K,
)(implicit ed: EntityDecoder[F, T], encoder: Encoder[T])
    extends CrudEndpoint[F, K, T, HttpRoutes[F], Error, Map[ParamName, ParamValue], SessionType]
    with ServiceEffects[F] {

  private def extractQueryParams(req: Request[F]): QueryParams = {
    val params = req.uri.query.params
    QueryParams(
      limit = params.get("limit").flatMap(_.toIntOption),
      offset = params.get("offset").flatMap(_.toIntOption),
      filters = params - "limit" - "offset",
    )
  }

  def create(service: Service): HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ POST -> Root =>
      safeRoute {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            jsonRequest[T](
              req,
              e => {
                implicit val session: SessionType = toSession(params)
                service.create(e).value.flatMap(f => jsonResponse(f, errorHandler))
              },
            )
        }
      }
    }

  def get(service: Service): HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ GET -> Root / id =>
      safeRoute {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: SessionType = toSession(params)
            service.get(toId(id)).value.flatMap(f => jsonResponse(f, errorHandler))
        }
      }
    }

  def list(service: Service): HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ GET -> Root =>
      safeRoute {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: SessionType = toSession(params)
            val queryParams = extractQueryParams(req)
            service.list(queryParams).value.flatMap(f => jsonResponse(f, errorHandler))
        }
      }
    }

  def update(service: Service): HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ PUT -> Root =>
      safeRoute {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            jsonRequest[T](
              req,
              e => {
                implicit val session: SessionType = toSession(params)
                service.update(e).value.flatMap(f => jsonResponse(f, errorHandler))
              },
            )
        }
      }
    }

  def delete(service: Service): HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ DELETE -> Root / id =>
      safeRoute {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: SessionType = toSession(params)
            service.delete(toId(id)).value.flatMap(f => jsonResponse(f, errorHandler))
        }
      }
    }

  def count(service: Service): HttpRoutes[F] =
    HttpRoutes.of[F] { case req @ GET -> Root / "count" =>
      safeRoute {
        toParams(req) match {
          case Left(e) => BadRequest(e)
          case Right(params) =>
            implicit val session: SessionType = toSession(params)
            service.size().value.flatMap(f => jsonResponse(f, errorHandler))
        }
      }
    }

  def endpoints(service: Service): HttpRoutes[F] =
    count(service) <+>
      create(service) <+>
      get(service) <+>
      list(service) <+>
      update(service) <+>
      delete(service)

  /** Convenience method to create endpoints directly from a repository */
  def endpoints(repo: CrudRepository[F, K, T, Error, SessionType]): HttpRoutes[F] =
    endpoints(UniversalService(repo))

  case class Count(count: Int)

}
