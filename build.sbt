name := """akkaircd"""

version := "1.0"

scalaVersion := "2.11.8"

def akkaVersion = "2.4.10"

libraryDependencies ++= Seq(
  // Akka
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  // Logging
  "ch.qos.logback" % "logback-classic" % "1.0.13"
)
