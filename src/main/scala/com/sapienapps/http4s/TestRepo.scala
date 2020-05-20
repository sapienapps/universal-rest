package com.sapienapps.http4s

import cats.Applicative
import cats.data.EitherT
import com.sapienapps.http4s.session.Session

case class TestRepo[F[_] : Applicative, Params, U]() extends CrudRepository[F, Int, String, String, Params, U] {
  override def create(entity: String)(implicit session: Session[Params, U]): EitherT[F, String, String] = {
    entity match {
      case "" => EitherT.fromEither(Left("Create Error"))
      case _ => EitherT.fromEither(Right("Create"))
    }
  }

  override def get(id: Int)(implicit session: Session[Params, U]): EitherT[F, String, String] = {
    id match {
      case 0 => EitherT.fromEither(Left("Get Error"))
      case _ => EitherT.fromEither(Right("Get"))
    }
  }

  override def delete(id: Int)(implicit session: Session[Params, U]): EitherT[F, String, String] = {
    id match {
      case 0 => EitherT.fromEither(Left("Delete Error"))
      case _ => EitherT.fromEither(Right("get"))
    }
  }

  override def update(entity: String)(implicit session: Session[Params, U]): EitherT[F, String, String] = {
    entity match {
      case "" => EitherT.fromEither(Left("Update Error"))
      case _ => EitherT.fromEither(Right("Update"))
    }
  }

  override def list()(implicit session: Session[Params, U]): EitherT[F, String, List[String]] = {
    EitherT.fromEither(Right(List("Item0", "Item1")))
  }

  override def size()(implicit session: Session[Params, U]): EitherT[F, String, Int] = {
    EitherT.fromEither(Right(2))
  }

}
