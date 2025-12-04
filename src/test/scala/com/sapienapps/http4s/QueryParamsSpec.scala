package com.sapienapps.http4s

import munit.FunSuite

class QueryParamsSpec extends FunSuite {

  test("QueryParams.empty has no values") {
    val params = QueryParams.empty
    assertEquals(params.limit, None)
    assertEquals(params.offset, None)
    assertEquals(params.filters, Map.empty[String, String])
    assert(params.isEmpty)
  }

  test("QueryParams with values is not empty") {
    val params = QueryParams(limit = Some(10), offset = Some(5))
    assert(!params.isEmpty)
  }

  test("QueryParams with only filters is not empty") {
    val params = QueryParams(filters = Map("status" -> "active"))
    assert(!params.isEmpty)
  }

  test("QueryParams can be constructed with all parameters") {
    val params = QueryParams(
      limit = Some(100),
      offset = Some(50),
      filters = Map("type" -> "user", "active" -> "true"),
    )
    assertEquals(params.limit, Some(100))
    assertEquals(params.offset, Some(50))
    assertEquals(params.filters.size, 2)
    assertEquals(params.filters.get("type"), Some("user"))
  }

}
