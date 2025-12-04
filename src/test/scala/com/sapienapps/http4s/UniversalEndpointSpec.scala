package com.sapienapps.http4s

import cats.effect.IO
import com.sapienapps.http4s.open.UniversalEndpoint
import com.sapienapps.http4s.{test => testpkg}
import io.circe.Encoder
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._

class UniversalEndpointSpec extends CatsEffectSuite {

  implicit val entityDecoder: EntityDecoder[IO, String] = jsonOf[IO, String]
  implicit val entityEncoder: Encoder[String] = Encoder.encodeString
  implicit val listEntityDecoder: EntityDecoder[IO, List[String]] = jsonOf[IO, List[String]]
  implicit val intEntityDecoder: EntityDecoder[IO, Int] = jsonOf[IO, Int]

  val toParams: Request[IO] => Either[String, Map[String, Any]] = _ => Right(Map.empty)
  val toSession: Map[String, Any] => String = _ => "test-session"
  val toId: String => Int = Integer.parseInt

  val repo: testpkg.TestRepo[IO, String] = testpkg.TestRepo[IO, String]()
  val service: UniversalService[IO, Int, String, testpkg.TestAppError, String] = UniversalService(repo)

  val endpoint: UniversalEndpoint[IO, Int, String, testpkg.TestAppError, String, Any, String] =
    UniversalEndpoint(toParams, toSession, testpkg.TestErrorHandler[IO](), toId)

  val routes: HttpRoutes[IO] = endpoint.endpoints(service)

  test("GET /:id returns entity") {
    val request = Request[IO](Method.GET, uri"/1")
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map { body =>
        assertEquals(body, "Get 1")
      }
    }
  }

  test("GET /:id returns BadRequest for not found") {
    val request = Request[IO](Method.GET, uri"/0")
    routes.orNotFound.run(request).map { response =>
      assertEquals(response.status, Status.BadRequest)
    }
  }

  test("GET / returns list of entities") {
    val request = Request[IO](Method.GET, uri"/")
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[List[String]].map { body =>
        assertEquals(body, List("Item0", "Item1"))
      }
    }
  }

  test("GET /count returns count") {
    val request = Request[IO](Method.GET, uri"/count")
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[Int].map { body =>
        assertEquals(body, 2)
      }
    }
  }

  test("POST / creates entity") {
    val request = Request[IO](Method.POST, uri"/")
      .withEntity("new-entity")(jsonEncoderOf[IO, String])
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map { body =>
        assertEquals(body, "Create new-entity")
      }
    }
  }

  test("POST / with empty body returns BadRequest") {
    val request = Request[IO](Method.POST, uri"/")
      .withEntity("")(jsonEncoderOf[IO, String])
    routes.orNotFound.run(request).map { response =>
      assertEquals(response.status, Status.BadRequest)
    }
  }

  test("PUT / updates entity") {
    val request = Request[IO](Method.PUT, uri"/")
      .withEntity("updated-entity")(jsonEncoderOf[IO, String])
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map { body =>
        assertEquals(body, "Update updated-entity")
      }
    }
  }

  test("DELETE /:id deletes entity") {
    val request = Request[IO](Method.DELETE, uri"/1")
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map { body =>
        assertEquals(body, "Delete 1")
      }
    }
  }

  test("DELETE /:id returns BadRequest for id 0") {
    val request = Request[IO](Method.DELETE, uri"/0")
    routes.orNotFound.run(request).map { response =>
      assertEquals(response.status, Status.BadRequest)
    }
  }

  test("GET / with limit param extracts pagination") {
    val request = Request[IO](Method.GET, uri"/?limit=10&offset=5")
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[List[String]].map { body =>
        assertEquals(body, List("Item0", "Item1"))
      }
    }
  }

  test("GET / with filter params extracts filters") {
    val request = Request[IO](Method.GET, uri"/?status=active&type=user")
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[List[String]].map { body =>
        assertEquals(body, List("Item0", "Item1"))
      }
    }
  }

  test("endpoints(repo) convenience method works") {
    val repoRoutes = endpoint.endpoints(repo)
    val request = Request[IO](Method.GET, uri"/1")
    repoRoutes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map { body =>
        assertEquals(body, "Get 1")
      }
    }
  }

}
