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

package org.scalafx.extras.pong

import scalafx.Includes._
import scalafx.animation.{AnimationTimer, KeyFrame, Timeline}
import scalafx.beans.property.BooleanProperty
import scalafx.event.ActionEvent
import scalafx.scene.Group
import scalafx.scene.control.Button
import scalafx.scene.input.KeyCode
import scalafx.scene.shape.Rectangle

import scala.language.postfixOps

class Pong {

  private val ball = new Ball()

  private val leftPaddle = new Paddle(20)
  private val rightPaddle = new Paddle(470)

  private val topWall = Rectangle(0, 0, 500, 1)
  private val rightWall = Rectangle(500, 0, 1, 500)
  private val leftWall = Rectangle(0, 0, 1, 500)
  private val bottomWall = Rectangle(0, 500, 500, 1)

  private val startButtonVisible = BooleanProperty(true)

  private val keyFrame = KeyFrame(10 ms, onFinished = {
    event: ActionEvent =>
      checkForCollision()
      val horzPixels = if (ball.movingRight) 1 else -1
      val vertPixels = if (ball.movingDown) 1 else -1
      ball.xPos() = ball.xPos.value + horzPixels
      ball.yPos() = ball.yPos.value + vertPixels
  })
  private val pongAnimation = new Timeline {
    keyFrames = Seq(keyFrame)
    cycleCount = Timeline.Indefinite
  }

  private val startButton = new Button {
    layoutX() = 225
    layoutY() = 470
    text = "Start!"
    visible <== startButtonVisible
    onAction = {
      event: ActionEvent =>
        startButtonVisible() = false
        pongAnimation.playFromStart()
        pongComponents.requestFocus()
    }
  }

  lazy val pongComponents: Group = new Group {
    focusTraversable = true
    children = List(
      ball.circle,
      topWall,
      leftWall,
      rightWall,
      bottomWall,
      leftPaddle.rect,
      rightPaddle.rect,
      startButton
    )

    onKeyPressed = k => k.code match {
      case KeyCode.L => rightPaddle.moveUp = true
      case KeyCode.Comma => rightPaddle.moveDown = true
      case KeyCode.A => leftPaddle.moveUp = true
      case KeyCode.Z => leftPaddle.moveDown = true
      case _ =>
    }
    onKeyReleased = k => k.code match {
      case KeyCode.L => rightPaddle.moveUp = false
      case KeyCode.Comma => rightPaddle.moveDown = false
      case KeyCode.A => leftPaddle.moveUp = false
      case KeyCode.Z => leftPaddle.moveDown = false
      case _ =>
    }

  }

  def initialize() {
    ball.xPos() = 250
    ball.yPos() = 250
    leftPaddle.positionY() = 235.0
    rightPaddle.positionY() = 235
    startButtonVisible() = true
    pongComponents.requestFocus()
  }

  private def checkForCollision() {
    if (ball.circle.intersects(rightWall.boundsInLocal()) || ball.circle.intersects(leftWall.boundsInLocal())) {
      pongAnimation.stop()
      initialize()
    } else if (ball.circle.intersects(bottomWall.boundsInLocal()) ||
      ball.circle.intersects(topWall.boundsInLocal())) {
      ball.movingDown = !ball.movingDown
    } else if (ball.circle.intersects(leftPaddle.rect.boundsInParent()) && !ball.movingRight) {
      ball.movingRight = !ball.movingRight
    } else if (ball.circle.intersects(rightPaddle.rect.boundsInParent()) && ball.movingRight) {
      ball.movingRight = !ball.movingRight
    }
  }

  private def movePaddleBy(paddle: Paddle, dy: Double): Unit = {
    if (dy < 0 && !paddle.intersects(topWall.boundsInLocal()) ||
      dy > 0 && !paddle.intersects(bottomWall.boundsInLocal())) {
      paddle.positionY() += dy
    }
  }

  private val timer = AnimationTimer { t =>
    val paddleSpeed = 3

    def updatePaddle(paddle: Paddle): Unit = {
      var dy = 0
      if (paddle.moveUp) dy -= paddleSpeed
      if (paddle.moveDown) dy += paddleSpeed
      movePaddleBy(paddle, dy)
    }

    updatePaddle(leftPaddle)
    updatePaddle(rightPaddle)
  }
  timer.start()

  initialize()
}
