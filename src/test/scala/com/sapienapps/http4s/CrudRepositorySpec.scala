package com.sapienapps.http4s

import cats.effect.IO
import com.sapienapps.http4s.{test => testpkg}
import munit.CatsEffectSuite

class CrudRepositorySpec extends CatsEffectSuite {

  implicit val session: String = "test-session"

  val repo: testpkg.TestRepo[IO, String] = testpkg.TestRepo[IO, String]()

  test("list() returns items from collection(isCount=false)") {
    repo.list().value.map { result =>
      assertEquals(result, Right(List("Item0", "Item1")))
    }
  }

  test("size() returns count from collection(isCount=true)") {
    repo.size().value.map { result =>
      assertEquals(result, Right(2))
    }
  }

  test("create delegates to implementation") {
    repo.create("entity").value.map { result =>
      assertEquals(result, Right("Create entity"))
    }
  }

  test("get delegates to implementation") {
    repo.get(42).value.map { result =>
      assertEquals(result, Right("Get 42"))
    }
  }

  test("update delegates to implementation") {
    repo.update("updated").value.map { result =>
      assertEquals(result, Right("Update updated"))
    }
  }

  test("delete delegates to implementation") {
    repo.delete(99).value.map { result =>
      assertEquals(result, Right("Delete 99"))
    }
  }

}
