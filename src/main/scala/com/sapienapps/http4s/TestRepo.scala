package com.sapienapps.http4s

import cats.Applicative
import cats.data.EitherT

case class TestRepo[F[_] : Applicative, Params]() extends CrudRepository[F, Int, String, String, String] {

  override def create(entity: String)(implicit session: String): EitherT[F, String, String] = {
    entity match {
      case "" => EitherT.fromEither(Left(s"Create Error"))
      case _ => EitherT.fromEither(Right(s"Create ${entity}"))
    }
  }

  override def get(id: Int)(implicit session: String): EitherT[F, String, String] = {
    id match {
      case 0 => EitherT.fromEither(Left(s"Get Error $id"))
      case _ => EitherT.fromEither(Right(s"Get $id"))
    }
  }

  override def delete(id: Int)(implicit session: String): EitherT[F, String, String] = {
    id match {
      case 0 => EitherT.fromEither(Left(s"Delete Error $id"))
      case _ => EitherT.fromEither(Right(s"Delete $id"))
    }
  }

  override def update(entity: String)(implicit session: String): EitherT[F, String, String] = {
    entity match {
      case "" => EitherT.fromEither(Left(s"Update Error"))
      case _ => EitherT.fromEither(Right(s"Update ${entity}"))
    }
  }

  override def list()(implicit session: String): EitherT[F, String, List[String]] = {
    EitherT.fromEither(Right(List("Item0", "Item1")))
  }

  override def size()(implicit session: String): EitherT[F, String, Int] = {
    EitherT.fromEither(Right(2))
  }

}
