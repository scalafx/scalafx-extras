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

package org.scalafx.extras.modelview

import java.io.IOException

import org.scalafx.extras._
import scalafx.Includes._
import scalafx.scene.{Parent, Scene}
import scalafx.stage.Stage
import scalafxml.core.{ControllerDependencyResolver, ExplicitDependencies, FXMLView}


/**
  * The ModelViewComponent implementation is very simple,
  * it only needs instance of the model and information about location of the FXML resource.
  *
  * See more details in the [[org.scalafx.extras.modelview `org.scalafx.extras.modelview`]] documentation.
  *
  * Example
  * {{{
  * import org.scalafx.extras.modelview.ModelViewComponent
  *
  * class StopWatchComponent(val model: StopWatchModel = new StopWatchModel())
  *   extends ModelViewComponent("/org/scalafx/extras/modelview/stopwatch/StopWatchView.fxml")
  * }}}
  */
abstract class ModelViewComponent(fxmlFilePath: String) {

  /** UI model for this component. */
  def model: Model

  /** Top level UI node for this component. */
  val view: Parent = createFXMLView()
  model.parent.value = view

  /** Create a stage containing this component. The model is initialized on a separate thread. */
  def createStage(title: String): Stage = {

    val caption = title

    // Create UI
    val stage = new Stage() {
      title = caption
      scene = new Scene(view)
      onCloseRequest = () => model.shutDown()
    }

    // Initialize model
    // Use worker thread for non-UI operations
    runTask(
      name = s"$caption model.startUp",
      task = new javafx.concurrent.Task[Unit] {
        override def call(): Unit = model.startUp()

        override def failed(): Unit = {
          val message = s"Error while initializing view for '$title'."
          showException(title, message, exceptionProperty.get(), Option(stage))
        }
      }
    )

    stage
  }

  /**
    * Dependencies for the view. Default implementation only provides the model as a dependency.
    * Overwrite to add additional dependencies as needed.
    *
    * @return dependencies injected into the view when it is created.
    */
  protected def viewDependencies: ControllerDependencyResolver = {
    new ExplicitDependencies(Map("model" -> model))
  }

  /**
    * Creates FXMLView using provided FXML file (`fxmlFilePath`).
    *
    * @return parent node of the loaded FXML view.
    */
  private def createFXMLView(): Parent = {
    // Load main view
    val resource = getClass.getResource(fxmlFilePath)
    if (resource == null) {

      throw new IOException("Cannot load resource: '" + fxmlFilePath + "'")
    }

    FXMLView(resource, viewDependencies)
  }


}

