package org.sapienapps

import cats.effect.{ExitCode, IO, IOApp}
import org.sapienapps.http4s.Server

object Main extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {
    Server().stream[IO](List()).compile.drain.as(ExitCode.Success)
  }
}
