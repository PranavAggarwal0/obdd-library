val Http4sVersion = "0.23.16"
val LogbackVersion = "1.2.6"

lazy val root = (project in file("."))
  .settings(
    organization := "obddlib",
    name := "obdd-http-app",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "3.2.0",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "obddlib" %% "obdd" % "0.1.0-SNAPSHOT",
      "ch.qos.logback" % "logback-classic" % LogbackVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "io.circe" %% "circe-generic" % "0.14.3",
      "io.circe" %% "circe-literal" % "0.14.3"
    ),
    testFrameworks += new TestFramework("munit.Framework")
  )
