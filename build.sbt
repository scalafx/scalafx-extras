import java.net.URL

// @formatter:off

//
// Environment variables used by the build:
// GRAPHVIZ_DOT_PATH - Full path to Graphviz dot utility. If not defined Scaladocs will be build without diagrams.
// JAR_BUILT_BY      - Name to be added to Jar metadata field "Built-By" (defaults to System.getProperty("user.name")
//

val projectVersion = "0.1.1"
val versionTagDir  = if (projectVersion.endsWith("SNAPSHOT")) "master" else "v" + projectVersion

crossScalaVersions := Seq("2.11.12", "2.12.6")
scalaVersion       := crossScalaVersions { versions => versions.head }.value

// ScalaFX project
lazy val scalaFXExtrasProject = (project in file("scalafx-extras")).settings(
  scalaFXExtrasSettings,
  description := "The ScalaFX Extras",
  fork in run := true,
  scalacOptions in(Compile, doc) ++= Seq(
    "-sourcepath", baseDirectory.value.toString,
    "-doc-root-content", baseDirectory.value + "/src/main/scala/root-doc.creole",
    "-doc-source-url", "https://github.com/SscalaFX-Extras/scalafx-extras/blob/" + versionTagDir + "/scalafx/€{FILE_PATH}.scala"
  ) ++ (Option(System.getenv("GRAPHVIZ_DOT_PATH")) match {
    case Some(path) => Seq("-diagrams", "-diagrams-dot-path", path)
    case None => Seq.empty[String]
  })
)

// ScalaFX Demos project
lazy val scalaFXExtrasDemosProject = (project in file("scalafx-extras-demos")).settings(
  scalaFXExtrasSettings,
  description := "The ScalaFX Extras demonstrations",
  fork in run := true,
  javaOptions ++= Seq(
    "-Xmx512M",
    "-Djavafx.verbose"
  ),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  publishArtifact := false
).dependsOn(scalaFXExtrasProject % "compile;test->test")

// Resolvers
lazy val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
lazy val sonatypeNexusStaging   = "Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"

// Add snapshots to root project to enable compilation with Scala SNAPSHOT compiler,
// e.g., 2.11.0-SNAPSHOT
resolvers += sonatypeNexusSnapshots

// Common settings
lazy val scalaFXExtrasSettings = Seq(
  organization := "org.scalafx",
  version := projectVersion,
  crossScalaVersions := Seq("2.11.8", "2.12.1"),
  scalaVersion := crossScalaVersions { versions => versions.head }.value,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature"),
  scalacOptions in(Compile, doc) ++= Opts.doc.title("ScalaFX Extras API"),
  scalacOptions in(Compile, doc) ++= Opts.doc.version(projectVersion),
  scalacOptions in(Compile, doc) += s"-doc-external-doc:${scalaInstance.value.libraryJar}#http://www.scala-lang.org/api/${scalaVersion.value}/",
  scalacOptions in(Compile, doc) ++= Seq("-doc-footer", s"ScalaFX Extras API v.$projectVersion"),
  javacOptions ++= Seq(
    "-target", "1.8",
    "-source", "1.8",
    "-Xlint:deprecation"),
  libraryDependencies ++= Seq(
    "org.scala-lang"  % "scala-reflect"       % scalaVersion.value,
    "org.scalafx"    %% "scalafx"             % "8.0.144-R12",
    "org.scalafx"    %% "scalafxml-core-sfx8" % "0.4",
    "org.scalatest"  %% "scalatest"           % "3.0.5" % "test"),
  autoAPIMappings := true,
  manifestSetting,
  publishSetting,
  fork in Test := true,
  parallelExecution in Test := false,
  resolvers += sonatypeNexusSnapshots,
  // print junit-style XML for CI
  testOptions in Test += {
    val t = (target in Test).value
    Tests.Argument(TestFrameworks.ScalaTest, "-u", s"$t/junitxmldir")
  },
  shellPrompt in ThisBuild := { state => "sbt:" + Project.extract(state).currentRef.project + "> " }
) ++ mavenCentralSettings

lazy val manifestSetting = packageOptions += {
  Package.ManifestAttributes(
    "Created-By" -> "Simple Build Tool",
    "Built-By" -> Option(System.getenv("JAR_BUILT_BY")).getOrElse(System.getProperty("user.name")),
    "Build-Jdk" -> System.getProperty("java.version"),
    "Specification-Title" -> name.value,
    "Specification-Version" -> version.value,
    "Specification-Vendor" -> organization.value,
    "Implementation-Title" -> name.value,
    "Implementation-Version" -> version.value,
    "Implementation-Vendor-Id" -> organization.value,
    "Implementation-Vendor" -> organization.value
  )
}

lazy val publishSetting = publishTo := version {
  version: String =>
    if (version.trim.endsWith("SNAPSHOT"))
      Some(sonatypeNexusSnapshots)
    else
      Some(sonatypeNexusStaging)
}.value

// Metadata needed by Maven Central
// See also http://maven.apache.org/pom.html#Developers
lazy val mavenCentralSettings = Seq(
  homepage := Some(new URL("http://www.scalafx.org/")),
  startYear := Some(2016),
  licenses := Seq(("BSD", new URL("https://github.com/scalafx/scalafx-extras/blob/master/LICENSE.txt"))),
  pomExtra :=
    <scm>
      <url>https://github.com/scalafx/scalafx-extras</url>
      <connection>scm:git:https://github.com/scalafx/scalafx-extras.git</connection>
    </scm>
      <developers>
        <developer>
          <id>jpsacha</id>
          <name>Jarek Sacha</name>
          <url>https://github.com/jpsacha</url>
        </developer>
      </developers>
)
