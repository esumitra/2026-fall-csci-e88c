name := "spark"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "4.0.2" % Provided,
  "org.apache.spark" %% "spark-sql"  % "4.0.2" % Provided
)

Compile / mainClass := Some("org.cscie88c.spark.SparkJob")

assembly / mainClass := Some("org.cscie88c.spark.SparkJob")
assembly / assemblyJarName := "SparkJob.jar"
assembly / test := {}
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case "application.conf"            => MergeStrategy.concat
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
// see shading feature at https://github.com/sbt/sbt-assembly#shading
assembly / assemblyShadeRules := Seq(
  ShadeRule.rename("shapeless.**" -> "shadeshapeless.@1").inAll
)
