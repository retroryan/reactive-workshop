name := """reactive-workshop"""

version := "0.5"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.4"

val akka_version = "2.3.7"

libraryDependencies ++= Seq(
    ws, // Play's web services module
    "org.webjars" % "bootstrap" % "2.3.1",
    "org.webjars" % "flot" % "0.8.0",
    "org.webjars" % "angularjs" % "1.2.16",
    "com.typesafe.akka" %% "akka-actor" % akka_version,
    "com.typesafe.akka" %% "akka-slf4j" % akka_version,
    "com.typesafe.akka" %% "akka-testkit" % akka_version % "test",
    "com.typesafe.akka" %% "akka-cluster" % akka_version,
    "com.typesafe.akka" %% "akka-contrib" % akka_version,
    "com.typesafe.akka" %% "akka-persistence-experimental" % akka_version exclude("org.iq80.leveldb","leveldb"),
    "org.iq80.leveldb"  %  "leveldb" % "0.7",
    "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2"
)

addCommandAlias("rb", "runMain backend.MainClusterManager -Dakka.remote.netty.tcp.port=2555 -Dakka.cluster.roles.0=backend")

addCommandAlias("rb2", "runMain backend.MainClusterManager -Dakka.remote.netty.tcp.port=2556 -Dakka.cluster.roles.0=backend")

addCommandAlias("sj", "runMain backend.journal.SharedJournalApp -Dakka.remote.netty.tcp.port=2560 -Dakka.cluster.roles.0=shared-journal")