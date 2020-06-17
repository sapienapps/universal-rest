package com.sapienapps.http4s

package object test {

  sealed trait TestAppError extends Throwable with Product with Serializable

  sealed trait TestEntityError extends TestAppError

  case class TestGenericError(msg: String) extends TestEntityError

  case class TestNotFoundError(msg: String) extends TestEntityError

}
