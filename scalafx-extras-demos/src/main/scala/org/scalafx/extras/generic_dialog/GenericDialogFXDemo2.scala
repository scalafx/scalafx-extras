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

import org.scalafx.extras.initFX

object GenericDialogFXDemo2 {

  def main(args: Array[String]): Unit = {

    // Initialize JavaFX, so we can display the dialog
    initFX()


    // Create dialog
    val dialog =
      new GenericDialogFX(
        title = "GenericDialogFX Demo",
        header = Option("Fancy description can go here.")
      ) {
        // Add fields
        addCheckbox("Check me out!", defaultValue = false)
        addCheckbox("Check me too!", defaultValue = true)
      }

    // Show dialog to the user
    dialog.showDialog()

    // Read input provided by the user
    if (dialog.wasOKed) {
      val select1 = dialog.nextBoolean()
      val select2 = dialog.nextBoolean()

      println(s"Selection 1: $select1")
      println(s"Selection 2: $select2")
    } else {
      println("Dialog was cancelled.")
    }


    // Use of initFX() requires explicit application exit
    System.exit(0)
  }
}
