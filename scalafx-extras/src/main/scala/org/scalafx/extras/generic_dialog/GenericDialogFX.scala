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

package org.scalafx.extras.generic_dialog

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
  *         header = Option("Fancy description can go here.")
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
  * @param title                dialogs title
  * @param header               optional header
  * @param parentWindow         optional parent window that will be blocked when this dialog is displayed.
  * @param lastDirectoryHandler customize how directory selections are remembered between uses of the dialog. Used with `addDirectoryField` and `addFileField`.
  */
class GenericDialogFX(val title: String,
                      val header: Option[String] = None,
                      val parentWindow: Option[Window] = None,
                      val lastDirectoryHandler: LastDirectoryHandler = new DefaultLastDirectoryHandler()) {

  import GenericDialogFX.*

  lazy private val _helpLabel: String = "Help"
  private val ButtonTypeHelp = new ButtonType(helpLabel, ButtonData.Help)
  private val _labeledControls = ListBuffer.empty[(String, Node)]
  private val _checkBoxes = ListBuffer.empty[CheckBox]
  private val _choiceBoxes = ListBuffer.empty[ChoiceBox[String]]
  private val _numberTextFields = ListBuffer.empty[NumberTextField]
  private val _stringProperties = ListBuffer.empty[StringProperty]
  private val _grid: GridPane = new GridPane() {
    hgap = 5
    vgap = 5
    padding = Insets(10, 10, 10, 10)
    maxWidth = Double.MaxValue
    hgrow = Priority.Always
    private val constrains = Seq(
      new ColumnConstraints(),
      new ColumnConstraints(),
      new ColumnConstraints(),
      new ColumnConstraints() {
        hgrow = Priority.Always
      }
    )
    columnConstraints.addAll(constrains.map(_.delegate) *)
  }
  private var _wasOKed = false
  private var _rowIndex = 0
  private var _checkBoxNextIndex = 0
  private var _choiceBoxNextIndex = 0
  private var _numberTextFieldNextIndex = 0
  private var _stringPropertyNextIndex = 0
  private var _helpURLOption: Option[String] = None

  /**
    * Adds a checkbox.
    *
    * @param label        the label
    * @param defaultValue the initial state
    */
  def addCheckbox(label: String, defaultValue: Boolean): Unit = {
    val label2 = label.replace('_', ' ')

    val checkBox = new CheckBox()
    checkBox.selected = defaultValue

    _grid.add(new Label(label2), 0, _rowIndex)
    _grid.add(checkBox, 1, _rowIndex)
    _rowIndex += 1

    _labeledControls.append((label, checkBox))
    _checkBoxes += checkBox
  }

  /**
    * Adds a choice list.
    *
    * @param label       the label
    * @param items       items on the list
    * @param defaultItem the initial item, must be equal to one of the `items`
    */
  def addChoice(label: String, items: Array[String], defaultItem: String): Unit = {

    require(items.contains(defaultItem))

    val label2 = label.replace('_', ' ')

    val choiceBox = new ChoiceBox[String](ObservableBuffer.from(items))
    choiceBox.selectionModel.value.select(defaultItem)

    _grid.add(new Label(label2), 0, _rowIndex)
    _grid.add(choiceBox, 1, _rowIndex)
    _rowIndex += 1

    _labeledControls.append((label, choiceBox))
    _choiceBoxes += choiceBox

  }

  /**
    * Adds a directory text field and "Browse" button, where the field width is determined by the length of
    * 'defaultPath', with a minimum of 25 columns. Use nextString to retrieve the directory path.
    *
    * @param label       the label
    * @param defaultPath initial path
    */
  def addDirectoryField(label: String, defaultPath: String): Unit = {
    val columns =
      if (defaultPath != null) Math.max(defaultPath.length, 25)
      else 25
    addDirectoryField(label, defaultPath, columns)
  }

  def addDirectoryField(label: String, defaultPath: String, columns: Int): Unit = {
    val label2 = label.replace('_', ' ')

    val directorySelectionField = new DirectorySelectionField(label2, parentWindow, lastDirectoryHandler)
    directorySelectionField.path.value = defaultPath

    _grid.add(new Label(label2), 0, _rowIndex)
    _grid.add(directorySelectionField.view, 1, _rowIndex, GridPane.Remaining, 1)
    _rowIndex += 1

    _labeledControls.append((label, directorySelectionField.view))
    _stringProperties += directorySelectionField.path
  }

  /**
    * Adds a file text field and "Browse" button, where the field width is determined by the length of
    * 'defaultPath', with a minimum of 25 columns. Use nextString to retrieve the file path.
    *
    * @param label       the label
    * @param defaultPath initial path
    */
  def addFileField(label: String, defaultPath: String = ""): Unit = {
    val label2 = label.replace('_', ' ')

    val fileSelectionField = new FileSelectionField(label2, parentWindow, lastDirectoryHandler)
    fileSelectionField.path.value = defaultPath

    _grid.add(new Label(label2), 0, _rowIndex)
    _grid.add(fileSelectionField.view, 1, _rowIndex, GridPane.Remaining, 1)
    _rowIndex += 1

    _labeledControls.append((label, fileSelectionField.view))
    _stringProperties += fileSelectionField.path
  }

  /**
    * Adds a "Help" button that opens the specified URL in the default browser.
    *
    * @param url the URL to open in the default browser
    */
  def addHelp(url: String): Unit = {
    _helpURLOption = Option(url)
  }

  /**
    * Adds a message consisting of one or more lines of text.
    *
    * That message cannot be edited, cannot be edited with a `next*()`
    *
    * @param message message
    * @param font    font used to render the message
    */
  def addMessage(message: String, font: Font): Unit = {
    addMessage(message, Option(font))
  }

  /**
    * Adds a message consisting of one or more lines of text.
    *
    * That message cannot be edited, cannot be edited with a `next*()`
    *
    * @param message message
    * @param font    Optional font used to render the message
    */

  def addMessage(message: String, font: Option[Font] = None): Unit = {
    val label = Label(message)
    font.foreach(label.font = _)

    _grid.add(label, 0, _rowIndex, GridPane.Remaining, 1)
    _rowIndex += 1
  }

  /** Add a custom node that will occupy a whole row */
  def addNode(node: Node): Unit = {
    _grid.add(node, 0, _rowIndex, GridPane.Remaining, 1)
    _rowIndex += 1
  }

  /** Add a custom node
    *
    * @param label the label
    * @param node  custom node. It is not included in `next*()` readouts, needs to be handled by the creator
    */
  def addNode(label: String, node: Node): Unit = {
    val label2 = label.replace('_', ' ')

    _grid.add(new Label(label2), 0, _rowIndex)
    _grid.add(node, 1, _rowIndex, GridPane.Remaining, 1)
    _rowIndex += 1
  }

  /**
    * Add a numeric field.
    *
    * @param label        the label
    * @param defaultValue the initial value
    */
  def addNumericField(label: String, defaultValue: Double): Unit = {
    val decimalPlaces = if (defaultValue.toInt == defaultValue) 0 else 3
    val columnWidth = if (decimalPlaces == 3) 8 else 6
    addNumericField(label, defaultValue, decimalPlaces, columnWidth, "")
  }

  /**
    * Add a numeric field.
    *
    * @param label         the label
    * @param defaultValue  the initial value
    * @param decimalPlaces number of decimal places to display
    * @param columnWidth   number of columns used to display the number
    * @param units         text displayed after the number
    */
  def addNumericField(label: String,
                      defaultValue: Double,
                      decimalPlaces: Int,
                      columnWidth: Int,
                      units: String): Unit = {
    require(columnWidth > 0)

    val label2 = label.replace('_', ' ')
    val textField = new NumberTextField(decimalPlaces) {
      prefColumnCount = columnWidth
      model.value = defaultValue
    }

    _grid.add(new Label(label2), 0, _rowIndex)
    _grid.add(textField, 1, _rowIndex)
    _grid.add(new Label(units), 2, _rowIndex)
    _rowIndex += 1

    _labeledControls.append((label, textField))
    _numberTextFields += textField
  }


  /**
    * Adds an 8 column text field.
    *
    * @param label       the label
    * @param defaultText the text initially displayed
    */
  def addStringField(label: String, defaultText: String): Unit = {
    addStringField(label, defaultText, 8)
  }

  /**
    * Adds a text field.
    *
    * @param label       the label
    * @param defaultText text initially displayed
    * @param columns     width of the text field
    */
  def addStringField(label: String, defaultText: String, columns: Int): Unit = {
    val label2 = label.replace('_', ' ')
    val textField = new TextField() {
      prefColumnCount = columns
      text.value = defaultText
    }

    _grid.add(new Label(label2), 0, _rowIndex)
    _grid.add(textField, 1, _rowIndex)
    _rowIndex += 1

    _labeledControls.append((label, textField))
    _stringProperties += textField.text
  }


  /**
    * Returns the state of the next checkbox
    */
  def nextBoolean(): Boolean = {
    require(_checkBoxNextIndex < _checkBoxes.size)

    val next = _checkBoxes(_checkBoxNextIndex).selected.value
    _checkBoxNextIndex += 1

    next
  }

  // noinspection AccessorLikeMethodIsEmptyParen
  def nextChoice(): String = {
    require(_choiceBoxNextIndex < _choiceBoxes.size)

    val next = _choiceBoxes(_choiceBoxNextIndex).selectionModel.value.selectedItem.value
    _choiceBoxNextIndex += 1

    next
  }

  /**
    * Returns the value of the next number
    */
  def nextNumber(): Double = {
    require(_numberTextFieldNextIndex < _numberTextFields.size)

    val next = _numberTextFields(_numberTextFieldNextIndex).model.value.value
    _numberTextFieldNextIndex += 1

    next.doubleValue()
  }

  /**
    * Returns the value of the next string
    */
  def nextString(): String = {
    require(_stringPropertyNextIndex < _stringProperties.size)

    val next = _stringProperties(_stringPropertyNextIndex).value
    _stringPropertyNextIndex += 1

    next
  }

  /**
    * Display the dialog and block till the dialog is closed
    */
  def showDialog(): Unit = {

    onFXAndWait {

      // Create the custom dialog.
      val dialog = new Dialog[Result]() {
        parentWindow.foreach(initOwner)
        this.title = GenericDialogFX.this.title
        header.foreach(headerText = _)
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

      dialog.dialogPane().content = _grid

      // Request focus on the first label by default
      _labeledControls.headOption.foreach(l => Platform.runLater(l._2.requestFocus()))

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
