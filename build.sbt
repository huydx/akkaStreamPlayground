import scalariform.formatter.preferences._

name := "akka-stream-scala"

version := "1.1"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-stream-experimental_2.11" % "1.0-M3",
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.aerospike" % "aerospike-client" % "3.1.1"
)

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PreserveDanglingCloseParenthesis, true)


fork in run := true