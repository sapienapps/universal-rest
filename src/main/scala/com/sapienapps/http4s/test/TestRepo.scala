package com.sapienapps.http4s.test

import cats.Applicative
import cats.data.EitherT
import com.sapienapps.http4s.{CountResult, CrudRepository, DataResult, ItrResult}

case class TestRepo[F[_]: Applicative, Params]() extends CrudRepository[F, Int, String, TestAppError, String] {

  override def create(entity: String)(implicit session: String): EitherT[F, TestAppError, String] =
    entity match {
      case "" => EitherT.fromEither(Left(TestGenericError(s"Create Error")))
      case _  => EitherT.fromEither(Right(s"Create ${entity}"))
    }

  override def get(id: Int)(implicit session: String): EitherT[F, TestAppError, String] =
    id match {
      case 0 => EitherT.fromEither(Left(TestNotFoundError(s"Get Error $id")))
      case _ => EitherT.fromEither(Right(s"Get $id"))
    }

  override def delete(id: Int)(implicit session: String): EitherT[F, TestAppError, String] =
    id match {
      case 0 => EitherT.fromEither(Left(TestGenericError(s"Delete Error $id")))
      case _ => EitherT.fromEither(Right(s"Delete $id"))
    }

  override def update(entity: String)(implicit session: String): EitherT[F, TestAppError, String] =
    entity match {
      case "" => EitherT.fromEither(Left(TestGenericError(s"Update Error")))
      case _  => EitherT.fromEither(Right(s"Update ${entity}"))
    }

  override def collection(isCount: Boolean)(implicit session: String): EitherT[F, TestAppError, DataResult[String]] =
    if (isCount) {
      EitherT.fromEither(Right(CountResult(2)))
    } else {
      EitherT.fromEither(Right(ItrResult(List("Item0", "Item1"))))
    }

}
