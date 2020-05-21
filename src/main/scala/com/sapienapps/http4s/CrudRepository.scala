package com.sapienapps.http4s

import cats.data.EitherT

trait CrudRepository[F[_], K, T, Error, SessionType] {

  def create(entity: T)(implicit session: SessionType): EitherT[F, Error, T]

  def get(id: K)(implicit session: SessionType): EitherT[F, Error, T]

  def delete(entityId: K)(implicit session: SessionType): EitherT[F, Error, T]

  def update(entity: T)(implicit session: SessionType): EitherT[F, Error, T]

  // TODO add to remaining Repos: def collection(isCount: Boolean)(implicit session: Session): DataResult[T]

  def list()(implicit session: SessionType): EitherT[F, Error, List[T]]

  def size()(implicit session: SessionType): EitherT[F, Error, Int]
}
