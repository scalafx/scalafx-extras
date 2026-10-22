/*
 * Copyright (c) 2011-2026, ScalaFX Project
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

import org.scalafx.extras.initFX
import org.scalatest.Outcome
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Try

class GenericPaneSpec extends AnyFlatSpec with Matchers {

  // Controls require JavaFX toolkit, it may not be possible to start it in a headless environment
  private lazy val fxAvailable: Boolean = Try(initFX()).isSuccess

  override def withFixture(test: NoArgTest): Outcome = {
    assume(fxAvailable, "JavaFX toolkit cannot be initialized")
    super.withFixture(test)
  }

  private def createPane(): GenericPane = {
    val gp = new GenericPane()
    gp.addCheckbox("Flag", defaultValue = true)
    gp.addChoice("Choice", Seq("a", "b", "c"), "b")
    gp.addNumericField("Number", 3.5)
    gp.addStringField("Text", "hello")
    gp
  }

  private def readAll(gp: GenericPane): (Boolean, String, Double, String) =
    (gp.nextBoolean(), gp.nextChoice(), gp.nextNumber(), gp.nextString())

  "GenericPane" should "read content of all field types" in {
    readAll(createPane()) shouldBe (true, "b", 3.5, "hello")
  }

  it should "read all field types again after resetReadout()" in {
    val gp    = createPane()
    val first = readAll(gp)

    gp.resetReadout()

    readAll(gp) shouldBe first
  }

  it should "fail reading past the last field without resetReadout()" in {
    val gp = createPane()
    readAll(gp)
    an[IllegalArgumentException] should be thrownBy gp.nextBoolean()
  }
}
