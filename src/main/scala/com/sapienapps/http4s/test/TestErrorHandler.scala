package com.sapienapps.http4s.test

import cats.Monad
import com.sapienapps.http4s.ErrorHandler
import org.http4s.Response

case class TestErrorHandler[F[_]]() extends ErrorHandler[F, TestAppError] {

  override def handle(e: TestAppError)(implicit m: Monad[F]): F[Response[F]] =
    BadRequest(s"BadRequest! ${e}")

}
