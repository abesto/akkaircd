name := """akkaircd"""

version := "1.0"

scalaVersion := "2.11.8"

def akkaVersion = "2.4.10"
def scalatestVersion = "3.0.0"

libraryDependencies ++= Seq(
  // Akka
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  // Logging
  "ch.qos.logback" % "logback-classic" % "1.0.13",
  // Testing
  "org.scalactic" %% "scalactic" % scalatestVersion,
  "org.scalatest" %% "scalatest" % scalatestVersion % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion
)

// Run Scalastyle as part of testing
lazy val testScalastyle = taskKey[Unit]("testScalastyle")
lazy val mainScalastyle = taskKey[Unit]("mainScalastyle")

mainScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value

testScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Test).toTask("").value

(test in Test) <<= (test in Test) dependsOn mainScalastyle

(test in Test) <<= (test in Test) dependsOn testScalastyle
