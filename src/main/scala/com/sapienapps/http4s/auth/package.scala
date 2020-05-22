package com.sapienapps.http4s

import org.http4s.Response
import tsec.authentication.{AugmentedJWT, SecuredRequest, TSecAuthService}

package object auth {
  type AuthService[F[_], User, Auth] = TSecAuthService[User, AugmentedJWT[Auth, String], F]
  type AuthEndpoint[F[_], User, Auth] = PartialFunction[SecuredRequest[F, User, AugmentedJWT[Auth, String]], F[Response[F]]]
}
