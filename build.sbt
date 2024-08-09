import xerial.sbt.Sonatype._

import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq, _}

//
// Environment variables used by the build:
// GRAPHVIZ_DOT_PATH - Full path to Graphviz dot utility. If not defined, Scaladocs will be built without diagrams.
// JAR_BUILT_BY      - Name to be added to Jar metadata field "Built-By" (defaults to System.getProperty("user.name")
//

val projectVersion = "0.10.0"
val versionTagDir  = if (projectVersion.endsWith("SNAPSHOT")) "master" else "v." + projectVersion
val _scalaVersions = Seq("3.3.3", "2.13.14", "2.12.19")
val _scalaVersion  = _scalaVersions.head

ThisBuild / version            := projectVersion
ThisBuild / crossScalaVersions := _scalaVersions
ThisBuild / scalaVersion       := _scalaVersion
ThisBuild / organization       := "org.scalafx"

publishArtifact := false
publish / skip  := true

lazy val libEnumeratum     = "com.beachape"               %% "enumeratum"          % "1.7.4"
lazy val libLogbackClassic = "ch.qos.logback"              % "logback-classic"     % "1.5.6"
lazy val libParadise       = "org.scalamacros"             % "paradise"            % "2.1.1" cross CrossVersion.full
lazy val libScalaLogging   = "com.typesafe.scala-logging" %% "scala-logging"       % "3.9.5"
lazy val libScalaFX        = "org.scalafx"                %% "scalafx"             % "22.0.0-R33"
lazy val libScalaFXML      = "org.scalafx"                %% "scalafxml-core-sfx8" % "0.5"
lazy val libScalaTest      = "org.scalatest"              %% "scalatest"           % "3.2.19"
lazy val libScalaReflect   = "org.scala-lang"              % "scala-reflect"

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
  )
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
  publishArtifact := false,
  libraryDependencies ++= Seq(
    libScalaLogging,
    libLogbackClassic % Runtime
  )
).dependsOn(scalaFXExtras % "compile;test->test")

// Resolvers
// Add snapshots to the root project to enable compilation with Scala SNAPSHOT compiler,
// e.g., 2.11.0-SNAPSHOT
resolvers ++= Resolver.sonatypeOssRepos("snapshots")

// Common settings
lazy val scalaFXExtrasSettings = Seq(
  // SAdd version specific directories
  Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / versionSubDir(scalaVersion.value),
  Test / unmanagedSourceDirectories += (Test / sourceDirectory).value / versionSubDir(scalaVersion.value),
  //
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-encoding",
    "utf8",
    "-feature",
    "-release",
    "8"
  ) ++
    (
      if (isScala2(scalaVersion.value))
        Seq(
          "-explaintypes",
          "-Xcheckinit",
          "-Xsource:3"
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
    if (isScala2(scalaVersion.value))
      Seq(
        s"-doc-external-doc:${scalaInstance.value.libraryJars.head}#https://www.scala-lang.org/api/${scalaVersion.value}/",
        "-doc-source-url",
        "https://github.com/SscalaFX-Extras/scalafx-extras/blob/" + versionTagDir + "/scalafx/€{FILE_PATH}.scala"
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
      Seq(compilerPlugin(libParadise))
    else
      Seq.empty[sbt.ModuleID]
  ),
  libraryDependencies ++= Seq(
    libScalaFX,
    libScalaTest % "test"
  ),
  libraryDependencies ++= (
    if (isScala2(scalaVersion.value))
      Seq(
        libEnumeratum,
        libScalaReflect % scalaVersion.value,
        libScalaFXML
      )
    else
      Seq.empty[sbt.ModuleID]
  ),
  // Use `pomPostProcess` to remove dependencies marked as "provided" from publishing in POM
  // This is to avoid dependency on wrong OS version JavaFX libraries
  // See also [https://stackoverflow.com/questions/27835740/sbt-exclude-certain-dependency-only-during-publish]
  pomPostProcess := {
    node: XmlNode =>
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
  resolvers ++= Resolver.sonatypeOssRepos("snapshots"),
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

// Metadata needed by Maven Central
// See also http://maven.apache.org/pom.html#Developers
lazy val mavenCentralSettings = Seq(
  homepage  := Some(new URI("https://www.scalafx.org/").toURL),
  startYear := Some(2016),
  licenses  := Seq(("BSD", new URI("https://github.com/scalafx/scalafx-extras/blob/master/LICENSE.txt").toURL)),
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
