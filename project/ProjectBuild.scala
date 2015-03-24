import sbt._
import sbt.Keys._

object ProjectBuild extends Build {
  import Settings._

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = parentSettings,
    aggregate = Seq(java7Future, guavaFuture, scalaFuture)
  )

  lazy val java7Future = Project(
    id = "java7-future",
    base = file("./java7-future"),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.java7)
  )

  lazy val guavaFuture = Project(
    id = "guava-future",
    base = file("./guava-future"),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.guava)
  )

  lazy val scalaFuture = Project(
    id = "scala-future",
    base = file("./scala-future"),
    settings = defaultSettings ++ Seq(libraryDependencies ++= Dependencies.scala)
  )
}

object Dependencies {
  import Versions._

  object Compile {
    val httpClient = "org.apache.httpcomponents" %% "httpclient" % HttpClientVer
  }

  object Test {
    val scalatest     = "org.scalatest"           %% "scalatest"            % ScalaTestVer      % "test"
    val junit         = "junit"                    % "junit"                % JunitVer          % "test"
    val junitInterf   = "com.novocode"             % "junit-interface"      % NovocodeVer       % "test"
  }

  /** Module deps */

  val java7 = Seq(Test.junit, Test.junitInterf)
  val guava = Seq(Test.junit)
  val scala = Seq(Test.scalatest)
}
