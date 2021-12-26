name := "server"

version := "0.1"

scalaVersion := "2.13.6"

idePackagePrefix := Some("com.chessonline")

val enumeratumVersion = "1.7.0"
val catsCoreVersion = "2.7.0"
val catsEffectVersion = "2.5.3"
val circeVersion = "0.14.1"
val http4sVersion = "0.22.7"
val refinedVersion = "0.9.28"
val scalatestVersion = "3.2.10"

enablePlugins(JavaAppPackaging)

scalacOptions ++= Seq(
  "-Ymacro-annotations"
)

addCompilerPlugin(
  "org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full
)

libraryDependencies ++= Seq(
  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "org.typelevel" %% "cats-core" % catsCoreVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,
  "eu.timepit" %% "refined" % refinedVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % "test",
  "org.scalamock" %% "scalamock" % "5.1.0" % Test
) ++ Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion) ++ Seq(
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-blaze-server",
  "org.http4s" %% "http4s-blaze-client",
  "org.http4s" %% "http4s-circe"
).map(_ % http4sVersion)
