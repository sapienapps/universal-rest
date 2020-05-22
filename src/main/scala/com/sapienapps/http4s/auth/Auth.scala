package com.sapienapps.http4s.auth

import cats.MonadError
import cats.data.OptionT
import tsec.authentication
import tsec.authorization.Authorization

case class Auth[F[_], Identity, Auth]()(implicit F: MonadError[F, Throwable]) extends Authorization[F, Identity, Auth] {
  override def isAuthorized(toAuth: authentication.SecuredRequest[F, Identity, Auth]): OptionT[F, authentication.SecuredRequest[F, Identity, Auth]] = {
    OptionT.fromOption(Option(toAuth))
  }
}
