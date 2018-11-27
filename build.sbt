import sbt.Keys.mainClass

name := "anti-domination-bot"
version := "0.1"
scalaVersion := "2.12.7"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.2",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

mainClass in (Compile, run) := Some("com.brightercode.antidominationbot.Runner")

lazy val discourse = RootProject(file(
  if (file("./local-discourse-scala-client").exists()) {
    "./local-discourse-scala-client"
  } else {
    "./discourse-scala-client"
  }))

lazy val root = (project in file("."))
  .dependsOn(discourse)
  .aggregate(discourse)

logBuffered in Test := false