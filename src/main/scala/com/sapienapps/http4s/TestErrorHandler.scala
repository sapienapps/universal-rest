package com.sapienapps.http4s

import cats.Monad
import org.http4s.Response

case class TestErrorHandler[F[_]]() extends ErrorHandler[F, String] {
  override def handle(e: String)(implicit m: Monad[F]): F[Response[F]] = {
    BadRequest(s"BadRequest! ${e}")
  }
}
