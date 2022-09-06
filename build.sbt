enablePlugins(JavaAppPackaging)

lazy val root = (project in file(".")).settings(
  name         := "b-threads",
  organization := "dev.kaden",
  scalaVersion := "3.1.3",
  libraryDependencies ++= Seq(
    // "core" module - IO, IOApp, schedulers
    // This pulls in the kernel and std modules automatically.
    "org.typelevel" %% "cats-effect" % "3.3.12",
    // concurrency abstractions and primitives (Concurrent, Sync, Async etc.)
    "org.typelevel" %% "cats-effect-kernel" % "3.3.12",
    // standard "effect" library (Queues, Console, Random etc.)
    "org.typelevel" %% "cats-effect-std" % "3.3.12",
    // Effectful testing via Weaver
    "com.disneystreaming" %% "weaver-cats" % "0.7.6" % Test
  ),
  testFrameworks += new TestFramework("weaver.framework.CatsEffect"),
  // Effectful logging via Log4Cats -> SLF4J -> Logback
  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.6",
    "org.typelevel" %% "log4cats-slf4j"  % "2.4.0"
  )
  // dockerExposedPorts ++= Seq(9000, 9001)
)
