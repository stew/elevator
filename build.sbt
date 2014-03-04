import org.scalatra.sbt._
import org.scalatra.sbt.PluginKeys._
import com.mojolly.scalate.ScalatePlugin._
import ScalateKeys._

name := "elevator"

scalaVersion := "2.10.3"

resolvers += "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka"           %% "akka-actor"                % "2.2.3",
  "org.scalacheck"              %% "scalacheck"                % "1.10.1" % "test",
  "org.specs2"                  %% "specs2"                    % "2.3.8"  % "test",
  "org.scalaz"                  %% "scalaz-scalacheck-binding" % "7.0.5"  % "test",
  "org.scalatra"                %% "scalatra"                  % "2.3.0-SNAPSHOT",
  "org.scalatra"                %% "scalatra-atmosphere"       % "2.3.0-SNAPSHOT",
  "org.scalatra"                %% "scalatra-scalate"          % "2.3.0-SNAPSHOT",
  "org.scalatra"                %% "scalatra-json"             % "2.3.0-SNAPSHOT",
  "org.json4s"                  %% "json4s-jackson"            % "3.2.6",
  "ch.qos.logback"              % "logback-classic"            % "1.0.6"  % "runtime",
  "org.eclipse.jetty"           % "jetty-webapp"               % "9.0.6.v20130930",
  "org.eclipse.jetty"           % "jetty-servlets"             % "9.0.6.v20130930",
  "javax.servlet"               % "javax.servlet-api"          % "3.1.0",
  "org.eclipse.jetty.websocket" % "websocket-server"           % "9.0.6.v20130930",
  "org.scalaz"                  %% "scalaz-core"               % "7.0.5")

scalateTemplateConfig in Compile <<= (sourceDirectory in Compile){ base =>
  Seq(
    TemplateConfig(
      base / "webapp" / "WEB-INF" / "templates",
      Seq.empty,  /* default imports should be added here */
      Seq(
        Binding("context", "_root_.org.scalatra.scalate.ScalatraRenderContext", importMembers = true, isImplicit = true)
      ),  /* add extra bindings here */
      Some("templates")
    )
  )}
