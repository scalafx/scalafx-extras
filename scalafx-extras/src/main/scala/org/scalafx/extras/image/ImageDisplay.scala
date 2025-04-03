/*
 * Copyright (c) 2011-2025, ScalaFX Project
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

import javafx.scene.image as jfxsi
import scalafx.Includes.*
import scalafx.beans.property.*
import scalafx.geometry.Pos
import scalafx.scene.canvas.Canvas
import scalafx.scene.control.ScrollPane
import scalafx.scene.layout.StackPane
import scalafx.scene.shape.Rectangle
import scalafx.scene.{Group, Node}

import scala.collection.mutable

/**
 * Displays an image view with the ability to zoom in, zoom out, zoom to fit.
 * It can be also automatically resized to the parent's size.
 * When `zoomToFit` is set to `true`, the image is sized to fit the parent scroll pane.
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
class ImageDisplay {

  /** Later for displaying the image */
  private val imageCanvas = new Canvas {
    alignmentInParent = Pos.Center
  }

  /** Layer for displaying the overlays */
  private val overlayCanvas = new Canvas {
    alignmentInParent = Pos.Center
    width <== imageCanvas.width
    height <== imageCanvas.height
  }

  private val canvasGroup = new Group {
    children = Seq(imageCanvas, overlayCanvas)
    alignmentInParent = Pos.Center
  }

  /** The node that will be zoomed, rotated, and flipped */
  private val transformTarget = canvasGroup

  private val scrollPane: ScrollPane = new ScrollPane {
    // setting `fitTo* = true` makes the image centered when the view point is larger than the zoomed image.
    fitToHeight = true
    fitToWidth = true
    pannable = true
    // Group inside StackPane to make image centered in the scroll pane
    content = new StackPane {
      alignment = Pos.Center
      alignmentInParent = Pos.Center
      children = new Group {
        children = canvasGroup
        alignmentInParent = Pos.Center
      }
    }
  }

  /**
   * Controls image zoom when `zoomToFit` is off.
   * The value of 1 means no scaling.
   * Values larger than 1 make image larger.
   * Values smaller than 1 make image smaller.
   */
  // noinspection ScalaWeakerAccess
  val zoom: ObjectProperty[ZoomScale] = ObjectProperty[ZoomScale](this, "Zoom", ZoomScale.Zoom100Perc)

  /** When set to `true`, the image fits to the size of the available view, maintaining its aspect ratio. */
  val zoomToFit: BooleanProperty = BooleanProperty(value = false)

  private val _actualZoom = ReadOnlyDoubleWrapper(1d)

  /**
   * Actual zoom value.
   * It should be the same as `zoom` when `zoomToFit==false`, it may be different if `zoomToFit==true`
   */
  val actualZoom: ReadOnlyDoubleProperty = _actualZoom.readOnlyProperty

  /** ScalaFX node in containing this image display UI. */
  val view: Node = scrollPane

  /** Flip image on X axis, this is done before applying rotation */
  val flipX: BooleanProperty = BooleanProperty(value = false)

  /** Flip image on Y axis, this is done before applying rotation */
  val flipY: BooleanProperty = BooleanProperty(value = false)

  /**
   * Property containing image to be displayed.
   * If `null`, the display will be blank (following JavaFX convention)
   */
  val image: ObjectProperty[Option[javafx.scene.image.Image]] = new ObjectProperty[Option[jfxsi.Image]] {
    value = None
    onChange { (_, _, newImageOpt) =>
      newImageOpt match {
        case Some(image) =>
          imageCanvas.width = image.width.value
          imageCanvas.height = image.height.value
          val gc = imageCanvas.graphicsContext2D
          gc.drawImage(image, 0, 0)

          drawOverlays()
        case None =>
          val gc = imageCanvas.graphicsContext2D
          gc.clearRect(0, 0, imageCanvas.width(), imageCanvas.height())
          imageCanvas.width = 0
          imageCanvas.height = 0
      }
    }
  }

  def setImage(newImage: scalafx.scene.image.Image): Unit = {
    image.value = Option(newImage)
  }

  private val _overlays: mutable.ListBuffer[Rectangle] = mutable.ListBuffer.empty[Rectangle]

  /**
   * Set overlays to display on the image. A rectangle represents individual overlay.
   * Rectangle properties used to draw the overlay:
   *   - stroke - color/paint of the outline. If `null`, no outline will be drawn.
   *   - strokeWidth - width of the line in screen pixels. The width is maintained constant to the display, regardless of zoom.
   *   - fill - fill color/paint. If `null`, no outline will be drawn.
   *
   * Note that the opacity of is controlled by opacity of the `stroke` and `fill` paint.
   *
   * @param ovls overlays to display on the image.
   */
  def overlays_=(ovls: Seq[Rectangle]): Unit = {
    _overlays.clear()
    _overlays.appendAll(ovls)
    drawOverlays()
  }

  /**
   * Overlays displayed on the image.
   * @see #overlays_=()
   */
  def overlays: Seq[Rectangle] = _overlays.toSeq

  initialize()

  /**
   * Image rotation in degrees.
   * The default value is 0 (no rotation).
   * This is done after applying flip operations.
   */
  def rotation: Double = transformTarget.rotate()

  def rotation_=(r: Double): Unit = {
    transformTarget.rotate() = r
  }

  /** Zoom in the view. */
  def zoomIn(): Unit = {
    zoom() = ZoomScale.zoomIn(zoom())
  }

  /** Zoom out the view. */
  def zoomOut(): Unit = {
    zoom() = ZoomScale.zoomOut(zoom())
  }

  private def initialize(): Unit = {

    flipX.onChange { (_, _, newValue) =>
      val v = math.abs(transformTarget.scaleX.value)
      transformTarget.scaleX.value = if (newValue) -v else v
    }

    flipY.onChange { (_, _, newValue) =>
      val v = math.abs(transformTarget.scaleY.value)
      transformTarget.scaleY.value = if (newValue) -v else v
    }

    updateFit()

    // Update fit when zoom or control size changes
    Seq(
      zoom,
      zoomToFit,
      scrollPane.viewportBounds,
      image,
      actualZoom,
      transformTarget.rotate
    ).foreach(_.onInvalidate {
      updateFit()
    })
  }

  private def updateFit(): Unit = {
    image().foreach { im =>
      val scale =
        if (zoomToFit()) {
          val viewportBounds = scrollPane.viewportBounds()

          // Calculate bounds for roted image at scale=1
          val rotatedImageBounds =
            new Rectangle {
              width = im.width()
              height = im.height()
              rotate = rotation
            }.boundsInParent()

          // Compute the zoom-to-fit scale
          scala.math.min(
            viewportBounds.width / rotatedImageBounds.width,
            viewportBounds.height / rotatedImageBounds.height
          )
        } else {
          zoom().scale
        }

      transformTarget.scaleX = if (flipX.value) -scale else scale
      transformTarget.scaleY = if (flipY.value) -scale else scale
      _actualZoom() = scale
      drawOverlays()
    }
  }

  private def drawOverlays(): Unit = {
    val gc = overlayCanvas.graphicsContext2D
    gc.clearRect(0, 0, overlayCanvas.width(), overlayCanvas.height())

    overlays.foreach { r =>
      // Draw outline
      Option(r.stroke()).foreach { stroke =>
        // Maintain displayed width of the line regardless of zoom
        gc.lineWidth = r.strokeWidth() / actualZoom()
        gc.stroke = stroke
        gc.strokeRect(r.x.value, r.y.value, r.width.value, r.height.value)
      }
      // Fill-in
      Option(r.fill()).foreach { fill =>
        gc.fill = fill
        gc.fillRect(r.x.value, r.y.value, r.width.value, r.height.value)
      }
    }
  }
}
