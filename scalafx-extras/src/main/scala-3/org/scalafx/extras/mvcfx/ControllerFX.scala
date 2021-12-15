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

package org.scalafx.extras.mvcfx

import javafx.fxml as jfxf

/**
 * The ControllerFX creates connection of the FXML to Scala code and underlying ModelFX for the application logic.
 *
 * Constructor argument names correspond to controls defined in FXML and the model. The constructor is used by ScalaFXML
 * macro to automatically expose FXML controls in Scala code of the view class.
 *
 * See more details in the [[org.scalafx.extras.mvcfx `org.scalafx.extras.mvcfx`]] documentation.
 *
 * Example:
 * {{{
 * import org.scalafx.extras.mvcfx.ControllerFX
 *
 * import scalafx.Includes.*
 * import scalafx.scene.control.Button
 *
 * import javafx.scene.control as jfxsc
 * import javafx.fxml as jfxf
 *
 * class StopWatchController(model: StopWatchModel) extends ControllerFX:
 *
 *   @jfxf.FXML
 *   private var startButton: jfxsc.Button = _
 *
 *   override def initialize(): Unit =
 *     startButton.disable <== model.running
 *     startButton.onAction = () => model.onStart()
 * }}}
 */
trait ControllerFX:

  /**
   * Performs custom initialization of the this controller.
   * It is called by JavaFX runtime after associated FXML from was loaded and controls instantiated.
   */
  def initialize(): Unit

  @jfxf.FXML
  private def initializeImpl(): Unit = initialize()
