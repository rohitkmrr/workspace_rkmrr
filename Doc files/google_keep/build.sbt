name := """google_keep"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)
lazy val myProject = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
 "com.nimbusds" % "nimbus-jose-jwt" % "4.11",
  javaWs,
"mysql" % "mysql-connector-java" % "5.1.39"
)
