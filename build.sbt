name := "universal-rest"

version := "0.1"

scalaVersion := "2.12.11"

val Http4sVersion  = "0.21.4"

libraryDependencies ++= Seq(
  "org.http4s"          %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"          %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s"          %% "http4s-circe"        % Http4sVersion,
  "org.http4s"          %% "http4s-dsl"          % Http4sVersion,
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Ypartial-unification",
  "-Xfatal-warnings"
)
