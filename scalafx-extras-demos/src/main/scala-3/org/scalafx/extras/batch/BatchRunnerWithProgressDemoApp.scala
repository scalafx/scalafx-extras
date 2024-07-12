/*
 * Copyright (c) 2000-2023 Jarek Sacha. All Rights Reserved.
 * Author's e-mail: jpsacha at gmail.com
 */

package org.scalafx.extras.batch

import org.scalafx.extras.BusyWorker
import org.scalafx.extras.BusyWorker.SimpleTask
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label}
import scalafx.scene.layout.VBox
import scalafx.stage.Window

import scala.util.Random

/**
 * Example of a task that for batch execution.
 */
private class MyTask(val i: Int) extends ItemTask[String] {

  import MyTask.*

  val name = s"Task #$i"

  def run(): String =
    val t = minTime + new Random().nextInt(maxTime - minTime)
    Thread.sleep(t)
    if t % 6 != 0 then
      s"name t = $t"
    else
      throw new Exception(s"Do not like $t")
}

object MyTask {
  val minTime = 500
  val maxTime = 1000
}

/**
 * Demo of `BatchRunnerWithProgress` GUI
 * @author Jarek Sacha
 */
object BatchRunnerProgressDemoApp extends JFXApp3:

  private lazy val busyWorker = new BusyWorker(Title, parentWindow)
  private val Title           = "Batch Processing / Progress Dialog Demo"
  private val nTasks          = 10

  override def start(): Unit =
    stage = new PrimaryStage:
      title = Title
      scene = new Scene:
        content = new VBox:
          padding = Insets(21)
          spacing = 14
          children = Seq(
            new Label(
              s"""Press "Run x" to initiate processing.
                 |Wait till processing finished or press "Abort".
                 |There will be $nTasks processed.
                 |Tasks will have randomly generated execution time,
                 |between ${MyTask.minTime} and ${MyTask.maxTime} ms.
                 |If task's time is divisible by 6, that task will fail.
                 |""".stripMargin
            ),
            new Button("Run as Sequence"):
              onAction = _ => onStart(false)
              prefWidth = 120
            ,
            new Button("Run in Parallel"):
              onAction = _ => onStart(true)
              prefWidth = 120
          )

  private def onStart(runInParallel: Boolean): Unit = busyWorker.doTask("Start") { () =>
    type ResultType = String
    val helper =
      new BatchRunnerWithProgress[ResultType]("Sample batch processing", Option(stage), runInParallel):
        def createTasks(): Seq[ItemTask[ResultType]] = (1 to nTasks).map { i => new MyTask(i) }

    helper.run()
  }

  private def parentWindow: Option[Window] = Option(stage)

end BatchRunnerProgressDemoApp
