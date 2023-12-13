package com.sapienapps

import cats.effect.{ExitCode, IO, IOApp}
import com.sapienapps.http4s.Server

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    Server().stream[IO](List()).use(_ => IO.never)
      .as(ExitCode.Success)
  }
}
