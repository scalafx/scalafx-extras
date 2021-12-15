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

import scala.math.*


enum ZoomScale(val scale: Double, val name: String):

  case Zoom1Perc extends ZoomScale(0.01, "1%")
  case Zoom1_5Perc extends ZoomScale(0.015, "1.5%")
  case Zoom2Perc extends ZoomScale(0.02, "2%")
  case Zoom3Perc extends ZoomScale(0.03, "3%")
  case Zoom4Perc extends ZoomScale(0.04, "4%")
  case Zoom5Perc extends ZoomScale(0.05, "5%")
  case Zoom6_25Perc extends ZoomScale(0.0625, "6.25%")
  case Zoom8_3Perc extends ZoomScale(0.083, "8.3%")
  case Zoom12_5Perc extends ZoomScale(0.125, "12.5%")
  case Zoom16_7Perc extends ZoomScale(0.167, "16.7%")
  case Zoom25Perc extends ZoomScale(0.25, "25%")
  case Zoom33_3Perc extends ZoomScale(0.333, "33.3%")
  case Zoom50Perc extends ZoomScale(0.5, "50%")
  case Zoom66_7Perc extends ZoomScale(0.667, "66.7%")
  case Zoom100Perc extends ZoomScale(1, "100%")
  case Zoom200Perc extends ZoomScale(2, "200%")
  case Zoom300Perc extends ZoomScale(3, "300%")
  case Zoom400Perc extends ZoomScale(4, "400%")
  case Zoom500Perc extends ZoomScale(5, "500%")
  case Zoom600Perc extends ZoomScale(6, "600%")
  case Zoom700Perc extends ZoomScale(7, "700%")
  case Zoom800Perc extends ZoomScale(8, "800%")
  case Zoom1200Perc extends ZoomScale(12, "1200%")
  case Zoom1600Perc extends ZoomScale(16, "1600%")
  case Zoom3200Perc extends ZoomScale(32, "3200%")


  override def toString: String = name


object ZoomScale:

  def zoomOut(zoomScale: ZoomScale): ZoomScale =
    val i: Int = ZoomScale.values.indexOf(zoomScale)
    if i < 0 then
      throw new IllegalArgumentException(
        "Internal error: scale: " + zoomScale + " is not one of indexed values."
      )
    else if i == 0 then
      zoomScale
    else if i < ZoomScale.values.length then
      ZoomScale.values(i - 1)
    else
      throw new IllegalArgumentException("Internal error looking for next scale.")


  def zoomIn(zoomScale: ZoomScale): ZoomScale =
    val i: Int = ZoomScale.values.indexOf(zoomScale)
    if i < 0 then
      throw new IllegalArgumentException(
        "Internal error: scale: " + zoomScale + " is not one of indexed values."
      )
    else if i == (ZoomScale.values.length - 1) then
      zoomScale
    else if i < (ZoomScale.values.length - 1) then
      ZoomScale.values(i + 1)
    else
      throw new IllegalArgumentException("Internal error looking for next scale.")


  private def closetsScaleTo(scaleCandidate: Double): Double =
    val values      = ZoomScale.values
    var minDistance = Double.PositiveInfinity
    var bestScale   = 1d
    for v <- ZoomScale.values do
      val d = abs(scaleCandidate - v.scale)
      if d < minDistance then
        minDistance = d
        bestScale = v.scale

    bestScale
