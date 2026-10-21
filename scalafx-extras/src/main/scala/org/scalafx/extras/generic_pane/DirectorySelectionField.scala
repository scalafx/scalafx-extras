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

import org.scalafx.extras.generic_pane.Utils.{cleanFile, selfAndParents}
import org.scalafx.extras.onFX
import scalafx.beans.property.StringProperty
import scalafx.scene.Node
import scalafx.scene.control.{Button, TextField}
import scalafx.scene.layout.{HBox, Priority}
import scalafx.stage.{DirectoryChooser, Window}

import java.io.File

object DirectorySelectionField {

  /**
    * Determine initial directory for the directory chooser.
    *
    * @param path          current content of the path field, can be empty or `null`
    * @param lastDirectory last directory used, used when no existing directory can be found from `path`
    * @return initial directory, if it can be determined
    */
  private[generic_pane] def initialDirectory(path: String, lastDirectory: File): Option[File] =
    Option(path)
      .filter(_.trim.nonEmpty)
      // First existing directory, path may also point to an existing file or not exist at all
      .flatMap(p => selfAndParents(cleanFile(new File(p))).find(_.isDirectory))
      .orElse(Option(lastDirectory).filter(_.isDirectory).map(cleanFile))
}

/**
 * Directory selection control, accessible through `view`. The text field shows the path, the button opens a dialog to
 * select the directory.
 *
 * @param title                dialogs title
 * @param ownerWindow          the owner window of the displayed dialog
 * @param lastDirectoryHandler customize how directory selections are remembered between uses of the dialog.
 * @param columns              width of the text field
 */
class DirectorySelectionField(
  val title: String,
  val ownerWindow: Option[Window],
  val lastDirectoryHandler: LastDirectoryHandler = new DefaultLastDirectoryHandler(),
  val columns: Int = 12
) {

  import DirectorySelectionField.*

  private lazy val chooser: DirectoryChooser = new DirectoryChooser() {
    this.title = DirectorySelectionField.this.title
  }

  private var _view: Option[Node] = None
  val path: StringProperty        = new StringProperty("")

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
        chooser.initialDirectory = initialDirectory(path.value, lastDirectoryHandler.lastDirectory).orNull

        val selection = chooser.showDialog(ownerWindow.orNull)

        Option(selection).map(cleanFile).foreach { s =>
          path.value = s.getPath
          lastDirectoryHandler.lastDirectory = s
          onFX { textField.positionCaret(textField.text.length.get()) }
        }
      }
    }

    new HBox(3, textField, button)
  }

}
