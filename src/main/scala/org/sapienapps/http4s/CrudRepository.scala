package org.sapienapps.http4s

import cats.data.EitherT

trait CrudRepository[F[_], K, T, Error, Params, U] {

  def create(entity: T)(implicit session: Session[Params, U]): EitherT[F, Error, T]

  def get(id: K)(implicit session: Session[Params, U]): EitherT[F, Error, T]

  def delete(entityId: K)(implicit session: Session[Params, U]): EitherT[F, Error, T]

  def update(entity: T)(implicit session: Session[Params, U]): EitherT[F, Error, T]

  // TODO add to remaining Repos: def collection(isCount: Boolean)(implicit session: Session): DataResult[T]

  def list()(implicit session: Session[Params, U]): EitherT[F, Error, List[T]]

  def size()(implicit session: Session[Params, U]): EitherT[F, Error, Int]
}
