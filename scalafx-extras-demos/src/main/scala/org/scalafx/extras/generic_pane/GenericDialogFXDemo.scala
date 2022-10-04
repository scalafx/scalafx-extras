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
import scalafx.application.JFXApp3
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.Scene
import scalafx.scene.control.{Button, ColorPicker}
import scalafx.scene.image.Image
import scalafx.scene.layout.{BorderPane, VBox}
import scalafx.scene.text.{Font, FontWeight}
import scalafx.stage.Window

object GenericDialogFXDemo extends JFXApp3 {


  def openGenericDialog(parentWindow: Option[Window]): Unit = {

    // We will use a custom control that is nt provided by GenericDialogFX
    val myColorPicker = new ColorPicker()

    val dialog =
      new GenericDialogFX(
        title = "GenericDialogFX Demo",
        header = "An attempt to emulate ImageJ's GenericDialog.",
        parentWindow = parentWindow
      ) {
        addCheckbox("Check me out!", defaultValue = false)
        addChoice("Make a choice", Array("A", "B", "C", "D"), "B")
        addDirectoryField("Input images", "images")
        addDirectoryField("Input masks", "masks", 33)
        addFileField("Configuration file")
        addStringField("Enter some text", "?", 24)
        addMessage("My bold message", Font.font(Font.default.family, FontWeight.Bold, Font.default.size * 1.25))
        // Here will add a custom control that we are handling on our own
        addNode("My custom control", myColorPicker)
        addNumericField("What's your number", 23.17)

        addHelp("https://github.com/scalafx/scalafx-extras")
      }

    dialog.showDialog()

    val (status, result) = if (dialog.wasOKed) {
      val select1 = dialog.nextBoolean()
      val choice2 = dialog.nextChoice()
      val directory3 = dialog.nextString()
      val directory4 = dialog.nextString()
      val file5 = dialog.nextString()
      val text6 = dialog.nextString()
      // Read our custom control
      val color7 = myColorPicker.value()
      val number8 = dialog.nextNumber()

      (
        "GenericDialogFX was OKed.",
        s"""Values of inputs:
           |  Selection 1 : $select1
           |  Choice 2    : $choice2
           |  Directory 3 : $directory3
           |  Directory 4 : $directory4
           |  File 5      : $file5
           |  Text 6      : $text6
           |  Color 7     : $color7
           |  Number 8    : $number8
           |""".stripMargin
      )

    } else {
      (
        "GenericDialogFX was cancelled",
        ""
      )
    }


    //    ShowMessage.information("GenericDialogFX Result", status, result, parentWindow)

    new GenericDialogFX("GenericDialogFX Result", status, parentWindow) {
      addMessage(result, Font.font("Monospaced", Font.default.size))
    }.showDialog()
  }


  override def start(): Unit = {

    val button = new Button {
      text = "Open GenericDialogFX"
      onAction = () => openGenericDialog(Option(stage))
      maxWidth = Double.MaxValue
    }

    stage = new JFXApp3.PrimaryStage {
      icons += new Image("/org/scalafx/extras/sfx.png")
      title = "StopWatch"
      scene = new Scene {
        root = new BorderPane {
          center = new VBox {
            spacing = 9
            alignment = Pos.Center
            padding = Insets(50)
            children ++= Seq(button)
          }
        }
      }
    }
  }
}
