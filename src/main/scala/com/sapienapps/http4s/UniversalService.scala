package com.sapienapps.http4s

import cats.Monad
import cats.data.EitherT
import com.sapienapps.http4s.session.Session

case class UniversalService[F[_], K, T, Error, Params, U](repo: CrudRepository[F, K, T, Error, Params, U])
  extends CrudService[F, K, T, Error, Params, U] {

  def create(entity: T)(implicit M: Monad[F], session: Session[Params, U]): EitherT[F, Error, T] = {
    for {
      created <- repo.create(entity)
    } yield created
  }

  def get(id: K)(implicit M: Monad[F], session: Session[Params, U]): EitherT[F, Error, T] = {
    for {
      entity <- repo.get(id)
    } yield entity
  }

  def update(entity: T)(implicit M: Monad[F], session: Session[Params, U]): EitherT[F, Error, T] = {
    for {
      updated <- repo.update(entity)
    } yield updated
  }

  def delete(id: K)(implicit M: Monad[F], session: Session[Params, U]): EitherT[F, Error, T] = {
    for {
      deleted <- repo.delete(id)
    } yield deleted
  }

  def list()(implicit M: Monad[F], session: Session[Params, U]): EitherT[F, Error, List[T]] = {
    for {
      list <- repo.list()
    } yield list
  }

  def size()(implicit M: Monad[F], session: Session[Params, U]): EitherT[F, Error, Int] = {
    for {
      size <- repo.size()
    } yield size
  }
}
