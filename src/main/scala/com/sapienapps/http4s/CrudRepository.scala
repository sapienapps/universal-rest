package com.sapienapps.http4s

import cats.Applicative
import cats.data.EitherT

trait CrudRepository[F[_], K, T, Error, SessionType] {

  def create(entity: T)(implicit session: SessionType): EitherT[F, Error, T]

  def get(id: K)(implicit session: SessionType): EitherT[F, Error, T]

  def delete(entityId: K)(implicit session: SessionType): EitherT[F, Error, T]

  def update(entity: T)(implicit session: SessionType): EitherT[F, Error, T]

  /**
   * Core collection method. Override this in implementations to support pagination and filtering.
   * Default implementation delegates to the simpler collection(isCount) method.
   */
  @SuppressWarnings(Array("org.wartremover.warts.UnusedMethodParameter"))
  def collection(isCount: Boolean, params: QueryParams)(implicit
    session: SessionType,
  ): EitherT[F, Error, DataResult[T]] = {
    val _ = params // suppress unused warning - params available for override implementations
    collection(isCount)
  }

  def collection(isCount: Boolean)(implicit session: SessionType): EitherT[F, Error, DataResult[T]]

  def list(params: QueryParams)(implicit session: SessionType, F: Applicative[F]): EitherT[F, Error, Iterable[T]] =
    collection(isCount = false, params).map {
      case CountResult(_) => List[T]()
      case ItrResult(v)   => v
    }

  def list()(implicit session: SessionType, F: Applicative[F]): EitherT[F, Error, Iterable[T]] =
    list(QueryParams.empty)

  def size()(implicit session: SessionType, F: Applicative[F]): EitherT[F, Error, Int] =
    collection(isCount = true).map {
      case CountResult(v) => v
      case ItrResult(_)   => 0
    }

}
