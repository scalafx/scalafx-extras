/*
 * Copyright (c) 2011-2022, ScalaFX Project
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

package org.scalafx.extras.generic_pane

import org.scalafx.extras.onFXAndWait
import scalafx.Includes.*
import scalafx.application.Platform
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.control.*
import scalafx.scene.control.ButtonBar.ButtonData
import scalafx.scene.layout.{ColumnConstraints, GridPane, Priority}
import scalafx.scene.text.Font
import scalafx.stage.Window

import java.net.URL
import scala.collection.mutable.ListBuffer

object GenericDialogFX {

  /**
   * @param buttonPressed
   * button used to close the dialog
   */
  private case class Result(buttonPressed: Option[ButtonType])
}

/**
 * A helper for crating custom dialogs. Particularly suited for creation of input dialogs.
 *
 * There are 3 steps to using a dialog:
 * 1. Creation, where elements of the dialog are appended vertically using `add*(...)` methods,
 * for instance, `addStringField(label, defaultText)`
 * 2. User interaction, dialog is displayed using `showDialog()` method
 * 3. Reading of input, once the dialog is closed, dialog content can be read using `next*()` methods.
 * Content is read in the order it is added.
 *
 * Here is en example:
 *
 * {{{
 *     val dialog =
 *       new GenericDialogFX(
 *         title = "GenericDialogFX Demo",
 *         "Fancy description can go here."
 *       ) {
 *         addCheckbox("Check me out!", defaultValue = false)
 *         addCheckbox("Check me too!", defaultValue = true)
 *       }
 *
 *     dialog.showDialog()
 *
 *     if (dialog.wasOKed) {
 *       val select1 = dialog.nextBoolean()
 *       val select2 = dialog.nextBoolean()
 *
 *       println(s"Selection 1: $select1")
 *       println(s"Selection 2: $select2")
 *     }
 * }}}
 *
 * @param title dialogs title
 * @param header dialog header
 * @param ownerWindow optional owner window that will be blocked when this dialog is displayed.
 * @param lastDirectoryHandler customize how directory selections are remembered between uses of the dialog. Used with `addDirectoryField` and `addFileField`.
 * @see GenericPane
 */
class GenericDialogFX(
  val title: String,
  val header: String = "",
  ownerWindow: Option[Window] = None,
  val lastDirectoryHandler: LastDirectoryHandler = new DefaultLastDirectoryHandler()
) extends GenericPaneBase {
  require(title != null, "Argument 'title' cannot be 'null'")
  require(header != null, "Argument 'header' cannot be 'null'")
  require(ownerWindow != null, "Argument 'ownerWindow' cannot be 'null'")
  require(lastDirectoryHandler != null, "Argument 'lastDirectoryHandler' cannot be 'null'")

  import GenericDialogFX.*

  lazy private val _helpLabel: String        = "Help"
  private val ButtonTypeHelp                 = new ButtonType(helpLabel, ButtonData.Help)
  private var _wasOKed                       = false
  private var _helpURLOption: Option[String] = None

  /**
   * Adds a "Help" button that opens the specified URL in the default browser.
   *
   * @param url the URL to open in the default browser
   */
  def addHelp(url: String): Unit = {
    _helpURLOption = Option(url)
  }

  /**
   * Display the dialog and block till the dialog is closed
   */
  def showDialog(): Unit = {

    onFXAndWait {

      // Create the custom dialog.
      val dialog = new Dialog[Result]() {
        ownerWindow.foreach(initOwner)
        this.title = GenericDialogFX.this.title
        if (header.nonEmpty) {
          headerText = header
        }
        resizable = true
      }

      dialog.dialogPane().buttonTypes =
        if (_helpURLOption.isDefined)
          Seq(ButtonType.OK, ButtonType.Cancel, ButtonTypeHelp)
        else
          Seq(ButtonType.OK, ButtonType.Cancel)

      // Place to add validation to enable OK button
      //    // Enable/Disable OK button depending on whether data is validated
      //    val okButton = dialog.dialogPane().lookupButton(ButtonType.OK)
      //    okButton.disable = true

      //    // Do some validation (disable when username is empty).
      //    username.text.onChange { (_, _, newValue) =>
      //      okButton.disable = newValue.trim().isEmpty
      //    }

      dialog.dialogPane().content = pane

      // Request focus on the first label by default
      requestFocusOnFirstLabeled()

      // Pressing any of the "official" dialog pane buttons will come with close request.
      // If a help button was pressed we do not want to close this dialog, but we want to display Help dialog
      dialog.onCloseRequest = e => {
        e.getSource match {
          case d: javafx.scene.control.Dialog[Result] =>
            if (d.getResult.buttonPressed.forall(_ == ButtonTypeHelp)) {
              // Show help
              showHelp()
              // Cancel closing request
              e.consume()
            }
          case _ =>
        }
      }

      // When an "official" button is clicked, convert the result containing that button.
      // We use it to detect when Help button is pressed
      dialog.resultConverter = dialogButton => Result(Option(dialogButton))

      // We could use some more digested result
      val result = dialog.showAndWait()

      _wasOKed = result.contains(Result(Some(ButtonType.OK)))
    }
  }

  private def showHelp(): Unit = {
    _helpURLOption.foreach { helpURL =>
      if (helpURL.startsWith("<html>")) {
        //        val title1 = title + " " + helpLabel
        //        if (this.isInstanceOf[NonBlockingGenericDialog]) new HTMLDialog(title, helpURL, false) // non blocking
        //        else new HTMLDialog(this, title, helpURL)                                              //modal
        ???
      } else {
        //        val `macro` = "call('ij.plugin.BrowserLauncher.open', '" + helpURL + "');"
        //        new MacroRunner(`macro`) // open on separate thread using BrowserLauncher
        val url = new URL(helpURL)
        Utils.openWebpage(url)
        //        ij.plugin.BrowserLauncher.open(helpURL)
      }
    }
  }

  /**
   * `true` if the dialog was closed by cancelling
   */
  def wasCanceled: Boolean = !_wasOKed

  /**
   * `true` if the dialog was closed using OK button
   */
  def wasOKed: Boolean = _wasOKed

  /**
   * Label used for the help button
   */
  def helpLabel: String = _helpLabel

  //  def helpLabel_=(label: String): Unit = {
  //    require(label != null, "Argument 'label' cannot be null.")
  //    _helpLabel = label
  //  }
}
