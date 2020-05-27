package com.sapienapps

package object http4s {

  sealed trait DataResult[T]

  case class ItrResult[T](list: Iterable[T]) extends DataResult[T]

  case class CountResult[T](value: Int) extends DataResult[T]

}
