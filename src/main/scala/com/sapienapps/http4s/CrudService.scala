package com.sapienapps.http4s

import cats.Monad
import cats.data.EitherT
import org.http4s.dsl.Http4sDsl
import com.sapienapps.http4s.session.Session

trait CrudService[F[_], K, T, Error, Params, U] extends Http4sDsl[F] {

  def create(entity: T)(implicit M: Monad[F], session: Session[Params, U]): EitherT[F, Error, T]

  def get(id: K)(implicit M: Monad[F], session: Session[Params, U]): EitherT[F, Error, T]

  def update(entity: T)(implicit M: Monad[F], session: Session[Params, U]): EitherT[F, Error, T]

  def delete(id: K)(implicit M: Monad[F], session: Session[Params, U]): EitherT[F, Error, T]

  def list()(implicit M: Monad[F], session: Session[Params, U]): EitherT[F, Error, List[T]]

  def size()(implicit M: Monad[F], session: Session[Params, U]): EitherT[F, Error, Int]

}
