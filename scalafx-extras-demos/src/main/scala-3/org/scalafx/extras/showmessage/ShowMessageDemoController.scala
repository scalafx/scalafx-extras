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

import javafx.scene.{control as jfxsc, layout as jfxsl}
import javafx.{event as jfxe, fxml as jfxf}
import org.scalafx.extras.mvcfx.ControllerFX
import scalafx.Includes.*
import scalafx.scene.control.{Button, Label}

/**
 * ShowMessage UI view. It is intended to create bindings between UI definition
 * loaded from FXML configuration and the UI model
 */
class ShowMessageDemoController(val model: ShowMessageDemoModel) extends ControllerFX:

  @jfxf.FXML
  private var showConfirmationButton: jfxsc.Button = _
  @jfxf.FXML
  private var showConfirmationYNCButton: jfxsc.Button = _
  @jfxf.FXML
  private var showInformationButton: jfxsc.Button = _
  @jfxf.FXML
  private var showWarningButton: jfxsc.Button = _
  @jfxf.FXML
  private var showErrorButton: jfxsc.Button = _
  @jfxf.FXML
  private var showExceptionButton: jfxsc.Button = _

  override def initialize(): Unit =
    showConfirmationButton.onAction = () => model.onShowConfirmation()
    showConfirmationYNCButton.onAction = () =>
      model.onShowConfirmationYesNoCancel()
    showInformationButton.onAction = () => model.onShowInformation()
    showWarningButton.onAction = () => model.onShowWarning()
    showErrorButton.onAction = () => model.onShowError()
    showExceptionButton.onAction = () => model.onShowException()
