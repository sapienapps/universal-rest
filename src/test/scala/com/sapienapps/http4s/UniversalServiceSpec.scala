package com.sapienapps.http4s

import cats.effect.IO
import com.sapienapps.http4s.{test => testpkg}
import munit.CatsEffectSuite

class UniversalServiceSpec extends CatsEffectSuite {

  implicit val session: String = "test-session"

  val repo: testpkg.TestRepo[IO, String] = testpkg.TestRepo[IO, String]()
  val service: UniversalService[IO, Int, String, testpkg.TestAppError, String] = UniversalService(repo)

  test("create returns created entity for valid input") {
    service.create("test-entity").value.map { result =>
      assertEquals(result, Right("Create test-entity"))
    }
  }

  test("create returns error for empty input") {
    service.create("").value.map { result =>
      assertEquals(result, Left(testpkg.TestGenericError("Create Error")))
    }
  }

  test("get returns entity for valid id") {
    service.get(1).value.map { result =>
      assertEquals(result, Right("Get 1"))
    }
  }

  test("get returns not found error for id 0") {
    service.get(0).value.map { result =>
      assertEquals(result, Left(testpkg.TestNotFoundError("Get Error 0")))
    }
  }

  test("update returns updated entity for valid input") {
    service.update("updated-entity").value.map { result =>
      assertEquals(result, Right("Update updated-entity"))
    }
  }

  test("update returns error for empty input") {
    service.update("").value.map { result =>
      assertEquals(result, Left(testpkg.TestGenericError("Update Error")))
    }
  }

  test("delete returns deleted entity for valid id") {
    service.delete(1).value.map { result =>
      assertEquals(result, Right("Delete 1"))
    }
  }

  test("delete returns error for id 0") {
    service.delete(0).value.map { result =>
      assertEquals(result, Left(testpkg.TestGenericError("Delete Error 0")))
    }
  }

  test("list returns all items") {
    service.list().value.map { result =>
      assertEquals(result, Right(List("Item0", "Item1")))
    }
  }

  test("size returns count of items") {
    service.size().value.map { result =>
      assertEquals(result, Right(2))
    }
  }

}
