package com.sapienapps

package object http4s {

  sealed trait DataResult[T] {
    def isEmpty: Boolean
  }

  case class ItrResult[T](list: Iterable[T]) extends DataResult[T] {
    def isEmpty: Boolean = list.isEmpty
  }

  case class CountResult[T](value: Int) extends DataResult[T] {
    def isEmpty: Boolean = value == 0
  }

}
