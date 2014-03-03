name := "elevator"

scalaVersion := "2.10.3"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor"                % "2.2.3",
  "org.scalacheck"    %% "scalacheck"                % "1.10.1" % "test",
  "org.specs2"        %% "specs2"                    % "2.3.8"  % "test",
  "org.scalaz"        %% "scalaz-scalacheck-binding" % "7.0.5"  % "test",
  "org.scalaz"        %% "scalaz-core"               % "7.0.5")
