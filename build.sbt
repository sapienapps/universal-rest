name := "universal-rest"

version := "0.9.0"
organization := "com.sapienapps"

githubOwner := "sapienapps"
githubRepository := "universal-rest"

scalaVersion := "2.13.10"

val Http4sVersion  = "0.23.24"
val CirceVersion   = "0.14.6"

libraryDependencies ++= Seq(
  "org.http4s"          %% "http4s-ember-server" % Http4sVersion,
  "org.http4s"          %% "http4s-ember-client" % Http4sVersion,
  "org.http4s"          %% "http4s-circe"        % Http4sVersion,
  "org.http4s"          %% "http4s-dsl"          % Http4sVersion,
  "io.circe"            %% "circe-generic"       % CirceVersion,
)

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings"
)
