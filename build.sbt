import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node as XmlNode, NodeSeq as XmlNodeSeq, *}

//
// Environment variables used by the build:
// GRAPHVIZ_DOT_PATH - Full path to Graphviz dot utility. If not defined, Scaladocs will be built without diagrams.
// JAR_BUILT_BY      - Name to be added to Jar metadata field "Built-By" (defaults to System.getProperty("user.name")
//

lazy val projectVersion    = "0.11.0.1-SNAPSHOT"
lazy val versionTagDir     = if (projectVersion.endsWith("SNAPSHOT")) "master" else "v." + projectVersion
lazy val javaTargetVersion = "21"

ThisBuild / version          := projectVersion
ThisBuild / scalaVersion     := "3.3.7"
ThisBuild / organization     := "org.scalafx"
ThisBuild / organizationName := "ScalaFX"
ThisBuild / homepage         := Some(url("https://www.scalafx.org/"))
ThisBuild / startYear        := Some(2016)
ThisBuild / licenses         := Seq(("BSD", url("https://github.com/scalafx/scalafx-extras/blob/master/LICENSE.txt")))
ThisBuild / organizationHomepage := Some(url("https://github.com/scalafx"))
ThisBuild / scmInfo              := Option(
  ScmInfo(
    url("https://github.com/scalafx/scalafx-extras"),
    "scm:https://github.com/scalafx/scalafx-extras.git"
  )
)

// Resolvers
// Add snapshots to the root project to enable compilation with Scala SNAPSHOT compiler,
// e.g., 2.11.0-SNAPSHOT
ThisBuild / resolvers += Resolver.sonatypeCentralSnapshots
ThisBuild / resolvers += Resolver.mavenLocal

ThisBuild / publishArtifact := false
ThisBuild / publish / skip  := true

lazy val libLogbackClassic = "ch.qos.logback"              % "logback-classic" % "1.5.32"
lazy val libScalaLogging   = "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.6"
lazy val libScalaFX        = "org.scalafx"                %% "scalafx"         % "25.0.2-R37"
lazy val libScalaTest      = "org.scalatest"              %% "scalatest"       % "3.2.19"

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
  publishArtifact := true
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

// Common settings
lazy val scalaFXExtrasSettings = Seq(
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-encoding",
    "utf8",
    "-feature",
    "-release",
    javaTargetVersion,
    "-explain",
    "-explain-types",
    "-rewrite",
    "-source:3.3-migration",
    "-Wunused:all"
  ),
  Compile / doc / scalacOptions ++= Opts.doc.title("ScalaFX Extras API"),
  Compile / doc / scalacOptions ++= Opts.doc.version(projectVersion),
  Compile / doc / scalacOptions ++= Seq("-doc-footer", s"ScalaFX Extras API v.$projectVersion"),
  // If using Scala 2.12, enable macro processing through a compiler plugin
  libraryDependencies ++= Seq(
    libScalaFX,
    libScalaTest % "test"
  ),
  // Use `pomPostProcess` to remove dependencies marked as "provided" from publishing in POM
  // This is to avoid dependency on a wrong OS version of JavaFX libraries
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
  // print junit-style XML for CI
  Test / testOptions += {
    val t = (Test / target).value
    Tests.Argument(TestFrameworks.ScalaTest, "-u", s"$t/junitxmldir")
  }
)

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
ThisBuild / publishMavenStyle := true
ThisBuild / developers        := List(
  Developer(
    id = "jpsacha",
    name = "Jarek Sacha",
    email = "jpsacha@gmail.com",
    url = url("https://github.com/jpsacha")
  )
)
