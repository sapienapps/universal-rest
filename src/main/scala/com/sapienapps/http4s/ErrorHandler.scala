package com.sapienapps.http4s

import cats.Monad
import org.http4s.Response
import org.http4s.dsl.Http4sDsl

trait ErrorHandler[F[_], Error] extends Http4sDsl[F] {

  def handle(e: Error)(implicit m: Monad[F]): F[Response[F]]

}
