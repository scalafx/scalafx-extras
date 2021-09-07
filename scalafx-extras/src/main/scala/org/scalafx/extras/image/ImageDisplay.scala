/*
 * Copyright (c) 2011-2021, ScalaFX Project
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

import scalafx.Includes._
import scalafx.beans.property._
import scalafx.scene.Node
import scalafx.scene.control.ScrollPane
import scalafx.scene.image.ImageView
import scalafx.scene.layout.{Pane, StackPane}
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

/**
  * Displays an image view with ability to zoom in, zoom out, zoom to fit. It can also automatically resizes to parent size.
  * When `zoomToFit` is set to `true` the image is sized to fit the parent scroll pane.
  *
  * Sample usage (full detains in `ImageDisplayDemoApp`)
  * {{{
  * object ImageDisplayDemoApp extends JFXApp3 {
  *
  *   override def start(): Unit = {
  *
  *     private val imageDisplay = new ImageDisplay()
  *
  *     stage = new PrimaryStage {
  *       scene = new Scene(640, 480) {
  *         title = "ImageDisplay Demo"
  *         root = new BorderPane {
  *           top = new ToolBar {
  *             items = Seq(
  *               new Button("Open...") {
  *                 onAction = () => onFileOpen()
  *               },
  *               new Button("Zoom In") {
  *                 onAction = () => imageDisplay.zoomIn()
  *                 disable <== imageDisplay.zoomToFit
  *               },
  *               new Button("Zoom Out") {
  *                 onAction = () => imageDisplay.zoomOut()
  *                 disable <== imageDisplay.zoomToFit
  *               },
  *               new ToggleButton("Zoom to fit") {
  *                 selected <==> imageDisplay.zoomToFit
  *               }
  *             )
  *           }
  *           center = imageDisplay.view
  *         }
  *       }
  *     }
  *   }
  * }
  * }}}
  */
class ImageDisplay() {

  private val imageView = new ImageView {
    preserveRatio = true
    smooth = true
    cache = true
  }

  private val overlayPane = new Pane()

  private val roiView = new Rectangle() {
    fill = Color(1, 1, 1, 0)
    stroke = Color.Yellow
  }

  private val scrollPane: ScrollPane = new ScrollPane {
    self =>
    // setting `fitTo* = true` makes the image centered when view point is larger than the zoomed image.
    fitToHeight = true
    fitToWidth = true
    // Wrap content in a group, as advised in ScrollPane documentation,
    // to get proper size for fitting with using `zoomToFit`.
    // This may not be needed, as wrapping in a group makes it difficult to center.
    //    content = new Group {
    //      children = new StackPane {
    //        children = Seq(imageView, overlayPane)
    //      }
    //    }
    content = new StackPane {
      children = Seq(imageView, overlayPane)
    }
  }

  /**
    * Controls image zoom when `zoomToFit` is off. Value of 1 mean no scaling.
    * Values larger than 1 make image larger. Values smaller than 1 make image smaller.
    */
  val zoom: ObjectProperty[ZoomScale] = ObjectProperty[ZoomScale](this, "Zoom", ZoomScale.Zoom100Perc)

  /**
    * When set to `true` the image fits to the size of the available view, maintaining its aspect ratio.
    */
  val zoomToFit: BooleanProperty = BooleanProperty(value = false)

  private val _actualZoom = ReadOnlyDoubleWrapper(1d)

  /**
    * Actual zoom value.
    * It should be the same as `zoom` when `zoomToFit==false`, it may be different if `zoomToFit==true`
    */
  val actualZoom: ReadOnlyDoubleProperty = _actualZoom.readOnlyProperty

  /**
    * Optional rectangular ROI to be displayed on the image
    */
  val roi: ObjectProperty[Option[Rectangle]] = ObjectProperty[Option[Rectangle]](None)

  /**
    * ScalaFX node in containing this image display UI.
    */
  val view: Node = scrollPane

  /**
    * Property containing image to be displayed. If `null` the display will be blank (following JavaFX convention)
    */
  val image: ObjectProperty[javafx.scene.image.Image] = imageView.image

  initialize()

  /**
    * Image rotation in degrees. Default value is 0 (no rotation).
    */
  def rotation: Double = imageView.rotate()

  def rotation_=(r: Double): Unit = {
    imageView.rotate() = r
  }

  /**
    * Zoom in the view.
    */
  def zoomIn(): Unit = {
    zoom() = ZoomScale.zoomIn(zoom())
  }

  /**
    * Zoom out the view.
    */
  def zoomOut(): Unit = {
    zoom() = ZoomScale.zoomOut(zoom())
  }

  private def initialize(): Unit = {

    roi.onChange { (_, oldROI, newROI) =>
      oldROI match {
        case Some(_) =>
          // Unbind from roiView
          roiView.x.unbind()
          roiView.y.unbind()
          roiView.width.unbind()
          roiView.height.unbind()
        case None =>
      }

      newROI match {
        case Some(r) =>
          // Bind to roiView
          roiView.x <== r.x * _actualZoom + imageView.layoutX
          roiView.y <== r.y * _actualZoom + imageView.layoutY
          roiView.width <== r.width * _actualZoom
          roiView.height <== r.height * _actualZoom

          overlayPane.children = roiView

        case None =>
          overlayPane.children.clear()
      }
    }

    updateFit()

    // Update fit when zoom or control size changes
    Seq(zoom, zoomToFit, scrollPane.width, scrollPane.height, imageView.image).foreach(_.onInvalidate {
      updateFit()
    })
  }

  private def updateFit(): Unit = {
    Option(imageView.image()).foreach { image =>
      val (w, h) =
        if (zoomToFit()) {
          val bounds = scrollPane.viewportBounds()
          (bounds.width, bounds.height)
        } else {
          (zoom().scale * image.width(), zoom().scale * image.height())
        }

      // Correct for rotation
      val r = new Rectangle {
        width = w
        height = h
        rotate = rotation
      }
      val b = r.boundsInParent()

      imageView.fitWidth = b.width
      imageView.fitHeight = b.height

      _actualZoom() = scala.math.min(w / image.width(), h / image.height())
    }
  }
}
