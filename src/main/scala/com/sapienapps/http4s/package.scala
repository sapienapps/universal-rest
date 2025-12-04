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

  /**
   * Query parameters for list operations supporting pagination and filtering.
   *
   * @param limit   Maximum number of results to return
   * @param offset  Number of results to skip (for pagination)
   * @param filters Key-value pairs for filtering results (implementation-specific)
   */
  case class QueryParams(
    limit: Option[Int] = None,
    offset: Option[Int] = None,
    filters: Map[String, String] = Map.empty,
  ) {
    def isEmpty: Boolean = limit.isEmpty && offset.isEmpty && filters.isEmpty
  }

  object QueryParams {
    val empty: QueryParams = QueryParams()
  }

}
