name := "server"

version := "0.1"

scalaVersion := "2.13.6"

idePackagePrefix := Some("com.chessonline")

val enumeratumVersion = "1.7.0"
val scalatestVersion = "3.2.10"

libraryDependencies ++= Seq(
  "com.beachape" %% "enumeratum" % enumeratumVersion,
  "org.typelevel" %% "cats-core" % "2.6.1",
  "org.scalatest" %% "scalatest" % "3.2.10" % "test"
)
