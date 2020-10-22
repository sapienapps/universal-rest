package com.sapienapps.http4s

import org.http4s.Response
import tsec.authentication.{SecuredRequest, TSecAuthService}

package object auth {
  type AuthService[F[_], User, Auth] = TSecAuthService[User, Auth, F]
  type AuthEndpoint[F[_], User, Auth] = PartialFunction[SecuredRequest[F, User, Auth], F[Response[F]]]
}
