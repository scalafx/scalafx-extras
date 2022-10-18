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

import scalafx.Includes.*
import scalafx.application.Platform
import scalafx.beans.property.StringProperty
import scalafx.collections.ObservableBuffer
import scalafx.geometry.Insets
import scalafx.scene.Node
import scalafx.scene.control.{CheckBox, ChoiceBox, Label, TextField}
import scalafx.scene.layout.{ColumnConstraints, GridPane, HBox, Priority}
import scalafx.scene.text.Font
import scalafx.stage.Window

import scala.collection.mutable.ListBuffer

/**
  * 
  * @param lastDirectoryHandler customize how directory selections are remembered between uses of the dialog. Used with `addDirectoryField` and `addFileField`.
  */
trait GenericPaneBase {
  require(lastDirectoryHandler != null, "Argument 'lastDirectoryHandler' cannot be 'null'")
  
  private val _labeledControls = ListBuffer.empty[(String, Node)]
  private val _checkBoxes = ListBuffer.empty[CheckBox]
  private val _choiceBoxes = ListBuffer.empty[ChoiceBox[String]]
  private val _numberTextFields = ListBuffer.empty[NumberTextField]
  private val _stringProperties = ListBuffer.empty[StringProperty]

  def lastDirectoryHandler: LastDirectoryHandler


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

  private var _rowIndex = 0
  private var _checkBoxNextIndex = 0
  private var _choiceBoxNextIndex = 0
  private var _numberTextFieldNextIndex = 0
  private var _stringPropertyNextIndex = 0


  protected def parentWindow: Option[Window] =
    Option(_grid).flatMap(n => Option(n.scene()).map(s => jfxWindow2sfx(s.window())))

  protected def resetReadout(): Unit = {
    _stringPropertyNextIndex = 0
  }

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
    * Adds a choice list.
    *
    * @param label       the label
    * @param items       items on the list
    * @param defaultItem the initial item, must be equal to one of the `items`
    */
  def addChoice(label: String, items: Seq[String], defaultItem: String): Unit =
    addChoice(label, items.toArray, defaultItem)


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

  protected def pane: Node = _grid
  
  protected def requestFocusOnFirstLabeled(): Unit = {
    _labeledControls.headOption.foreach(l => Platform.runLater(l._2.requestFocus()))
  }
}
