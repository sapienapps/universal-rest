package com.sapienapps.http4s

import cats.Applicative
import cats.data.EitherT

trait CrudRepository[F[_], K, T, Error, SessionType] {

  def create(entity: T)(implicit session: SessionType): EitherT[F, Error, T]

  def get(id: K)(implicit session: SessionType): EitherT[F, Error, T]

  def delete(entityId: K)(implicit session: SessionType): EitherT[F, Error, T]

  def update(entity: T)(implicit session: SessionType): EitherT[F, Error, T]

  def collection(isCount: Boolean)(implicit session: SessionType): EitherT[F, Error, DataResult[T]]

  def list()(implicit session: SessionType, F: Applicative[F]): EitherT[F, Error, Iterable[T]] =
    collection(isCount = false).map {
      case CountResult(_) => List[T]()
      case ItrResult(v)   => v
    }

  def size()(implicit session: SessionType, F: Applicative[F]): EitherT[F, Error, Int] =
    collection(isCount = true).map {
      case CountResult(v) => v
      case ItrResult(_)   => 0
    }

}
