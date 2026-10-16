import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters._

val scala_version = "2.12.11"

// sbt-native-image 0.3.1 asks Coursier for graalvm-java11:20.2.0, which is no
// longer in the JVM index and has no macOS arm64 build. Prefer a local GraalVM
// that already includes the native-image tool.
def installedGraalHome: Option[java.nio.file.Path] = {
  val fromEnv = Seq("GRAALVM_HOME", "GRAAL_HOME").flatMap(sys.env.get).map(Paths.get(_))
  val sdkmanRoot = Paths.get(sys.props("user.home"), ".sdkman", "candidates", "java")
  val fromSdkman =
    if (Files.isDirectory(sdkmanRoot))
      Files.list(sdkmanRoot).iterator.asScala.toList
        .filter(_.getFileName.toString != "current")
    else Nil
  (fromEnv ++ fromSdkman).find { home =>
    Files.isExecutable(home.resolve("bin").resolve("native-image"))
  }
}

libraryDependencies += "com.lihaoyi" %% "pprint" % "0.7.0"

libraryDependencies += "com.lihaoyi" %% "fastparse" % "2.2.2"

libraryDependencies += "junit"             % "junit-dep"      % "4.10"  % "test"

libraryDependencies += "org.scalatest"     % "scalatest_2.12" % "3.1.1" % "test"

lazy val bayestensor = (project in file("."))
    .enablePlugins(NativeImagePlugin)
    .settings(
      scalaVersion := scala_version,
      Compile / mainClass := Some("bayestensor.driver.Driver"),
      nativeImageVersion := "22.3.1",
      nativeImageInstalled := installedGraalHome.isDefined,
      nativeImageOptions ++= Seq(
        "--no-fallback",
        "--report-unsupported-elements-at-runtime"
      ),
      nativeImageGraalHome := installedGraalHome.getOrElse {
        val coursier = nativeImageCoursier.value.getAbsolutePath
        Paths.get(
          scala.sys.process
            .Process(
              Seq(
                coursier,
                "java-home",
                "--jvm-index",
                "cs",
                "--jvm",
                s"${nativeImageJvm.value}:${nativeImageVersion.value}"
              )
            )
            .!!
            .trim
        )
      }
    )