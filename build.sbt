name := """reactive-workshop"""

version := "0.43"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.2"

val akka = "2.3.6"

libraryDependencies ++= Seq(
  ws,
  "org.webjars" % "bootstrap" % "3.1.1",
  "org.webjars" % "jquery" % "2.1.0-2",
  "org.webjars" % "angularjs" % "1.2.16",
  "org.webjars" % "angular-leaflet-directive" % "0.7.6",
  "com.typesafe.akka" %% "akka-contrib" % akka,
  "com.typesafe.akka" %% "akka-cluster" % akka,
  "com.typesafe.akka" %% "akka-testkit" % akka % "test",
  "org.scalatestplus" %% "play" % "1.1.0" % "test"
)

// Apply digest calculation and gzip compression to assets
pipelineStages := Seq(digest, gzip)

addCommandAlias("rb", "runMain backend.MainTweetLoader 2552 -Dakka.remote.netty.tcp.port=2552 -Dakka.cluster.roles.0=backend-loader")