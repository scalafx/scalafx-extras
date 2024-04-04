/*
 * Copyright (c) 2011-2024, ScalaFX Project
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

import org.scalafx.extras.onFX
import scalafx.beans.property.StringProperty
import scalafx.scene.Node
import scalafx.scene.control.{Button, TextField}
import scalafx.scene.layout.{HBox, Priority}
import scalafx.stage.{FileChooser, Window}

import java.io.File

/**
 * File selection control, accessible through `view`. The text field shows the path, the button allow browsing to
 * select the File.
 *
 * @param title                dialogs title
 * @param ownerWindow          the owner window of the displayed dialog
 * @param lastDirectoryHandler customize how directory selections are remembered between uses of the dialog.
 * @param columns              width of the text field
 */
class FileSelectionField(
  val title: String,
  val ownerWindow: Option[Window],
  val lastDirectoryHandler: LastDirectoryHandler = new DefaultLastDirectoryHandler(),
  val columns: Int = 12
) {
  private lazy val fileChooser: FileChooser = new FileChooser() {
    this.title = FileSelectionField.this.title
  }
  val path: StringProperty        = new StringProperty("")
  private var _view: Option[Node] = None

  def view: Node = {
    if (_view.isEmpty) {
      _view = Option(buildView())
    }
    _view.get
  }

  private def buildView(): Node = {

    val textField = new TextField() {
      hgrow = Priority.Always
      maxWidth = Double.MaxValue
      text <==> path
      prefColumnCount = columns
    }

    val button = new Button("Browse") {
      onAction = _ => {
        val initialPath = path.value
        if (initialPath.trim.nonEmpty) {
          val file = new File(initialPath)
          fileChooser.initialFileName = file.getName
          if (file.getParentFile.exists()) {
            fileChooser.initialDirectory = file.getParentFile
          }
        } else {
          val parent = lastDirectoryHandler.lastDirectory
          if (parent.exists())
            fileChooser.initialDirectory = parent
        }

        val selection = fileChooser.showOpenDialog(ownerWindow.orNull)

        Option(selection).foreach { s =>
          path.value = s.getCanonicalPath
          lastDirectoryHandler.lastDirectory = s
          onFX { textField.positionCaret(textField.text.length.get()) }
        }
      }
    }

    new HBox(3, textField, button)
  }

}
