/*
 * Copyright (c) 2011-2025, ScalaFX Project
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

import scalafx.Includes.*
import scalafx.scene.Node
import scalafx.scene.control.Alert.AlertType
import scalafx.scene.control.{Alert, ButtonType, Label, TextArea}
import scalafx.scene.layout.{GridPane, Priority, Region}
import scalafx.stage.Window

import java.io.{PrintWriter, StringWriter}

object ShowMessage {

  private def showMessage(parentWindow: Option[Window]): ShowMessage = {
    val pw = parentWindow
    new ShowMessage {
      override def parentWindow: Option[Window] = pw
    }
  }

  /**
   * Show error dialog
   *
   * @param title        dialog title
   * @param header       header text.
   * @param content      main content text.
   * @param parentWindow owner window that will be blacked by the dialog.
   */
  def error(title: String, header: String, content: String = "", parentWindow: Option[Window] = None): Unit =
    showMessage(parentWindow).showError(title, header, content)

  /**
   * Show a modal dialog with an expandable details about an exception (stack trace).
   *
   * @param title     dialog title
   * @param message   message shown in the dialog header.
   * @param t         exception.
   * @param ownerNode owner window that will be blacked by the dialog. Can be `null`.
   */
  def exception(title: String, message: String, t: Throwable, ownerNode: Node): Unit =
    exception(title, message, t, parentWindow(ownerNode))

  /**
   * Show a modal dialog with an expandable details about an exception (stack trace).
   *
   * @param title        dialog title
   * @param message      message shown in the dialog header.
   * @param t            exception.
   * @param parentWindow owner window that will be blacked by the dialog. Can be `null` to match JavaFX convention.
   */
  def exception(title: String, message: String, t: Throwable, parentWindow: Window): Unit =
    exception(title, message, t, Option(parentWindow))

  /**
   * Show a modal dialog with an expandable details about an exception (stack trace).
   *
   * @param title        dialog title
   * @param message      message shown in the dialog header.
   * @param t            exception.
   * @param parentWindow owner window that will be blacked by the dialog.
   */
  def exception(
    title: String,
    message: String,
    t: Throwable,
    parentWindow: Option[Window] = None,
    resizable: Boolean = false
  ): Unit = {
    t.printStackTrace()

    // Rename to avoid name clashes
    val _title     = title
    val _resizable = resizable

    // Create expandable Exception.
    val exceptionText = {
      val sw = new StringWriter()
      val pw = new PrintWriter(sw)
      t.printStackTrace(pw)
      sw.toString
    }
    val label = new Label("The exception stack trace was:")
    val textArea = new TextArea {
      text = exceptionText
      editable = false
      wrapText = true
      maxWidth = Double.MaxValue
      maxHeight = Double.MaxValue
      vgrow = Priority.Always
      hgrow = Priority.Always
    }
    val expContent = new GridPane {
      maxWidth = Double.MaxValue
      add(label, 0, 0)
      add(textArea, 0, 1)
    }

    onFXAndWait {
      new Alert(AlertType.Error) {
        initOwner(parentWindow.orNull)
        this.title = _title
        headerText = message
        contentText = Option(t.getMessage).getOrElse("")
        // Set expandable Exception into the dialog pane.
        dialogPane().expandableContent = expContent
        this.resizable = _resizable
      }.showAndWait()
    }
  }

  /**
   * Show information dialog
   *
   * @param title        dialog title
   * @param header       header text.
   * @param content      main content text.
   * @param parentWindow owner window that will be blacked by the dialog.
   */
  def information(title: String, header: String, content: String = "", parentWindow: Option[Window] = None): Unit =
    showMessage(parentWindow).showInformation(title, header, content)

  def information(title: String, header: String, content: String, ownerNode: Node): Unit =
    showMessage(parentWindow(ownerNode)).showInformation(title, header, content)

  /**
   * Show a warning dialog
   *
   * @param title        dialog title
   * @param header       header text.
   * @param content      main content text.
   * @param parentWindow owner window that will be blacked by the dialog.
   */
  def warning(title: String, header: String, content: String, parentWindow: Option[Window] = None): Unit =
    showMessage(parentWindow).showWarning(title, header, content)

  /**
   * Show a confirmation dialog with "OK" and "Cancel" buttons.
   *
   * @param title        dialog title.
   * @param header       header text.
   * @param content      content text.
   * @param parentWindow owner window that will be blacked by the dialog.
   * @return `true` when the user selected 'OK' and `false` when the user selected `Cancel` or dismissed the dialog.
   */
  def confirmation(title: String, header: String, content: String = "", parentWindow: Option[Window] = None): Boolean =
    showMessage(parentWindow).showConfirmation(title, header, content)

  /**
   * Show a confirmation dialog with "OK", "No", and "Cancel" buttons.
   *
   * @param title        dialog title.
   * @param header       header text.
   * @param content      content text.
   * @param parentWindow owner window that will be blacked by the dialog.
   * @return `Some(true)` when the user selected 'OK', `Some(false)` when the user selected `No`,
   *         and `None` user selected `Cancel` or dismissed the dialog.
   */
  def confirmationYesNoCancel(
    title: String,
    header: String,
    content: String = "",
    parentWindow: Option[Window] = None
  ): Option[Boolean] =
    showMessage(parentWindow).showConfirmationYesNoCancel(title, header, content)

}

/**
 * Mixin that adds the ability to easily show message dialogs.
 * A messageLogger can be provided, so when the error or warning dialogs are shown, they are also logged.
 *
 * A ShowMessage mixin will typically be used with the [[org.scalafx.extras.mvcfx.ModelFX ModelFX]].
 *
 * @author Jarek Sacha
 */
trait ShowMessage {

  /**
   * Parent window for a dialog. Dialogs are shown as modal, the window will be blocked while the dialog is displayed.
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
  def showError(title: String, header: String, content: String = "", resizable: Boolean = false): Unit = {
    messageLogger.foreach(_.error(s"<$title> $header $content"))
    // Rename to avoid name clashes
    val _title     = title
    val _resizable = resizable

    onFXAndWait {
      new Alert(AlertType.Error) {
        initOwner(parentWindow.orNull)
        this.title = _title
        headerText = header
        contentText = content
        this.resizable = _resizable
        dialogPane().setMinWidth(Region.UsePrefSize)
        dialogPane().setMinHeight(Region.UsePrefSize)
      }.showAndWait()
    }
  }

  /**
   * Displays an error dialog with expandable exception information.
   *
   * @param title   Dialog title
   * @param message Message (excluding t.getMessage(), it is automatically displayed)
   * @param t       exception to be displayed in the dialog
   */
  def showException(title: String, message: String, t: Throwable, resizable: Boolean = false): Unit = {
    messageLogger.foreach(_.error(s"<$title> $message", t))
    ShowMessage.exception(title, message, t, parentWindow)
  }

  /**
   * Show information dialog
   *
   * @param title   dialog title
   * @param header  header text.
   * @param content main content text.
   */
  def showInformation(title: String, header: String, content: String, resizable: Boolean = false): Unit = {
    //    messageLogger.info(s"<$title> $header $content")
    // Rename to avoid name clashes
    val _title     = title
    val _resizable = resizable

    onFXAndWait {
      new Alert(AlertType.Information) {
        initOwner(parentWindow.orNull)
        this.title = _title
        headerText = header
        contentText = content
        this.resizable = _resizable
        dialogPane().setMinWidth(Region.UsePrefSize)
        dialogPane().setMinHeight(Region.UsePrefSize)
      }.showAndWait()
    }
  }

  /**
   * Show a warning dialog
   *
   * @param title   dialog title
   * @param header  header text.
   * @param content main content text.
   */
  def showWarning(title: String, header: String, content: String, resizable: Boolean = false): Unit = {
    messageLogger.foreach(_.warn(s"<$title> $header $content"))
    // Rename to avoid name clashes
    val _title     = title
    val _resizable = resizable

    onFXAndWait {
      new Alert(AlertType.Warning) {
        initOwner(parentWindow.orNull)
        this.title = _title
        headerText = header
        contentText = content
        this.resizable = _resizable
        dialogPane().setMinWidth(Region.UsePrefSize)
        dialogPane().setMinHeight(Region.UsePrefSize)
      }.showAndWait()
    }
  }

  /**
   * Show a confirmation dialog with "OK" and "Cancel" buttons.
   *
   * @param title   dialog title.
   * @param header  header text.
   * @param content content text.
   * @return `true` when the user selected 'OK' and `false` when the user selected `Cancel` or dismissed the dialog.
   */
  def showConfirmation(title: String, header: String, content: String = "", resizable: Boolean = false): Boolean = {
    // Rename to avoid name clashes
    val _title     = title
    val _resizable = resizable

    val result = onFXAndWait {
      new Alert(AlertType.Confirmation) {
        initOwner(parentWindow.orNull)
        this.title = _title
        headerText = header
        contentText = content
        this.resizable = _resizable
        dialogPane().setMinWidth(Region.UsePrefSize)
        dialogPane().setMinHeight(Region.UsePrefSize)
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
   * @return `Some(true)` when the user selected 'OK', `Some(false)` when the user selected `No`,
   *         and `None` user selected `Cancel` or dismissed the dialog.
   */
  def showConfirmationYesNoCancel(
    title: String,
    header: String,
    content: String = "",
    resizable: Boolean = false
  ): Option[Boolean] = {
    // Rename to avoid name clashes
    val _title     = title
    val _resizable = resizable

    val result = onFXAndWait {
      new Alert(AlertType.Confirmation) {
        initOwner(parentWindow.orNull)
        this.title = _title
        headerText = header
        contentText = content
        this.resizable = _resizable
        dialogPane().setMinWidth(Region.UsePrefSize)
        dialogPane().setMinHeight(Region.UsePrefSize)
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
