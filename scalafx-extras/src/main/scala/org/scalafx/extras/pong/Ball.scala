package org.scalafx.extras.pong

import scalafx.beans.property.DoubleProperty
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle

private[pong] class Ball {
  val xPos = new DoubleProperty
  val yPos = new DoubleProperty

  var movingRight = true
  var movingDown = true

  val circle: Circle = new Circle {
    radius = 5.0
    fill = Color.White
    centerX <== xPos
    centerY <== yPos
  }
}
