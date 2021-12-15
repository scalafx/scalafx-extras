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

package org.scalafx.extras.showmessage

import com.typesafe.scalalogging.Logger
import org.scalafx.extras.*
import org.scalafx.extras.mvcfx.ModelFX

import scala.util.control.NonFatal

/**
 * ShowMessage behaviour ModelFX.
 */
class ShowMessageDemoModel extends ModelFX with ShowMessage {

  private val Title = "ShowMessage Demo"

  // Connect your custom logger to ShowMessageLogger (this is optional).
  private val _logger = Logger[ShowMessageDemoModel]
  override def messageLogger: Option[ShowMessageLogger] = Some(
    new ShowMessageLogger {
      override def warn(message: String): Unit                = _logger.warn(message)
      override def error(message: String): Unit               = _logger.error(message)
      override def error(message: String, t: Throwable): Unit = _logger.error(message, t)
    }
  )

  def onShowInformation(): Unit = {
    showInformation(Title, "This is a information \"header\"", "This is the information detailed \"content\".")
  }

  def onShowConfirmation(): Unit = {
    val ok: Boolean =
      showConfirmation(Title, "This is the confirmation \"header\"", "This is the confirmation detailed \"content\".")

    showInformation(Title, "Confirmation (Yes/No) result", "Dialog returned: " + ok)
  }

  def onShowConfirmationYesNoCancel(): Unit = {
    val ok: Option[Boolean] = showConfirmationYesNoCancel(
      Title,
      "This is the Yes/No/Cancel confirmation \"header\"",
      "This is the confirmation detailed \"content\"."
    )

    showInformation(Title, "Confirmation (Yes/No/Cancel) result", "Dialog returned: " + ok)
  }

  def onShowWarning(): Unit = {
    showWarning(Title, "This is the warning \"header\"", "This is the warning detailed \"content\".")
  }

  def onShowError(): Unit = {
    showError(Title, "This is the error \"header\"", "This is the error detailed \"content\".")
  }

  def onShowException(): Unit = {
    try {
      throw new Exception("A demo exception 1.")
    } catch {
      case NonFatal(e) => showException(Title, "A sample exception was thrown.", e)
    }
  }
}
