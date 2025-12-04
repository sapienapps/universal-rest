# universal-rest

A Scala library for rapidly building type-safe CRUD REST APIs with http4s.

[![Scala CI](https://github.com/sapienapps/universal-rest/actions/workflows/scala.yml/badge.svg)](https://github.com/sapienapps/universal-rest/actions/workflows/scala.yml)

## Features

- Composable CRUD endpoints with minimal boilerplate
- Support for both open and authenticated routes
- Tagless final design with Cats Effect
- Functional error handling with `EitherT`
- Built-in JSON serialization via Circe
- CORS support out of the box

## Installation

Add to your `build.sbt`:

```scala
resolvers += "GitHub Package Registry (sapienapps)" at "https://maven.pkg.github.com/sapienapps/universal-rest"

libraryDependencies += "com.sapienapps" %% "universal-rest" % "1.0.0"
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

val routes: HttpRoutes[IO] = endpoint.endpoints(UniversalService(UserRepo[IO]()))
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

val authRoutes: HttpRoutes[IO] = authEndpoint.endpoints(
  UniversalService(UserRepo[IO]()),
  authMiddleware
)
```

## Generated Endpoints

Each endpoint class generates the following routes:

| Method | Path | Description |
|--------|------|-------------|
| POST | `/` | Create entity |
| GET | `/:id` | Get entity by ID |
| GET | `/` | List all entities |
| GET | `/count` | Get entity count |
| PUT | `/` | Update entity |
| DELETE | `/:id` | Delete entity by ID |

## Architecture

```
CrudEndpoint (HTTP routing)
    ↓
CrudService (business logic)
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
