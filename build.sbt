name := """play-scala-request-scoped-di-example"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.0.0" % Test,
  "io.methvin.fastforward" %% "macros" % "0.0.1" % "provided"
)

resolvers +=
  "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
