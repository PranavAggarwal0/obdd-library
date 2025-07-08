val scala3Version = "3.2.0"

lazy val root = project
  .in(file("."))
  .settings(
    name                                            := "obdd",
    organization                                    := "obddlib",
    version                                         := "0.1.0-SNAPSHOT",
    scalaVersion                                    := scala3Version,
    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1",
    libraryDependencies += "org.typelevel"          %% "cats-effect"              % "3.3.12",
    libraryDependencies += "org.scalatest"          %% "scalatest"                % "3.2.14"%"test"
  )
