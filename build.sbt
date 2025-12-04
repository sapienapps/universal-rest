name := "universal-rest"

version := "0.9.6"
organization := "com.sapienapps"

githubOwner := "sapienapps"
githubRepository := "universal-rest"

val Http4sVersion = "0.23.33"
val CirceVersion = "0.14.15"
val MunitVersion = "1.2.1"
val MunitCEVersion = "2.1.0"
val Slf4jVersion = "2.0.17"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-server" % Http4sVersion,
  "org.http4s" %% "http4s-ember-client" % Http4sVersion,
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "org.scalameta" %% "munit" % MunitVersion % Test,
  "org.typelevel" %% "munit-cats-effect" % MunitCEVersion % Test,
  "org.slf4j" % "slf4j-simple" % Slf4jVersion % Test,
)

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.13.4" cross CrossVersion.full)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  "-Xfatal-warnings",
  "-Ywarn-unused"
)

inThisBuild(
  List(
    scalaVersion := "2.13.18",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
  ),
)