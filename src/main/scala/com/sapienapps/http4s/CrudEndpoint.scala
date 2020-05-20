package com.sapienapps.http4s

import org.http4s.Request
import org.http4s.dsl.Http4sDsl


trait CrudEndpoint[F[_], K, T, Endpoint, Error, Params, U] extends Http4sDsl[F] {

  type Service = CrudService[F, K, T, Error, Params, U]

  def toParamMap(request: Request[F]): Either[String, Map[Params, _]]

  def create(service: Service): Endpoint

  def get(service: Service): Endpoint

  def list(service: Service): Endpoint

  def update(service: Service): Endpoint

  def count(service: Service): Endpoint
}
