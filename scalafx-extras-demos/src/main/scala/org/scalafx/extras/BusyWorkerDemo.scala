/*
 * Copyright (c) 2011-2018, ScalaFX Project
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

import java.util.concurrent.Future

import org.scalafx.extras.BusyWorker.SimpleTask
import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, Label, ProgressBar, ToolBar}
import scalafx.scene.image.Image
import scalafx.scene.layout.{BorderPane, HBox, Priority, VBox}

/**
  * An application illustrating use of `BusyWorker`, including progress and message updates.
  */
object BusyWorkerDemo extends JFXApp {

  private val progressLabel = new Label("") {
    hgrow = Priority.Always
    maxWidth = Double.MaxValue
  }
  private val progressBar = new ProgressBar() {
    progress = 0
  }

  private var busyWorker: BusyWorker = _

  //noinspection ConvertExpressionToSAM
  private val buttonPane = new VBox {
    spacing = 9
    alignment = Pos.Center
    padding = Insets(21)
    children ++= Seq(
      new Button("Task with explicit progress value") {
        onAction = () => busyWorker.doTask("Task 1")(
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
      new Button("Task with simple progress indicator") {
        onAction = () => busyWorker.doTask("Task 2") {
          println("Task 2")
          Thread.sleep(3000)
        }
        maxWidth = Double.MaxValue
      },
      new Button("Task failing with exception (on 7)") {
        onAction = () => busyWorker.doTask("Task 1")(
          new SimpleTask[String] {
            override def call(): String = {
              val maxItems = 10
              for (i <- 1 to maxItems) {
                println(i)
                message() = s"Processing item $i/$maxItems"
                progress() = (i - 1) / 10.0
                Thread.sleep(250)

                if (i == 7) {
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
      }
    ).map(_.delegate)
  }

  stage = new PrimaryStage {
    scene = new Scene {
      icons += new Image("/org/scalafx/extras/sfx.png")
      title = "BusyWorker Demo"
      root = {
        new BorderPane {
          padding = Insets(3)
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

  busyWorker = new BusyWorker("BusyWorker Demo", buttonPane) {
    progressLabel.text <== progressMessage
    progressBar.progress <== progressValue
  }


}
