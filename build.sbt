import sbt._
import Keys._

name := "ReSurfAlt"

organization := "benjamin.finley@.aalto.fi"

version := "1.0"

scalaVersion in ThisBuild := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation")

resolvers ++= Seq(
    "maven.org" at "http://repo.maven.apache.org/",
    "twitter" at "http://maven.twttr.com/"
)

lazy val slf4jDependencies = Seq(
    "log4j" % "log4j" % "1.2.16",
    "org.slf4j" % "slf4j-log4j12" % "1.7.5",
    "org.slf4j" % "slf4j-api" % "1.7.5"
)

libraryDependencies ++= Seq(
     // Using the ScalaTest library (only for testing)
     "org.scalatest" %% "scalatest" % "3.0.0" % "test",
     // Using the very useful utils by twitter
     "com.twitter" %% "util-collection" % "6.35.0",
     // Using scalaz for memoization
     "org.scalaz" %% "scalaz-core" % "7.2.5",
     // Manipulating in memory graphs
     "org.graphstream" % "gs-core" %  "1.3",
     "org.graphstream" % "gs-algo" %  "1.3" ) ++ slf4jDependencies

//wartremoverErrors ++= Warts.allBut(Wart.NoNeedForMonad)

// Import setting to help us automatically create project using 'np'
//seq(npSettings: _*)
