package com.sapienapps.http4s

import cats.Applicative
import cats.data._
import org.http4s.headers.Authorization
import org.http4s.{AuthedRequest, Request, Response, Status}

package object auth {

  type AuthEndpoint[F[_], Context] = PartialFunction[AuthedRequest[F, Context], F[Response[F]]]

  object asAuthed {

    /**
     * Matcher for the http4s dsl
     *
     * @param ar
     * @tparam F
     * @tparam Context
     * @return
     */
    def unapply[F[_], Context](ar: AuthedRequest[F, Context]): Option[(Request[F], Context)] =
      Option(ar.req -> ar.context)

  }

  case class User(id: Long, name: String)

  def authUserTest[F[_]: Applicative]: Kleisli[OptionT[F, *], Request[F], String] =
    Kleisli { _ =>
      OptionT.fromOption(None)
    }

  def customFailure[F[_]](implicit F: Applicative[F]): Request[F] => F[Response[F]] =
    _ => F.pure(Response[F](Status.Forbidden))

  def authUser[F[_]: Applicative]: Kleisli[OptionT[F, *], Request[F], User] = Kleisli { request =>
    val message = for {
      header <- request.headers.get[Authorization].toRight("Couldn't find an Authorization header")
      token = header.credentials.toString()
      message <- Right(User(10, ""))
    } yield message
    val either = message.toOption
    OptionT.fromOption(either)
  }

}
