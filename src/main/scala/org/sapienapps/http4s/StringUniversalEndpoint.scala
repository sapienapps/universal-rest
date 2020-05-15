package org.sapienapps.http4s

import cats.{Defer, MonadError}
import cats.effect.Sync
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion.User
import io.circe.Encoder
import org.http4s.{EntityDecoder, HttpRoutes, Request}

case class StringUniversalEndpoint[F[_] : Sync, T](ed: EntityDecoder[F, T], e: Encoder[T]) extends UniversalEndpoint[F, String, T, Any, Any, Any] {

  override implicit def A: App = ???

  override implicit def Error: MonadError[F, Throwable] = ???

  override implicit def entityDecoder: EntityDecoder[F, T] = ed

  override implicit def encoder: Encoder[T] = e

  override implicit def defer: Defer[F] = ???

  override def toParamMap(request: Request[F]): Either[String, Map[Any, _]] = ???

  override def toId(request: String): String = ???


}

object StringUniversalEndpoint {

  def endpoints[F[_] : Sync, T](service: CrudService[F, String, T, Any, Any, Any])
                               (implicit ed: EntityDecoder[F, T], e: Encoder[T]): HttpRoutes[F] =
    new StringUniversalEndpoint[F, T](ed, e).endpoints(service)
}
