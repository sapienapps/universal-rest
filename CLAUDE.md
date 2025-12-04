# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

universal-rest is a Scala library for rapidly building CRUD REST APIs with http4s. It provides a composable architecture with tagless final patterns, supporting both open (unauthenticated) and authenticated endpoints.

## Build Commands

```bash
sbt compile          # Compile the project
sbt test             # Run all tests
sbt "testOnly *UniversalServiceSpec"  # Run a single test class
sbt run              # Run the example server (port 8080)
sbt scalafmtAll      # Format all Scala files
sbt "scalafixAll"    # Run scalafix linting/refactoring
sbt publish          # Publish to GitHub Packages
```

## Architecture

### Core Abstraction Layers

The library follows a layered architecture with clear separation of concerns:

```
CrudEndpoint (HTTP routing)
    ↓
CrudService (business logic)
    ↓
CrudRepository (data access)
```

### Key Components

**Repository Layer** (`CrudRepository[F, K, T, Error, SessionType]`)
- Type parameters: effect type `F`, key type `K`, entity type `T`, error type, session/context type
- Returns `EitherT[F, Error, T]` for all operations enabling functional error handling
- `collection(isCount: Boolean)` returns `DataResult[T]` (either `ItrResult` or `CountResult`)

**Service Layer** (`CrudService`, `UniversalService`)
- `UniversalService` is a pass-through implementation delegating to repository
- All operations require implicit `Monad[F]` and `SessionType`

**Endpoint Layer**
- `open.UniversalEndpoint` - unauthenticated routes using `HttpRoutes[F]`
- `auth.AuthUniversalEndpoint` - authenticated routes using http4s `AuthMiddleware`
- Both implement `CrudEndpoint` trait providing: create, get, list, update, delete, count

**RouteBuilder** - Convenience wrapper combining endpoint creation with CORS policy

### Type Parameters Convention

Throughout the codebase:
- `F[_]` - Effect type (typically `IO`)
- `K` - Entity key/ID type
- `T` - Entity type
- `Error` - Application error type (sealed trait recommended)
- `SessionType`/`Context` - Request context (user session, auth info)
- `ParamName`/`ParamValue` - Request parameter types

### Error Handling Pattern

Implement `ErrorHandler[F, E]` trait to map domain errors to HTTP responses:
```scala
trait ErrorHandler[F[_], E] {
  def handle(e: E)(implicit m: Monad[F]): F[Response[F]]
}
```

### Auth Pattern

The `auth` package provides:
- `AuthEndpoint[F, Context]` type alias for authenticated partial functions
- `asAuthed` extractor for pattern matching in route definitions
- Uses http4s `AuthMiddleware` for authentication

## Tech Stack

- Scala 2.13.12
- http4s 0.23.24 (Ember server/client)
- Circe 0.14.6 (JSON)
- Cats Effect (tagless final)
- munit-cats-effect 2.0.0 (testing)
- kind-projector compiler plugin

## Code Style

- Scalafmt 3.7.12 with trailing commas, max 120 columns
- Scalafix rules: RemoveUnused, LeakingImplicitClassVal, ProcedureSyntax, ExplicitResultTypes
- `-Xfatal-warnings` enabled - all warnings are errors
