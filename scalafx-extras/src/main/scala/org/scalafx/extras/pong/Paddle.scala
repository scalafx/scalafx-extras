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

package org.scalafx.extras.pong

import scalafx.Includes.*
import scalafx.beans.property.DoubleProperty
import scalafx.geometry.Bounds
import scalafx.scene.Cursor
import scalafx.scene.paint.Color
import scalafx.scene.shape.Rectangle

private[pong] class Paddle(val xPos: Int) {
  val positionY = new DoubleProperty()

  var initPaddleTranslateY: Double = _

  var dragAnchorY: Double = _

  var moveUp: Boolean   = false
  var moveDown: Boolean = false

  val rect: Rectangle = new Rectangle {
    x = xPos
    width = 10
    height = 30
    fill = Color.LightBlue
    cursor = Cursor.Hand
    translateY <== positionY
    onMousePressed = me => {
      initPaddleTranslateY = rect.translateY()
      dragAnchorY = me.sceneY
    }

    onMouseDragged = me => {
      val dragY = me.sceneY - dragAnchorY
      positionY() = initPaddleTranslateY + dragY
    }
  }

  def intersects(b: Bounds): Boolean = {
    rect.boundsInParent().intersects(b)
  }
}
