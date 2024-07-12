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

object BatchRunnerProgressHelperDemoApp extends JFXApp3:

  private lazy val busyWorker = new BusyWorker(Title, parentWindow)
  private val Title = "Batch Processing / Progress Dialog Demo"

  private val nTasks = 100
  private val minTime = 500
  private val maxTime = 1000

  override def start(): Unit =
    stage = new PrimaryStage:
      title = Title
      scene = new Scene:
        content = new VBox:
          padding = Insets(21)
          spacing = 14
          children = Seq(
            new Label(
              s"""Press "Run" to initiate processing.
                 |Wait till processing finished or press "Abort".
                 |There will be $nTasks processed. 
                 |Tasks will have randomly generated time between $minTime and $maxTime ms.
                 |If task's time is divisible by 6, that task will fail.
                 |""".stripMargin
            ),
            new Button("Run Sequential"):
              onAction = (_) => onStart(false)
              prefWidth = 120
            ,
            new Button(" Run Parallel "):
              onAction = (_) => onStart(false)
              prefWidth = 120
          )

  private def onStart(useParallelProcessing: Boolean): Unit = busyWorker.doTask("Start") {
    new SimpleTask[Unit]:
      override def call(): Unit =
        val helper =
          new BatchRunnerProgressHelper[String]("Sample batch processing", parentWindow, useParallelProcessing):
            override def createSampleTasks(): Seq[ItemTask[String]] =
              val r = new Random()
              (1 to nTasks).map { i =>
                new ItemTask[String]:
                  override val name = s"Task #$i"

                  override def run(): String =
                    val t = minTime + r.nextInt(maxTime - minTime)
                    Thread.sleep(t)
                    if t % 6 == 0 then throw new Exception(s"Do not like ${t}")
                    s"name t = $t"
              }
        helper.run()
  }

  def parentWindow: Option[Window] = Option(stage)
