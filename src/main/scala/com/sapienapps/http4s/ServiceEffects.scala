package com.sapienapps.http4s

import cats.implicits._
import cats.{data, Applicative, Monad, MonadError}
import io.circe.Encoder
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, ParseFailure, Request, Response}
import org.log4s.{getLogger, Logger}

import scala.util.Try

trait ServiceEffects[F[_]] extends Http4sDsl[F] {

  private val log: Logger = getLogger

  /**
   * Wraps route logic in Try/catch to handle unexpected exceptions gracefully.
   * Logs errors and returns BadRequest for unhandled exceptions.
   *
   * @param body - route logic that returns F[Response[F]]
   * @param F    - Applicative for creating error response
   * @return F[Response[F]]
   */
  def safeRoute(body: => F[Response[F]])(implicit F: Applicative[F]): F[Response[F]] =
    Try(body).toEither match {
      case Left(err) =>
        log.error(err)("Unhandled Error")
        BadRequest("Unknown Error")
      case Right(r) => r
    }

  /**
   * App Request Choke point for JSON Requests which handles decoding error condition
   *
   * @param request - request object
   * @param fun     - decoded response handler for success handling
   * @param decoder - implicit decoder for entity
   * @param F       - Future Request Type
   * @tparam T - Entity Type
   * @return
   */
  def jsonRequest[T](
    request: Request[F],
    fun: T => F[Response[F]],
  )(implicit decoder: EntityDecoder[F, T], F: MonadError[F, Throwable]): F[Response[F]] =
    request
      .as[T]
      .attempt
      .flatMap {
        case Right(saved) => fun(saved)
        case Left(e)      =>
          log.error(e)("Request Issue")
          BadRequest(e.toString)
      }

  /**
   * App Response Choke point for capturing the handling -> HTTP error JSON responses based on the ValidationError
   *
   * @param either  - Either[ValidationError, T]
   * @param encoder - Encoder for Encoding T to JSON
   * @param m       - Monad
   * @tparam T - Entity Type
   * @return
   */
  def jsonResponse[E, T](either: Either[E, T], errorHandler: ErrorHandler[F, E])(implicit
    encoder: Encoder[T],
    m: Monad[F],
  ): F[Response[F]] =
    either match {
      case Right(saved) =>
        saved match {
          case Right(_) =>
            log.error("Improper Attempt/Handler Used, this is due to an extra wrapping of Either")
            InternalServerError("Improper Attempt/Handler Used")
          case _ => Ok(saved.asJson)
        }
      case Left(e) =>
        errorHandler.handle(e)
    }

  def handleOptionalParam[D, T](
    option: Option[data.ValidatedNel[ParseFailure, D]],
    noneFun: () => T,
    someFun: (D) => T,
  )(implicit encoder: Encoder[T], app: Applicative[F]): F[Response[F]] =
    option match {
      case None        => Ok(noneFun().asJson)
      case Some(param) =>
        param.fold(
          err => {
            err.map(e => log.warn(e.message))
            BadRequest("Unable to Parse parameter")
          },
          id => Ok(someFun(id).asJson),
        )
    }

}
