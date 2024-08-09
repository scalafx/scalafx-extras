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

package org.scalafx.extras.image

import javafx.beans.binding as jfxbb
import org.scalafx.extras.ShowMessage
import scalafx.Includes.*
import scalafx.application.JFXApp3
import scalafx.application.JFXApp3.PrimaryStage
import scalafx.collections.ObservableBuffer
import scalafx.scene.Scene
import scalafx.scene.control.*
import scalafx.scene.image.Image
import scalafx.scene.layout.{BorderPane, FlowPane}
import scalafx.stage.FileChooser

/**
 * Demonstrates use of `ImageDisplay` class.
 */
object ImageDisplayDemoApp extends JFXApp3 {

  private val rotationItems = ObservableBuffer(0, 90, 180, 270)

  override def start(): Unit = {

    val imageDisplay = new ImageDisplay()
    //  private var roi: Option[Rectangle] = None

    stage = new PrimaryStage {
      scene = new Scene(640, 480) {
        icons += new Image("/org/scalafx/extras/sfx.png")
        title = "ImageDisplay Demo"
        root = new BorderPane {
          top = new ToolBar {
            items = Seq(
              new Button("Open...") {
                onAction = () => onFileOpen()
              },
              new Button("Zoom In") {
                onAction = () => imageDisplay.zoomIn()
                disable <== imageDisplay.zoomToFit
              },
              new Button("Zoom Out") {
                onAction = () => imageDisplay.zoomOut()
                disable <== imageDisplay.zoomToFit
              },
              new ToggleButton("Zoom to fit") {
                selected <==> imageDisplay.zoomToFit
              },
              new ToggleButton("Flip X") {
                selected <==> imageDisplay.flipX
              },
              new ToggleButton("Flip Y") {
                selected <==> imageDisplay.flipY
              },
              new ChoiceBox(rotationItems) {
                selectionModel().selectedItem.onChange { (_, _, newValue) =>
                  imageDisplay.rotation = newValue
                }
                selectionModel().selectFirst()
              }
            )
          }
          center = imageDisplay.view
          bottom = new FlowPane {
            children = Seq {
              new Label("???") {
                text <==
                  when(!imageDisplay.zoomToFit) choose {
                    jfxbb.Bindings.format("Zoom %.2f%%", (imageDisplay.actualZoom * 100).delegate)
                  } otherwise {
                    jfxbb.Bindings.format("Zoom to fit (%.2f%%)", (imageDisplay.actualZoom * 100).delegate)
                  }
              }
            }
          }
        }
      }
    }

    //  setROI()
    // ---------------------------------------------------------------------------

    /**
     * Let user select an image file and load it.
     */
    def onFileOpen(): Unit = {
      val fileChooser = new FileChooser()
      val file        = fileChooser.showOpenDialog(stage)
      if (file != null) {
        try {
          val image = new Image("file:" + file.getCanonicalPath)
          if (!image.error()) {
            imageDisplay.image() = image
          } else {
            image.exception().printStackTrace()
            ShowMessage.exception(
              title = "Open image...",
              message = "Failed to load image from file:\n" + file.getCanonicalPath,
              t = image.exception(),
              parentWindow = stage
            )
          }
        } catch {
          case ex: IllegalArgumentException =>
            ex.printStackTrace()
            ShowMessage.exception(
              title = "Open image...",
              message = "Failed to load image from file:\n" + file.getCanonicalPath,
              t = ex,
              parentWindow = stage
            )

        }
      }
    }

    //  private def setROI(): Unit = {
    //    roi = roi match {
    //      case Some(r) => None
    //      case None => Some(Rectangle(278, 205, 37, 10))
    //    }
    //
    //    imageDisplay.roi() = roi
    //  }
  }
}
