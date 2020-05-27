package com.sapienapps.http4s

import cats.Monad
import cats.data.EitherT

case class UniversalService[F[_], K, T, Error, SessionType](repo: CrudRepository[F, K, T, Error, SessionType])
  extends CrudService[F, K, T, Error, SessionType] {

  def create(entity: T)(implicit M: Monad[F], session: SessionType): EitherT[F, Error, T] = {
    for {
      created <- repo.create(entity)
    } yield created
  }

  def get(id: K)(implicit M: Monad[F], session: SessionType): EitherT[F, Error, T] = {
    for {
      entity <- repo.get(id)
    } yield entity
  }

  def update(entity: T)(implicit M: Monad[F], session: SessionType): EitherT[F, Error, T] = {
    for {
      updated <- repo.update(entity)
    } yield updated
  }

  def delete(id: K)(implicit M: Monad[F], session: SessionType): EitherT[F, Error, T] = {
    for {
      deleted <- repo.delete(id)
    } yield deleted
  }

  def list()(implicit M: Monad[F], session: SessionType): EitherT[F, Error, Iterable[T]] = {
    for {
      list <- repo.list()
    } yield list
  }

  def size()(implicit M: Monad[F], session: SessionType): EitherT[F, Error, Int] = {
    for {
      size <- repo.size()
    } yield size
  }
}
