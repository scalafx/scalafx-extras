import java.net.URL
import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq, _}

// @formatter:off

//
// Environment variables used by the build:
// GRAPHVIZ_DOT_PATH - Full path to Graphviz dot utility. If not defined Scaladocs will be build without diagrams.
// JAR_BUILT_BY      - Name to be added to Jar metadata field "Built-By" (defaults to System.getProperty("user.name")
//

val projectVersion = "0.5.0"
val versionTagDir  = if (projectVersion.endsWith("SNAPSHOT")) "master" else "v." + projectVersion
val _scalaVersions = Seq("3.0.2", "2.13.7", "2.12.15")
val _scalaVersion  = _scalaVersions.head
val _javaFXVersion = "17.0.1"

ThisBuild / version             := projectVersion
ThisBuild / crossScalaVersions  := _scalaVersions
ThisBuild / scalaVersion        := _scalaVersion
ThisBuild / sonatypeProfileName := "org.scalafx"

publishArtifact     := false
publish / skip      := true

lazy val OSName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _                            => throw new Exception("Unknown platform!")
}

lazy val JavaFXModuleNames = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
lazy val JavaFXModuleLibsProvided: Seq[ModuleID] =
  JavaFXModuleNames.map(m => "org.openjfx" % s"javafx-$m" % _javaFXVersion % "provided" classifier OSName)
lazy val JavaFXModuleLibs: Seq[ModuleID] =
  JavaFXModuleNames.map(m => "org.openjfx" % s"javafx-$m" % _javaFXVersion classifier OSName)

def isScala2(scalaVersion: String): Boolean = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, _)) => true
    case _            => false
  }
}

def isScala2_12(scalaVersion: String): Boolean = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 12)) => true
    case _             => false
  }
}

def isScala2_13(scalaVersion: String): Boolean = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, 13)) => true
    case _             => false
  }
}

// Add src/main/scala-3- for Scala 2.13 and older
//   and src/main/scala-3+ for Scala versions older than 3 and newer
def versionSubDir(scalaVersion: String): String =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, _)) => "scala-2"
    case Some((3, _)) => "scala-3"
    case _            => throw new Exception(s"Unsupported scala version $scalaVersion")
  }

// ScalaFX Extras project
lazy val scalaFXExtras = (project in file("scalafx-extras")).settings(
  scalaFXExtrasSettings,
  name        := "scalafx-extras",
  description := "The ScalaFX Extras",
  Compile / doc / scalacOptions ++= Seq(
    "-sourcepath",
    baseDirectory.value.toString,
    "-doc-root-content",
    baseDirectory.value + "/src/main/scala/root-doc.creole"
  ),
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
  libraryDependencies ++= JavaFXModuleLibs,
  publishArtifact := false,
  libraryDependencies ++= Seq(
    "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.4",
    "ch.qos.logback"              % "logback-classic" % "1.2.8"
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
  scalaVersion       := _scalaVersion,
  // SAdd version specific directories
  Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / versionSubDir(scalaVersion.value),
  Test / unmanagedSourceDirectories += (Test / sourceDirectory).value / versionSubDir(scalaVersion.value),
  //
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-encoding",
    "utf8",
    "-feature"
  ) ++
    (
      if (isScala2(scalaVersion.value))
        Seq(
          "-explaintypes",
          "-Xcheckinit",
          "-Xsource:3",
//          "-Xlint",
//          "-Xcheckinit",
//          "-Xlint:missing-interpolator",
//          "-Ywarn-dead-code",
//          "-Ywarn-unused:-patvars,_",
        )
      else
        Seq(
          "-explain",
          "-explain-types"
        )
    ),
  Compile / doc / scalacOptions ++= Opts.doc.title("ScalaFX Extras API"),
  Compile / doc / scalacOptions ++= Opts.doc.version(projectVersion),
  Compile / doc / scalacOptions ++= Seq("-doc-footer", s"ScalaFX Extras API v.$projectVersion"),
  Compile / doc / scalacOptions ++= (
    if(isScala2(scalaVersion.value))
      Seq(
        s"-doc-external-doc:${scalaInstance.value.libraryJars.head}#http://www.scala-lang.org/api/${scalaVersion.value}/",
        "-doc-source-url", "https://github.com/SscalaFX-Extras/scalafx-extras/blob/" + versionTagDir + "/scalafx/€{FILE_PATH}.scala"
      ) ++ (
        Option(System.getenv("GRAPHVIZ_DOT_PATH")) match {
          case Some(path) => Seq("-diagrams", "-diagrams-dot-path", path)
          case None       => Seq.empty[String]
        }
      )
    else
      Seq.empty[String]
  ),
  // If using Scala 2.13 or better, enable macro processing through compiler option
  scalacOptions += (if (isScala2_13(scalaVersion.value)) "-Ymacro-annotations" else ""),
  // If using Scala 2.12 or lower, enable macro processing through compiler plugin
  libraryDependencies ++= (
    if (isScala2_12(scalaVersion.value))
      Seq(compilerPlugin(
        "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
      ))
    else
      Seq.empty[sbt.ModuleID]
  ),
  javacOptions ++= Seq(
       "-target", "1.8",
       "-source", "1.8",
    "-Xlint:deprecation"
  ),
  libraryDependencies ++= Seq(
    "org.scalafx"   %% "scalafx"   % "17.0.1-R26",
    "org.scalatest" %% "scalatest" % "3.2.10" % "test"
  ) ++ JavaFXModuleLibsProvided,
  libraryDependencies ++= (
    if (isScala2(scalaVersion.value))
      Seq(
        "com.beachape"  %% "enumeratum"          % "1.7.0",
        "org.scala-lang" % "scala-reflect"       % scalaVersion.value,
        "org.scalafx"   %% "scalafxml-core-sfx8" % "0.5"
      )
    else
      Seq.empty[sbt.ModuleID]
  ),
  // Use `pomPostProcess` to remove dependencies marked as "provided" from publishing in POM
  // This is to avoid dependency on wrong OS version JavaFX libraries
  // See also [https://stackoverflow.com/questions/27835740/sbt-exclude-certain-dependency-only-during-publish]
  pomPostProcess := { node: XmlNode =>
    new RuleTransformer(new RewriteRule {
      override def transform(node: XmlNode): XmlNodeSeq = node match {
        case e: Elem if e.label == "dependency" && e.child.exists(c => c.label == "scope" && c.text == "provided") =>
          val organization = e.child.filter(_.label == "groupId").flatMap(_.text).mkString
          val artifact     = e.child.filter(_.label == "artifactId").flatMap(_.text).mkString
          val version      = e.child.filter(_.label == "version").flatMap(_.text).mkString
          Comment(s"provided dependency $organization#$artifact;$version has been omitted")
        case _ => node
      }
    }).transform(node).head
  },
  autoAPIMappings := true,
  manifestSetting,
  run / fork               := true,
  Test / fork              := true,
  Test / parallelExecution := false,
  resolvers += Resolver.sonatypeRepo("snapshots"),
  // print junit-style XML for CI
  Test / testOptions += {
    val t = (Test / target).value
    Tests.Argument(TestFrameworks.ScalaTest, "-u", s"$t/junitxmldir")
  }
) ++ mavenCentralSettings

lazy val manifestSetting = packageOptions += {
  Package.ManifestAttributes(
    "Created-By"               -> "Simple Build Tool",
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

import xerial.sbt.Sonatype._

// Metadata needed by Maven Central
// See also http://maven.apache.org/pom.html#Developers
lazy val mavenCentralSettings = Seq(
  homepage               := Some(new URL("http://www.scalafx.org/")),
  startYear              := Some(2016),
  licenses               := Seq(("BSD", new URL("https://github.com/scalafx/scalafx-extras/blob/master/LICENSE.txt"))),
  sonatypeProfileName    := "org.scalafx",
  sonatypeProjectHosting := Some(GitHubHosting("org.scalafx", "scalafx-extras", "jpsacha@gmail.com")),
  publishMavenStyle      := true,
  publishTo              := sonatypePublishToBundle.value,
  developers := List(
    Developer(
      id = "jpsacha",
      name = "Jarek Sacha",
      email = "jpsacha@gmail.com",
      url = url("https://github.com/jpsacha")
    )
  )
)
