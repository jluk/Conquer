name := "conquer"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.5"

libraryDependencies ++= Seq(jdbc, anorm, cache, ws)

resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "org.reactivemongo" 		%% 	"play2-reactivemongo" 		% "0.10.5.0.akka23",
  "org.webjars" 			%% 	"webjars-play" 				% "2.3.0",
  "org.webjars" 			%	"bootstrap" 				% "3.1.1-1",
  "org.webjars" 			% 	"bootswatch-united"			% "3.1.1",
  "org.webjars" 			% 	"html5shiv" 				% "3.7.0",
  "org.webjars" 			% 	"respond" 					% "1.4.2"
)
