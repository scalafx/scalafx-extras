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

package org.scalafx.extras.mvcfx.stopwatch


import org.scalafx.extras.mvcfx.ControllerFX
import scalafx.Includes._
import scalafx.scene.control.{Button, Label}
import scalafxml.core.macros.sfxml

/**
  * StopWatch UI view.
  * It is intended to create bindings between UI definition loaded fro FXML configuration and the UI model
  */
@sfxml
class StopWatchController(minutesLabel: Label,
                          secondsLabel: Label,
                          fractionLabel: Label,
                          startButton: Button,
                          stopButton: Button,
                          resetButton: Button,
                          model: StopWatchModel) extends ControllerFX {

  minutesLabel.text.value = format2d(model.minutes.longValue)
  model.minutes.onChange { (_, _, newValue) =>
    minutesLabel.text.value = format2d(newValue.longValue)
  }
  secondsLabel.text.value = format2d(model.seconds.longValue())
  model.seconds.onChange { (_, _, newValue) =>
    secondsLabel.text.value = format2d(newValue.longValue())
  }
  fractionLabel.text.value = format2d(model.secondFraction.longValue() / 10)
  model.secondFraction.onChange { (_, _, newValue) =>
    fractionLabel.text.value = format2d(newValue.longValue() / 10)
  }

  startButton.disable <== model.running
  stopButton.disable <== !model.running
  resetButton.disable <== model.running

  startButton.onAction = () => model.onStart()
  stopButton.onAction = () => model.onStop()
  resetButton.onAction = () => model.onReset()

  private def format2d(t: Number) = f"${t.longValue()}%02d"
}
