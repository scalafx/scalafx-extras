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
 * A helper for crating custom panes. Particularly suited for creation of input controls.
 *
 * There are 4 steps to using a generic pane:
 *
 * 1. Creation, where elements of the pane are appended vertically using `add*(...)` methods, * for instance,
 *    `addStringField(label, defaultText)`
 *
 * 2. Adding the pane to the UI
 *
 * 3. User interaction, after the pane is displayed
 *
 * 4. Optionally, reading of input. Pane editable content can be read using `next*()` methods.
 *    Content is read in the order it is added.
 *    The whole pane content can be read multiple tiles.
 *    Remember to call `resetReadout()` to ensure that reading is restarted from the beginning of the pane.
 *
 * Example:
 * {{{
 *   // Build a pane
 *   val gp = new GenericPane()
 *   gp.addDirectoryField("Input", "images")
 *   gp.addDirectoryField("Output", "output")
 *
 *   // Use it in some other control
 *   ...
 *
 *   // Later ...
 *   // Print its content
 *   gp.resetReadout()
 *   println(s"Input dir : ${gp.nextString()}")
 *   println(s"Output dir: ${gp.nextString()}")
 * }}}
 *
 * @param lastDirectoryHandler customize how directory selections are remembered between uses of the dialog. Used with `addDirectoryField` and `addFileField`.
 * @see GenericDialogFX
 */
class GenericPane(val lastDirectoryHandler: LastDirectoryHandler = new DefaultLastDirectoryHandler())
    extends GenericPaneBase {
  require(lastDirectoryHandler != null, "Argument 'lastDirectoryHandler' cannot be 'null'")

  override def parentWindow: Option[Window] = super.parentWindow

  override def resetReadout(): Unit = super.resetReadout()
  override def pane: Node           = super.pane
}
