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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.nio.file.Files

class DirectorySelectionFieldSpec extends AnyFlatSpec with Matchers {

  private val tmpDir   = Files.createTempDirectory("DirectorySelectionFieldSpec").toFile.getCanonicalFile
  private val otherDir = Files.createTempDirectory("DirectorySelectionFieldSpec-other").toFile.getCanonicalFile

  "DirectorySelectionField.initialDirectory" should "use existing directory from path" in {
    DirectorySelectionField.initialDirectory(tmpDir.getPath, otherDir) shouldBe Some(tmpDir)
  }

  it should "use first existing parent of a missing directory" in {
    val missing = new File(tmpDir, "a/b/c")
    DirectorySelectionField.initialDirectory(missing.getPath, otherDir) shouldBe Some(tmpDir)
  }

  it should "use current directory for a relative path without parent" in {
    DirectorySelectionField.initialDirectory("missing-dir-name", otherDir) shouldBe Some(new File(".").getCanonicalFile)
  }

  it should "return clean path, without `.` and `..`" in {
    DirectorySelectionField.initialDirectory(new File(tmpDir, "a/../.").getPath, otherDir) shouldBe Some(tmpDir)
    DirectorySelectionField.initialDirectory(new File(tmpDir, "a/../b/./../c").getPath, otherDir) shouldBe Some(tmpDir)
    DirectorySelectionField.initialDirectory("", new File(otherDir, "../" + otherDir.getName)) shouldBe Some(otherDir)
  }

  it should "use the parent when path points to an existing file" in {
    val file = File.createTempFile("DirectorySelectionFieldSpec", ".txt", tmpDir)
    DirectorySelectionField.initialDirectory(file.getPath, otherDir) shouldBe Some(tmpDir)
  }

  it should "use last directory for empty or null path" in {
    DirectorySelectionField.initialDirectory("", otherDir) shouldBe Some(otherDir)
    DirectorySelectionField.initialDirectory(null, otherDir) shouldBe Some(otherDir)
  }

  it should "ignore last directory that is a file" in {
    val file = File.createTempFile("DirectorySelectionFieldSpec", ".txt", tmpDir)
    DirectorySelectionField.initialDirectory("", file) shouldBe None
  }
}
