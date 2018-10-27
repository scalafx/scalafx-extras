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

package org.scalafx.extras.image

import enumeratum.{Enum, EnumEntry}

import scala.collection.immutable
import scala.math._

object ZoomScale extends Enum[ZoomScale] {

  case object Zoom1Perc extends ZoomScale(0.01, "1%")

  case object Zoom1_5Perc extends ZoomScale(0.015, "1.5%")

  case object Zoom2Perc extends ZoomScale(0.02, "2%")

  case object Zoom3Perc extends ZoomScale(0.03, "3%")

  case object Zoom4Perc extends ZoomScale(0.04, "4%")

  case object Zoom5Perc extends ZoomScale(0.05, "5%")

  case object Zoom6_25Perc extends ZoomScale(0.0625, "6.25%")

  case object Zoom8_3Perc extends ZoomScale(0.083, "8.3%")

  case object Zoom12_5Perc extends ZoomScale(0.125, "12.5%")

  case object Zoom16_7Perc extends ZoomScale(0.167, "16.7%")

  case object Zoom25Perc extends ZoomScale(0.25, "25%")

  case object Zoom33_3Perc extends ZoomScale(0.333, "33.3%")

  case object Zoom50Perc extends ZoomScale(0.5, "50%")

  case object Zoom66_7Perc extends ZoomScale(0.667, "66.7%")

  case object Zoom100Perc extends ZoomScale(1, "100%")

  case object Zoom200Perc extends ZoomScale(2, "200%")

  case object Zoom300Perc extends ZoomScale(3, "300%")

  case object Zoom400Perc extends ZoomScale(4, "400%")

  case object Zoom500Perc extends ZoomScale(5, "500%")

  case object Zoom600Perc extends ZoomScale(6, "600%")

  case object Zoom700Perc extends ZoomScale(7, "700%")

  case object Zoom800Perc extends ZoomScale(8, "800%")

  case object Zoom1200Perc extends ZoomScale(12, "1200%")

  case object Zoom1600Perc extends ZoomScale(16, "1600%")

  case object Zoom3200Perc extends ZoomScale(32, "3200%")

  val values: immutable.IndexedSeq[ZoomScale] = findValues

  def zoomOut(zoomScale: ZoomScale): ZoomScale = {
    val i: Int = values.indexOf(zoomScale)
    if (i < 0) {
      throw new IllegalArgumentException("Internal error: scale: " + zoomScale + " is not one of indexed values.")
    } else if (i == 0) {
      zoomScale
    } else if (i < values.length) {
      values(i - 1)
    } else {
      throw new IllegalArgumentException("Internal error looking for next scale.")
    }
  }

  def zoomIn(zoomScale: ZoomScale): ZoomScale = {
    val i: Int = values.indexOf(zoomScale)
    if (i < 0) {
      throw new IllegalArgumentException("Internal error: scale: " + zoomScale + " is not one of indexed values.")
    } else if (i == (values.length - 1)) {
      zoomScale
    } else if (i < (values.length - 1)) {
      values(i + 1)
    } else {
      throw new IllegalArgumentException("Internal error looking for next scale.")
    }
  }


  private def closetsScaleTo(scaleCandidate: Double): Double = {
    val values = ZoomScale.values
    var minDistance = Double.PositiveInfinity
    var bestScale = 1d
    for (v <- ZoomScale.values) {
      val d = abs(scaleCandidate - v.scale)
      if (d < minDistance) {
        minDistance = d
        bestScale = v.scale
      }
    }
    bestScale
  }

}


/**
  * @author Jarek Sacha
  */
sealed abstract class ZoomScale(val scale: Double, val name: String) extends EnumEntry {

  override def toString: String = name
}
