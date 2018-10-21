/*
 * Copyright (c) 2011-2018, ScalaFX Project
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

package org.scalafx.extras.mvcfx

import javafx.scene.Node
import scalafx.Includes._
import scalafx.beans.property.ObjectProperty
import scalafx.stage.Window
import scalafx.stage.Window.sfxWindow2jfx

/**
  * Trait for for implementing component logic.
  * Is not aware how the UI structure is implemented.
  * Contains references to parent and parentWindow to help display dialogs.
  *
  * See more details in the [[org.scalafx.extras.mvcfx `org.scalafx.extras.mvcfx`]] documentation.
  */
trait ModelFX {

  /**
    * Parent node of the view. Can be null.
    */
  val parent: ObjectProperty[Node] = new ObjectProperty(this, "parent", null)

  /**
    * Window of the parent node. Can be null.
    */
  def parentWindow: Option[Window] =
    Option(parent.value).flatMap(n => Option(n.scene()).map(s => sfxWindow2jfx(s.window())))

  def startUp(): Unit = {}

  def shutDown(): Unit = {}

}
