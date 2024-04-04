/*
 * Copyright (c) 2011-2024, ScalaFX Project
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the ScalaFX Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE SCALAFX PROJECT OR ITS CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.scalafx.extras

import org.scalafx.extras.BusyWorker.SimpleTask
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.concurrent.WorkerStateEvent
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, ProgressBar, ToolBar}
import scalafx.scene.image.Image
import scalafx.scene.layout.{BorderPane, HBox, Priority, VBox}

import java.util.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 * An application illustrating use of `BusyWorker` and handling of exception in execution of the task.
 */
object BusyWorkerErrorHandlingDemo extends JFXApp3 {

  override def start(): Unit = {

    val progressLabel = new Label("") {
      hgrow = Priority.Always
      maxWidth = Double.MaxValue
    }
    val progressBar = new ProgressBar() {
      progress = 0
    }

    // noinspection ConvertExpressionToSAM
    lazy val buttonPane = new VBox { parentNode =>
      spacing = 9
      alignment = Pos.Center
      padding = Insets(21)
      children ++= Seq(
        new Button("No errors") {
          onAction = () =>
            busyWorker.doTask("Task 1")(
              new SimpleTask[String] {
                override def call(): String = {
                  val maxItems = 10
                  for (i <- 1 to maxItems) {
                    println(i)
                    message() = s"Processing item $i/$maxItems"
                    progress() = (i - 1) / 10.0
                    Thread.sleep(250)
                  }
                  progress() = 1
                  "Done"
                }

                override def onFinish(result: Future[String], successful: Boolean): Unit = {
                  // Any onFinish after running a task would happen here.
                  println(s"Task completion was successful: '$successful'")
                  if (successful) {
                    println(s"Task produced result: '${result.get()}'")
                  }
                }
              }
            )
          maxWidth = Double.MaxValue
        },
        new Button("Exception on 3 - default error handling") {
          onAction = () =>
            busyWorker.doTask("Task 2")(
              new SimpleTask[String] {
                override def call(): String = {
                  val maxItems = 10
                  for (i <- 1 to maxItems) {
                    println(i)
                    message() = s"Processing item $i/$maxItems"
                    progress() = (i - 1) / 10.0
                    Thread.sleep(250)

                    if (i == 3) {
                      throw new Exception("Simulating task failure.")
                    }
                  }
                  progress() = 1
                  "Done"
                }

                override def onFinish(result: Future[String], successful: Boolean): Unit = {
                  // Any onFinish after running a task would happen here.
                  println(s"Task completion was successful: '$successful'")
                  if (successful) {
                    println(s"Task produced result: '${result.get()}'")
                  }
                }
              }
            )
          maxWidth = Double.MaxValue
        },
        new Button("Exception on 3 - custom error handling") {
          onAction = () =>
            busyWorker.doTask("Task 3")(
              new SimpleTask[String] {
                override def call(): String = {
                  val maxItems = 10
                  for (i <- 1 to maxItems) {
                    println(i)
                    message() = s"Processing item $i/$maxItems"
                    progress() = (i - 1) / 10.0
                    Thread.sleep(250)

                    if (i == 3) {
                      throw new Exception("Simulating task failure.")
                    }
                  }
                  progress() = 1
                  "Done"
                }

                override def onFailed(e: WorkerStateEvent): Unit =
                  // Mark event as consumed to prevent default error handlig
                  e.consume()

                override def onFinish(result: Future[String], successful: Boolean): Unit = {
                  Try(result.get()) match {
                    case Success(value) =>
                      ShowMessage.information(
                        "Task Success",
                        s"Task completion was successful: '$successful'",
                        s"Task produced result: '$value'",
                        parent.value
                      )
                    case Failure(exception) =>
                      ShowMessage.exception("Task Failure", "Custom error handling", exception, parent.value)
                  }
                }
              }
            )
          maxWidth = Double.MaxValue
        }
      ).map(_.delegate)
    }

    lazy val busyWorker: BusyWorker =
      new BusyWorker(title = "BusyWorker Error Handling Demo", disabledNode = buttonPane) {
        progressLabel.text <== progressMessage
        progressBar.progress <== progressValue
      }

    stage = new PrimaryStage {
      scene = new Scene {
        icons += new Image("/org/scalafx/extras/sfx.png")
        title = "BusyWorker Error Handling Demo"
        root = {
          new BorderPane {
            padding = Insets(7)
            top = new ToolBar()
            center = buttonPane
            bottom = new HBox {
              spacing = 3
              children ++= Seq(progressLabel, progressBar)
            }
          }
        }
      }
    }
  }
}
