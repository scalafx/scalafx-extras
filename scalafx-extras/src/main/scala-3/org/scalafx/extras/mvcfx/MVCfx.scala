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

import javafx.{fxml as jfxf, scene as jfxs, util as jfxu}
import org.scalafx.extras.*
import scalafx.Includes.*
import scalafx.scene.{Parent, Scene}
import scalafx.stage.Stage

import java.io.IOException
import java.net.URL
import scala.reflect.*

/**
 * MVCfx is the "root" class for creation of UI components using MVCfx pattern. It instantiates and binds together the
 * model, the controller, and the view (FXML).
 *
 * The implementation of a class that extends MVCfx is very simple, it only needs instance of the model and information
 * about location of the FXML resource. For example:
 * {{{
 * import org.scalafx.extras.mvcfx.MVCfx
 *
 * class StopWatch(val model: StopWatchModel = new StopWatchModel())
 *   extends MVCfx[StopWatchController]("StopWatch.fxml"):
 *
 *   def controllerInstance: StopWatchController = new StopWatchController(model)
 * }}}
 *
 * The implementation will include:
 *   - `StopWatch` extends MVCfx
 *   - `StopWatchModel` extends ModelFX
 *   - `StopWatchController` extends ControllerFX
 *   - `StopWatch.fxml`
 *
 * The complete example in in demo module.
 *
 * See more details on MVCfx see [[https://github.com/scalafx/scalafx-extras/wiki/MVCfx-Pattern MVCfx Pattern wiki]].
 */
abstract class MVCfx[T <: ControllerFX](fxmlFilePath: String)(implicit tag: ClassTag[T]):

  /** UI model for this component. */
  def model: ModelFX

  /** Top level UI node for this component. */
  lazy val view: Parent =
    val _view = createFXMLView()
    // Delay assigment till `view` is accessed first time
    model.parent.value = _view
    _view

  /**
   * Create an instance of a controller.
   *
   * Example:
   * {{{
   *   def controllerInstance: MyController = new MyController(model)
   * }}}
   * where `MyController` extends `ControllerFX`
   */
  protected def controllerInstance: ControllerFX

  /**
   * Create a stage containing this component. The model is initialized on a separate thread.
   */
  def createStage(title: String): Stage =

    val caption = title

    // Create UI
    val stage = new Stage():
      this.title = caption
      scene = new Scene(view)
      onCloseRequest = () => model.shutDown()

    // Initialize model
    // Use worker thread for non-UI operations
    runTask(
      name = s"$caption model.startUp",
      task = new javafx.concurrent.Task[Unit]:
        override def call(): Unit = model.startUp()

        override def failed(): Unit =
          val message = s"Error while initializing view for '$title'."
          showException(title, message, exceptionProperty.get(), Option(stage))
    )

    stage

  /**
   * Creates parent using provided FXML file (`fxmlFilePath`).
   *
   * @return
   * parent node of the loaded FXML view.
   */
  private def createFXMLView(): Parent =
    val loader = new jfxf.FXMLLoader(resourceURL)
    loader.setControllerFactory(controllerFactory)
    val parent: jfxs.Parent = loader.load()
    parent

  private def resourceURL: URL =
    val resource = getClass.getResource(fxmlFilePath)
    if resource != null then
      resource
    else
      throw new IOException("Cannot load resource: '" + fxmlFilePath + "'")

  private def loadFXML(controllerFactory: jfxu.Callback[Class[?], Object]): Parent =
    val loader = new jfxf.FXMLLoader(resourceURL)
    loader.setControllerFactory(controllerFactory)
    loader.load()

  /**
   * Factory method for creating the controller object for this component.
   */
  private def controllerFactory: jfxu.Callback[Class[?], Object] =
    (controllerType: Class[?]) =>
      if controllerType == classTag[T].runtimeClass then
        controllerInstance
      else
        throw new IllegalStateException("Unexpected controller class: " + controllerType.getName)
