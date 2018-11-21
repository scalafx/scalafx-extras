import java.net.URL

// @formatter:off

//
// Environment variables used by the build:
// GRAPHVIZ_DOT_PATH - Full path to Graphviz dot utility. If not defined Scaladocs will be build without diagrams.
// JAR_BUILT_BY      - Name to be added to Jar metadata field "Built-By" (defaults to System.getProperty("user.name")
//

val projectVersion = "0.2.2-SNAPSHOT"
val versionTagDir  = if (projectVersion.endsWith("SNAPSHOT")) "master" else "v" + projectVersion
val _scalaVersions = Seq("2.12.7")
val _scalaVersion = _scalaVersions.head

crossScalaVersions := _scalaVersions
scalaVersion := _scalaVersion

lazy val OSName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _                            => throw new Exception("Unknown platform!")
}
  
lazy val JavaFXModuleNames = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
lazy val JavaFXModuleLibs: Seq[ModuleID] = 
  JavaFXModuleNames.map(m => "org.openjfx" % s"javafx-$m" % "11" classifier OSName)

// ScalaFX Extras project
lazy val scalaFXExtras = (project in file("scalafx-extras")).settings(
  scalaFXExtrasSettings,
  name        := "scalafx-extras",
  description := "The ScalaFX Extras",
  scalacOptions in(Compile, doc) ++= Seq(
    "-sourcepath", baseDirectory.value.toString,
    "-doc-root-content", baseDirectory.value + "/src/main/scala/root-doc.creole",
    "-doc-source-url", "https://github.com/SscalaFX-Extras/scalafx-extras/blob/" + versionTagDir + "/scalafx/€{FILE_PATH}.scala"
  ) ++ (Option(System.getenv("GRAPHVIZ_DOT_PATH")) match {
    case Some(path) => Seq("-diagrams", "-diagrams-dot-path", path)
    case None => Seq.empty[String]
  }) ++ (if (_scalaVersion.startsWith("2.11")) Seq("-Xexperimental") else Seq.empty[String])
)

// ScalaFX Extras Demos project
lazy val scalaFXExtrasDemos = (project in file("scalafx-extras-demos")).settings(
  scalaFXExtrasSettings,
  name        := "scalafx-extras-demos",
  description := "The ScalaFX Extras demonstrations",
  javaOptions ++= Seq(
    "-Xmx512M",
    "-Djavafx.verbose"
  ),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
  publishArtifact := false,
  libraryDependencies ++= Seq(
    "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.0",
    "ch.qos.logback"              % "logback-classic" % "1.2.3"
  )
).dependsOn(scalaFXExtras % "compile;test->test")

// Resolvers
// Add snapshots to root project to enable compilation with Scala SNAPSHOT compiler,
// e.g., 2.11.0-SNAPSHOT
resolvers += Resolver.sonatypeRepo("snapshots")

// Common settings
lazy val scalaFXExtrasSettings = Seq(
  organization       := "org.scalafx",
  version            := projectVersion,
  crossScalaVersions := _scalaVersions,
  scalaVersion := _scalaVersion,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-Xcheckinit", "-encoding", "utf8", "-feature"),
  scalacOptions in(Compile, doc) ++= (if (_scalaVersion.startsWith("2.11")) Seq("-Xexperimental") else Seq.empty[String]),
  scalacOptions in(Compile, doc) ++= Opts.doc.title("ScalaFX Extras API"),
  scalacOptions in(Compile, doc) ++= Opts.doc.version(projectVersion),
  scalacOptions in(Compile, doc) += s"-doc-external-doc:${scalaInstance.value.libraryJar}#http://www.scala-lang.org/api/${scalaVersion.value}/",
  scalacOptions in(Compile, doc) ++= Seq("-doc-footer", s"ScalaFX Extras API v.$projectVersion"),
  javacOptions ++= Seq(
    //    "-target", "1.8",
    //    "-source", "1.8",
    "-Xlint:deprecation"),
  libraryDependencies ++= Seq(
    "com.beachape"   %% "enumeratum"          % "1.5.13",
    "org.scala-lang"  % "scala-reflect"       % scalaVersion.value,
    "org.scalafx"    %% "scalafx"             % "11-R16",
    "org.scalafx"    %% "scalafxml-core-sfx8" % "0.4",
    "org.scalatest"  %% "scalatest"           % "3.0.5" % "test"
  ) ++ JavaFXModuleLibs,
  autoAPIMappings := true,
  manifestSetting,
  publishSetting,
  fork in run := true,
  fork in Test := true,
  parallelExecution in Test := false,
  resolvers += Resolver.sonatypeRepo("snapshots"),
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
    "Built-By"                 -> Option(System.getenv("JAR_BUILT_BY")).getOrElse(System.getProperty("user.name")),
    "Build-Jdk"                -> System.getProperty("java.version"),
    "Specification-Title"      -> name.value,
    "Specification-Version"    -> version.value,
    "Specification-Vendor"     -> organization.value,
    "Implementation-Title"     -> name.value,
    "Implementation-Version"   -> version.value,
    "Implementation-Vendor-Id" -> organization.value,
    "Implementation-Vendor"    -> organization.value
  )
}

lazy val publishSetting = publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

// Metadata needed by Maven Central
// See also http://maven.apache.org/pom.html#Developers
lazy val mavenCentralSettings = Seq(
  homepage  := Some(new URL("http://www.scalafx.org/")),
  startYear := Some(2016),
  licenses  := Seq(("BSD", new URL("https://github.com/scalafx/scalafx-extras/blob/master/LICENSE.txt"))),
  pomExtra  :=
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
