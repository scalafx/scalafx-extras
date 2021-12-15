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

package org.scalafx.extras

import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType}
import scalafx.stage.Window

/**
 * Mixin that adds ability to easily show message dialogs.
 * A messageLogger can be provided, so when the error or warning dialogs are shown, they are also logged.
 *
 * A ShowMessage mixin will typically be used with the [[org.scalafx.extras.mvcfx.ModelFX ModelFX]].
 *
 * @author Jarek Sacha
 */
trait ShowMessage {

  /**
   * Parent window for a dialog. Dialogs are shown modal, the window will be blocked while dialog is displayed.
   */
  protected def parentWindow: Option[Window]

  /**
   * Logger to use for error and warning dialogs.
   */
  protected def messageLogger: Option[ShowMessageLogger] = None

  /**
   * Show error dialog
   *
   * @param title   dialog title
   * @param header  header text.
   * @param content main content text.
   */
  def showError(title: String, header: String, content: String = ""): Unit = {
    messageLogger.foreach(_.error(s"<$title> $header $content"))
    // Rename to avoid name clashes
    val dialogTitle = title

    onFXAndWait {
      new Alert(AlertType.Error) {
        initOwner(parentWindow.orNull)
        this.title = dialogTitle
        headerText = header
        contentText = content
      }.showAndWait()
    }
  }

  /**
   * Displays error dialog with expandable exception information.
   *
   * @param title   Dialog title
   * @param message Message (excluding t.getMessage(), it is automatically displayed)
   * @param t       exception to be displayed in the dialog
   */
  def showException(title: String, message: String, t: Throwable): Unit = {
    messageLogger.foreach(_.error(s"<$title> $message", t))
    org.scalafx.extras.showException(title, message, t, parentWindow)
  }

  /**
   * Show information dialog
   *
   * @param title   dialog title
   * @param header  header text.
   * @param content main content text.
   */
  def showInformation(title: String, header: String, content: String): Unit = {
    //    messageLogger.info(s"<$title> $header $content")
    // Rename to avoid name clashes
    val dialogTitle = title

    onFXAndWait {
      new Alert(AlertType.Information) {
        initOwner(parentWindow.orNull)
        this.title = dialogTitle
        headerText = header
        contentText = content
      }.showAndWait()
    }
  }

  /**
   * Show warning dialog
   *
   * @param title   dialog title
   * @param header  header text.
   * @param content main content text.
   */
  def showWarning(title: String, header: String, content: String): Unit = {
    messageLogger.foreach(_.warn(s"<$title> $header $content"))
    // Rename to avoid name clashes
    val dialogTitle = title

    onFXAndWait {
      new Alert(AlertType.Warning) {
        initOwner(parentWindow.orNull)
        this.title = dialogTitle
        headerText = header
        contentText = content
      }.showAndWait()
    }
  }

  /**
   * Show a confirmation dialog with "OK" and "Cancel" buttons.
   *
   * @param title   dialog title.
   * @param header  header text.
   * @param content content text.
   * @return `true` when user selected 'OK' and `false` when user selected `Cancel` or dismissed the dialog.
   */
  def showConfirmation(title: String, header: String, content: String = ""): Boolean = {
    // Rename to avoid name clashes
    val dialogTitle = title

    val result = onFXAndWait {
      new Alert(AlertType.Confirmation) {
        initOwner(parentWindow.orNull)
        this.title = dialogTitle
        headerText = header
        contentText = content
      }.showAndWait()
    }
    result match {
      case Some(ButtonType.OK) => true
      case _                   => false
    }
  }

  /**
   * Show a confirmation dialog with "OK", "No", and "Cancel" buttons.
   *
   * @param title   dialog title.
   * @param header  header text.
   * @param content content text.
   * @return `Some(true)` when user selected 'OK', `Some(false)` when user selected `No`,
   *         and `None` user selected `Cancel` or dismissed the dialog.
   */
  def showConfirmationYesNoCancel(title: String, header: String, content: String = ""): Option[Boolean] = {
    // Rename to avoid name clashes
    val dialogTitle = title

    val result = onFXAndWait {
      new Alert(AlertType.Confirmation) {
        initOwner(parentWindow.orNull)
        this.title = dialogTitle
        headerText = header
        contentText = content
        buttonTypes = Seq(ButtonType.OK, ButtonType.No, ButtonType.Cancel)
      }.showAndWait()
    }
    result match {
      case Some(ButtonType.OK) => Some(true)
      case Some(ButtonType.No) => Some(false)
      case _                   => None
    }
  }

}
