/*
 * Copyright (c) 2011-2024, ScalaFX Project
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

package org.scalafx.extras.progress_dialog

import javafx.concurrent as jfxc
import org.scalafx.extras
import org.scalafx.extras.*
import org.scalafx.extras.progress_dialog.impl.ProgressStatus
import scalafx.Includes.*
import scalafx.beans.property.*
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.Button
import scalafx.scene.layout.BorderPane
import scalafx.stage.{Stage, Window}

import java.time.Duration

class ProgressStatusDialog(dialogTitle: String, parentWindow: Option[Window]):

  private val elapsedTimeService = new ElapsedTimeService()
  private val progressStatus     = new ProgressStatus()

  progressStatus.model.statusText.value =
    "------------------------------------------------------------------------------"

  val abortFlag: BooleanProperty = BooleanProperty(false)

  private def updateETA(): Unit =
    val strVal =
      val progress = progressStatus.model.progress.value
      if progress <= 0 then
        "?"
      else
        // TODO: prevent jitter of estimate when progress value changes,
        //       compute running average of last predictions or something...
        val et        = elapsedTimeService.elapsedTime.value
        val eta: Long = (et * (1 - progress) / progress).ceil.toLong
        formatDuration(Duration.ofMillis(eta))
    progressStatus.model.etaTimeText.value = strVal

  elapsedTimeService.elapsedTime.onChange { (_, _, newValue) =>
    progressStatus.model.elapsedTimeText.value = formatDuration(Duration.ofMillis(newValue.longValue()))
    updateETA()
  }

  private val abortButton = new Button:
    text = "Abort batch processing"
    padding = Insets(7)
    margin = Insets(7)
    onAction = _ =>
      extras.offFXAndWait {
        // TODO show abort status
        onFX {
          progressStatus.model.statusText.value = "Aborting processing"
        }
        onFX {
          abortFlag.value = true
        }
      }
    disable <== abortFlag
    alignmentInParent = Pos.Center

  private val dialog: Stage = new Stage:
    initOwner(parentWindow.orNull)
    parentWindow.foreach { w =>
      w.delegate match
        case s: javafx.stage.Stage =>
          icons ++= s.icons
        case x =>
          throw new Exception(s"Invalid parent window delegate: $x")
    }
    title = dialogTitle
    resizable = false
    scene = new Scene:
      root = new BorderPane:
        padding = Insets(14)
        center = progressStatus.view
        bottom = abortButton
      parentWindow.foreach(w => stylesheets = w.scene().stylesheets)

    onShown = _ =>
      // TODO: prevent double initialization
      elapsedTimeService.doStart()
    onCloseRequest = e =>
      abortFlag.value = true
      // Do not allow closing the window
      e.consume()

  private def formatDuration(duration: Duration): String =
    val seconds    = duration.getSeconds
    val absSeconds = Math.abs(seconds)
    val positive   = f"${absSeconds / 3600}%d:${absSeconds % 3600 / 60}%02d:${absSeconds % 60}%02d"
    if seconds < 0 then
      "-" + positive
    else
      positive

  private class ElapsedTimeService extends jfxc.ScheduledService[Long]:

    private var startTime: Long = _

    private val _elapsedTime = new ReadOnlyLongWrapper()

    /** Elapsed time in milliseconds */
    val elapsedTime: ReadOnlyLongProperty = _elapsedTime.readOnlyProperty

    this.period = 250.ms

    override def createTask(): jfxc.Task[Long] = () =>
      val ct = System.currentTimeMillis()
      val et = ct - startTime
      onFX { _elapsedTime.value = et }
      et

    def doStart(): Unit =
      this.restart()
      startTime = System.currentTimeMillis()
      onFX {
        _elapsedTime.value = 0
      }

  def window: Window = dialog

  def progress: DoubleProperty = progressStatus.model.progress

  def statusText: StringProperty = progressStatus.model.statusText

  def totalCount: StringProperty = progressStatus.model.totalCountText

  def processedCount: StringProperty = progressStatus.model.processedCountText

  def successfulCount: StringProperty = progressStatus.model.successfulCountText

  def failedCount: StringProperty = progressStatus.model.failedCountText

  def cancelledCount: StringProperty = progressStatus.model.cancelledCountText

  def close(): Unit =
    elapsedTimeService.cancel()
    dialog.close()

  def show(): Unit =
    dialog.show()
end ProgressStatusDialog
