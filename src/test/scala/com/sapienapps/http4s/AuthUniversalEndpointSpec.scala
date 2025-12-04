package com.sapienapps.http4s

import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import com.sapienapps.http4s.auth.AuthUniversalEndpoint
import com.sapienapps.http4s.{test => testpkg}
import io.circe.Encoder
import munit.CatsEffectSuite
import org.http4s._
import org.http4s.circe._
import org.http4s.headers.Authorization
import org.http4s.implicits._
import org.http4s.server.AuthMiddleware

class AuthUniversalEndpointSpec extends CatsEffectSuite {

  // Using String as context type to match TestRepo's session type
  type Context = String

  implicit val entityDecoder: EntityDecoder[IO, String] = jsonOf[IO, String]
  implicit val entityEncoder: Encoder[String] = Encoder.encodeString
  implicit val listEntityDecoder: EntityDecoder[IO, List[String]] = jsonOf[IO, List[String]]
  implicit val intEntityDecoder: EntityDecoder[IO, Int] = jsonOf[IO, Int]

  val toParams: AuthedRequest[IO, Context] => Either[String, Map[String, Any]] = _ => Right(Map.empty)
  val toSession: (Map[String, Any], Context) => Context = (_, ctx) => ctx
  val toId: String => Int = Integer.parseInt

  // Simple auth that extracts user from Authorization header
  val authUser: Kleisli[OptionT[IO, *], Request[IO], Context] = Kleisli { request =>
    val user = for {
      _ <- request.headers.get[Authorization]
    } yield "authenticated-user"
    OptionT.fromOption[IO](user)
  }

  val onFailure: Request[IO] => IO[Response[IO]] = _ => IO.pure(Response[IO](Status.Forbidden))

  val middleware: AuthMiddleware[IO, Context] = AuthMiddleware.noSpider(authUser, onFailure)

  val repo: testpkg.TestRepo[IO, String] = testpkg.TestRepo[IO, String]()
  val service: UniversalService[IO, Int, String, testpkg.TestAppError, Context] = UniversalService(repo)

  val endpoint: AuthUniversalEndpoint[IO, Int, String, testpkg.TestAppError, String, Any, Context] =
    AuthUniversalEndpoint(toParams, toSession, testpkg.TestErrorHandler[IO](), toId)

  val routes: HttpRoutes[IO] = endpoint.endpoints(service, middleware)

  def authHeader: Authorization = Authorization(Credentials.Token(AuthScheme.Bearer, "test-token"))

  test("GET /:id returns entity when authenticated") {
    val request = Request[IO](Method.GET, uri"/1").putHeaders(authHeader)
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map { body =>
        assertEquals(body, "Get 1")
      }
    }
  }

  test("GET /:id returns Forbidden without auth") {
    val request = Request[IO](Method.GET, uri"/1")
    routes.orNotFound.run(request).map { response =>
      assertEquals(response.status, Status.Forbidden)
    }
  }

  test("GET / returns list when authenticated") {
    val request = Request[IO](Method.GET, uri"/").putHeaders(authHeader)
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[List[String]].map { body =>
        assertEquals(body, List("Item0", "Item1"))
      }
    }
  }

  test("GET /count returns count when authenticated") {
    val request = Request[IO](Method.GET, uri"/count").putHeaders(authHeader)
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[Int].map { body =>
        assertEquals(body, 2)
      }
    }
  }

  test("POST / creates entity when authenticated") {
    val request = Request[IO](Method.POST, uri"/")
      .putHeaders(authHeader)
      .withEntity("new-entity")(jsonEncoderOf[IO, String])
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map { body =>
        assertEquals(body, "Create new-entity")
      }
    }
  }

  test("POST / returns Forbidden without auth") {
    val request = Request[IO](Method.POST, uri"/")
      .withEntity("new-entity")(jsonEncoderOf[IO, String])
    routes.orNotFound.run(request).map { response =>
      assertEquals(response.status, Status.Forbidden)
    }
  }

  test("PUT / updates entity when authenticated") {
    val request = Request[IO](Method.PUT, uri"/")
      .putHeaders(authHeader)
      .withEntity("updated-entity")(jsonEncoderOf[IO, String])
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map { body =>
        assertEquals(body, "Update updated-entity")
      }
    }
  }

  test("DELETE /:id deletes entity when authenticated") {
    val request = Request[IO](Method.DELETE, uri"/1").putHeaders(authHeader)
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map { body =>
        assertEquals(body, "Delete 1")
      }
    }
  }

  test("DELETE /:id returns Forbidden without auth") {
    val request = Request[IO](Method.DELETE, uri"/1")
    routes.orNotFound.run(request).map { response =>
      assertEquals(response.status, Status.Forbidden)
    }
  }

  test("GET / with pagination params works when authenticated") {
    val request = Request[IO](Method.GET, uri"/?limit=10&offset=0").putHeaders(authHeader)
    routes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[List[String]].map { body =>
        assertEquals(body, List("Item0", "Item1"))
      }
    }
  }

  test("endpoints(repo, auth) convenience method works") {
    val repoRoutes = endpoint.endpoints(repo, middleware)
    val request = Request[IO](Method.GET, uri"/1").putHeaders(authHeader)
    repoRoutes.orNotFound.run(request).flatMap { response =>
      assertEquals(response.status, Status.Ok)
      response.as[String].map { body =>
        assertEquals(body, "Get 1")
      }
    }
  }

}
