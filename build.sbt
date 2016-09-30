// Copyright (c) 2016, Zoltán Nagy <abesto@abesto.net>
//
// Permission to use, copy, modify, and/or distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

name := """akkaircd"""

version := "1.0"

scalaVersion := "2.11.8"

def akkaVersion: String = "2.4.10"
def scalatestVersion: String = "3.0.0"

libraryDependencies ++= Seq(
  // Akka
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  // Parsing
  "org.parboiled" %% "parboiled" % "2.1.3",
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

// Fail tests on Scalastyle violations
(scalastyleFailOnError in Test) := true