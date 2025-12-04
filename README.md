# universal-rest

A Scala library for rapidly building type-safe CRUD REST APIs with http4s.

[![Scala CI](https://github.com/sapienapps/universal-rest/actions/workflows/scala.yml/badge.svg)](https://github.com/sapienapps/universal-rest/actions/workflows/scala.yml)

## Features

- Composable CRUD endpoints with minimal boilerplate
- Support for both open and authenticated routes
- Pagination and filtering via query parameters
- Tagless final design with Cats Effect
- Functional error handling with `EitherT`
- Built-in JSON serialization via Circe
- CORS support out of the box

## Installation

Add to your `build.sbt`:

```scala
resolvers += "GitHub Package Registry (sapienapps)" at "https://maven.pkg.github.com/sapienapps/universal-rest"

libraryDependencies += "com.sapienapps" %% "universal-rest" % "0.9.4"
```

## Quick Start

### 1. Define your repository

```scala
import cats.Applicative
import cats.data.EitherT
import com.sapienapps.http4s._

case class UserRepo[F[_]: Applicative]() extends CrudRepository[F, String, User, AppError, Session] {
  def create(entity: User)(implicit session: Session): EitherT[F, AppError, User] = ???
  def get(id: String)(implicit session: Session): EitherT[F, AppError, User] = ???
  def update(entity: User)(implicit session: Session): EitherT[F, AppError, User] = ???
  def delete(id: String)(implicit session: Session): EitherT[F, AppError, User] = ???
  def collection(isCount: Boolean)(implicit session: Session): EitherT[F, AppError, DataResult[User]] = ???

  // Optional: Override for pagination/filtering support
  override def collection(isCount: Boolean, params: QueryParams)(implicit session: Session) = {
    // Use params.limit, params.offset, params.filters
    ???
  }
}
```

### 2. Create an error handler

```scala
import com.sapienapps.http4s.ErrorHandler

case class MyErrorHandler[F[_]]() extends ErrorHandler[F, AppError] {
  def handle(e: AppError)(implicit m: Monad[F]): F[Response[F]] = e match {
    case NotFound(msg) => NotFound(msg)
    case ValidationError(msg) => BadRequest(msg)
    case _ => InternalServerError("Something went wrong")
  }
}
```

### 3. Wire up endpoints

**Open (unauthenticated) endpoints:**

```scala
import com.sapienapps.http4s.open.UniversalEndpoint

val endpoint = UniversalEndpoint[IO, String, User, AppError, String, Any, Session](
  toParams = _ => Right(Map.empty),
  toSession = _ => Session(),
  errorHandler = MyErrorHandler[IO](),
  toId = identity
)

// Using a service
val routes: HttpRoutes[IO] = endpoint.endpoints(UniversalService(UserRepo[IO]()))

// Or directly with a repository (convenience method)
val routes: HttpRoutes[IO] = endpoint.endpoints(UserRepo[IO]())
```

**Authenticated endpoints:**

```scala
import com.sapienapps.http4s.auth.AuthUniversalEndpoint
import org.http4s.server.AuthMiddleware

val authEndpoint = AuthUniversalEndpoint[IO, String, User, AppError, String, Any, UserContext](
  toParams = _ => Right(Map.empty),
  toSession = (_, ctx) => ctx,
  errorHandler = MyErrorHandler[IO](),
  toId = identity
)

// Using a service
val authRoutes: HttpRoutes[IO] = authEndpoint.endpoints(
  UniversalService(UserRepo[IO]()),
  authMiddleware
)

// Or directly with a repository
val authRoutes: HttpRoutes[IO] = authEndpoint.endpoints(UserRepo[IO](), authMiddleware)
```

## Generated Endpoints

Each endpoint class generates the following routes:

| Method | Path | Description | Query Params |
|--------|------|-------------|--------------|
| POST | `/` | Create entity | - |
| GET | `/:id` | Get entity by ID | - |
| GET | `/` | List entities | `limit`, `offset`, custom filters |
| GET | `/count` | Get entity count | - |
| PUT | `/` | Update entity | - |
| DELETE | `/:id` | Delete entity by ID | - |

### Pagination & Filtering

The list endpoint (`GET /`) automatically extracts query parameters:

```
GET /users?limit=10&offset=20&status=active&role=admin
```

These are passed to your repository as `QueryParams`:

```scala
case class QueryParams(
  limit: Option[Int] = None,
  offset: Option[Int] = None,
  filters: Map[String, String] = Map.empty
)
```

## Architecture

```
CrudEndpoint (HTTP routing)
    ↓
CrudService (business logic) ← optional, can use repo directly
    ↓
CrudRepository (data access)
```

## Tech Stack

- Scala 2.13
- http4s 0.23.x (Ember)
- Circe (JSON)
- Cats Effect

## License

MIT
