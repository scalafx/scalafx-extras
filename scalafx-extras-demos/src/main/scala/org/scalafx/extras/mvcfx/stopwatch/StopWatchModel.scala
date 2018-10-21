/*
 * Copyright (c) 2011-2016, ScalaFX Project
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

import javafx.{concurrent => jfxc}
import org.scalafx.extras._
import org.scalafx.extras.mvcfx.ModelFX
import scalafx.Includes._
import scalafx.beans.property.{LongProperty, ReadOnlyBooleanProperty, ReadOnlyBooleanWrapper}


/**
  * StopWatch behaviour ModelFX.
  */
class StopWatchModel extends ModelFX {


  private val _running = ReadOnlyBooleanWrapper(false)

  val running: ReadOnlyBooleanProperty = _running.readOnlyProperty

  private val counterService = new CounterService()
  counterService.period = 10.ms

  val minutes = new LongProperty()
  val seconds = new LongProperty()
  val secondFraction = new LongProperty()

  counterService.elapsedTime.onChange { (_, _, newValue) =>
    val t = newValue.longValue()
    secondFraction.value = t % 1000
    seconds.value = (t / 1000) % 60
    minutes.value = t / 1000 / 60
  }


  def onStart(): Unit = {
    counterService.doResume()
    _running.value = true
  }

  def onStop(): Unit = {
    counterService.doPause()
    _running.value = false
  }

  def onReset(): Unit = {
    counterService.doReset()
  }


  private class CounterService extends jfxc.ScheduledService[Long] {

    private var timeAccumulator: Long = 0
    private var restartTime: Long = 0

    val elapsedTime = new LongProperty()

    override def createTask(): jfxc.Task[Long] = {
      new jfxc.Task[Long]() {

        override protected def call(): Long = {
          val ct = System.currentTimeMillis()
          val et = timeAccumulator + (ct - restartTime)
          onFX {
            elapsedTime.value = et
          }
          et
        }
      }
    }

    def doPause(): Unit = {
      val ct = System.currentTimeMillis()
      timeAccumulator += (ct - restartTime)
      onFX {
        elapsedTime.value = timeAccumulator
      }
      this.cancel()
    }

    def doResume(): Unit = {
      restartTime = System.currentTimeMillis()
      this.restart()
    }

    def doReset(): Unit = {
      timeAccumulator = 0
      onFX {
        elapsedTime.value = 0
      }
    }
  }

}
