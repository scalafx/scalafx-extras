import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{Node as XmlNode, NodeSeq as XmlNodeSeq, *}

//
// Environment variables used by the build:
// JAR_BUILT_BY      - Name to be added to Jar metadata field "Built-By" (defaults to System.getProperty("user.name")
//

ThisBuild / version := "0.12.1.1-SNAPSHOT"
ThisBuild / scalaVersion := "3.3.9"
ThisBuild / organization := "org.scalafx"
ThisBuild / organizationName := "ScalaFX"
ThisBuild / organizationHomepage := Some(uri("https://github.com/scalafx"))
ThisBuild / homepage := Some(uri("https://www.scalafx.org/"))
ThisBuild / startYear := Some(2016)
ThisBuild / licenses := Seq(("BSD", uri("https://github.com/scalafx/scalafx-extras/blob/master/LICENSE.txt")))
ThisBuild / scmInfo              := Option(
  ScmInfo(
    uri("https://github.com/scalafx/scalafx-extras"),
    "scm:https://github.com/scalafx/scalafx-extras.git"
  )
)

// Enable use of SNAPSHOT dependencies, e.g., ScalaFX SNAPSHOT
ThisBuild / resolvers += Resolver.sonatypeCentralSnapshots

lazy val root = project.in(file("."))
  .aggregate(scalaFXExtras, scalaFXExtrasDemos)
  .settings(
    name := "scalafx-extras-root",
    publishArtifact := false,
    publish / skip := true
  )

// Set the Java version target for compatibility for the current FIJI distribution
// We do not want to be over the FIJI Java version.
lazy val javaTargetVersion = "25"

lazy val libLogbackClassic = "ch.qos.logback"              % "logback-classic" % "1.6.6"
lazy val libScalaLogging   = "com.typesafe.scala-logging" %% "scala-logging"   % "3.9.6"
lazy val libScalaFX        = "org.scalafx"                %% "scalafx"         % "27.0.0-R39"
lazy val libScalaTest      = "org.scalatest"              %% "scalatest"       % "3.2.20"

// ScalaFX Extras project
lazy val scalaFXExtras = project.in(file("scalafx-extras"))
  .settings(
    name        := "scalafx-extras",
    description := "The ScalaFX Extras",
    commonSettings,
    Compile / doc / scalacOptions ++= Seq(
      "-sourcepath",
      baseDirectory.value.toString,
      "-doc-root-content",
      (baseDirectory.value / "src/main/scala/root-doc.creole").toString
    ),
    publishArtifact := true,
    Test / publishArtifact := false,
    Test / publish / skip  := true
  )

// ScalaFX Extras Demos project
lazy val scalaFXExtrasDemos = project.in(file("scalafx-extras-demos")).settings(
  name        := "scalafx-extras-demos",
  description := "The ScalaFX Extras demonstrations",
  commonSettings,
  javaOptions ++= Seq(
    "-Xmx512M",
    "-Djavafx.verbose"
  ),
  publishArtifact := false,
  publish / skip  := true,
  // There are many demo applications, do not pick one as the jar's main class
  Compile / packageBin / mainClass := None,
  // Ask which demo to run every time `run` is called (sbt 2 caches the selected main class)
  Compile / selectMainClass := Def.uncached {
    (Compile / mainClass).value orElse Defaults.askForMainClass((Compile / discoveredMainClasses).value)
  },
  Compile / run / mainClass := Def.uncached((Compile / run / selectMainClass).value),
  libraryDependencies ++= Seq(
    libScalaLogging,
    libLogbackClassic % Runtime
  )
).dependsOn(scalaFXExtras % "compile;test->test")

// Common settings
lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-encoding",
    "UTF-8",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-explain",
    "-explain-types",
//    "-rewrite",
//    "-source:3.3-migration",
    "-Wunused:all",
    // To deal with classes implementing `ControllerFX` and using @FXML annotations to passing variables from FXML declarations
    // or "-Wunused:-privates"
    "-Wconf:msg=unset private var:s",
    "-release",
    javaTargetVersion
  ),
  // sbt sets Scaladoc `-project` to the module name, replace it with the API title
  Compile / doc / scalacOptions := {
    val opts = (Compile / doc / scalacOptions).value
    val i = opts.indexOf("-project")
    (if (i >= 0) opts.patch(i, Nil, 2) else opts) ++ Seq(
      "-project",
      "ScalaFX Extras API",
      "-project-version",
      version.value,
      "-project-footer",
      s"ScalaFX Extras API v.${version.value}"
    )
  },
  Compile / compile / javacOptions ++= Seq("-deprecation", "-Xlint", "--release", javaTargetVersion),
  //
  libraryDependencies ++= Seq(
    libScalaFX,
    libScalaTest % "test"
  ),
  // Use `pomPostProcess` to remove dependencies marked as "provided" from publishing in POM
  // This is to avoid dependency on a wrong OS version of JavaFX libraries
  // See also [https://stackoverflow.com/questions/27835740/sbt-exclude-certain-dependency-only-during-publish]
  pomPostProcess := {
    (node: XmlNode) =>
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
  // Forked JVMs: allow JavaFX to load native libraries and Scala 3.3 `LazyVals` to use `sun.misc.Unsafe`
  // without JDK warnings
  javaOptions ++= Seq(
    "--enable-native-access=ALL-UNNAMED",
    "--sun-misc-unsafe-memory-access=allow"
  )
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

//
// Publishing to Maven Central
//
ThisBuild / publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
ThisBuild / developers := List(
  Developer(
    id = "jpsacha",
    name = "Jarek Sacha",
    email = "jpsacha@gmail.com",
    url = uri("https://github.com/jpsacha")
  )
)
