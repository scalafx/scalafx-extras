package org.scalafx.extras.pong

import scalafx.Includes._
import scalafx.beans.property.DoubleProperty
import scalafx.geometry.Bounds
import scalafx.scene.Cursor
import scalafx.scene.input.MouseEvent
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

private[pong] class Paddle(val xPos: Int) {
  val positionY = new DoubleProperty()

  var initPaddleTranslateY: Double = _

  var dragAnchorY: Double = _

  var moveUp: Boolean = false
  var moveDown: Boolean = false

  val rect: Rectangle = new Rectangle {
    x = xPos
    width = 10
    height = 30
    fill = Color.LightBlue
    cursor = Cursor.Hand
    translateY <== positionY
    onMousePressed = (me: MouseEvent) => {
      initPaddleTranslateY = rect.translateY()
      dragAnchorY = me.sceneY
    }

    onMouseDragged = (me: MouseEvent) => {
      val dragY = me.sceneY - dragAnchorY
      positionY() = initPaddleTranslateY + dragY
    }
  }

  def intersects(b: Bounds): Boolean = {
    rect.boundsInParent().intersects(b)
  }
}

