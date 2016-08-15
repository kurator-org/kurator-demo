name := "play-java"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

resolvers += Resolver.mavenLocal

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs
)

libraryDependencies ++= Seq(
  "org.python" % "jython-standalone" % "2.7.1b3",
  "com.esotericsoftware.yamlbeans" % "yamlbeans" % "1.09",
  "net.sf.jopt-simple" % "jopt-simple" % "3.2",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "org.yaml" % "snakeyaml" % "1.13"
)