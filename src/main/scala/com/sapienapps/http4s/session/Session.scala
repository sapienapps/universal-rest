package com.sapienapps.http4s.session

case class Session[Params, U](params: Map[Params, _] = Map(), user: Option[U] = None) {

}
