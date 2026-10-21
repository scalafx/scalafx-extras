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

class FileSelectionFieldSpec extends AnyFlatSpec with Matchers {

  private val tmpDir   = Files.createTempDirectory("FileSelectionFieldSpec").toFile.getCanonicalFile
  private val otherDir = Files.createTempDirectory("FileSelectionFieldSpec-other").toFile.getCanonicalFile

  "FileSelectionField.initialLocation" should "not fail for a bare file name without parent" in {
    val (dir, name) = FileSelectionField.initialLocation("data.txt", otherDir)
    name shouldBe Some("data.txt")
    // Bare name is resolved against the current directory
    dir shouldBe Some(new File(".").getCanonicalFile)
  }

  it should "return clean path, without `.` and `..`" in {
    val messy = new File(tmpDir, "a/.././image.png")
    FileSelectionField.initialLocation(messy.getPath, otherDir) shouldBe (Some(tmpDir), Some("image.png"))
    FileSelectionField.initialLocation("", new File(otherDir, "../" + otherDir.getName)) shouldBe (Some(otherDir), None)
  }

  it should "use parent of an existing path" in {
    val file = new File(tmpDir, "image.png")
    FileSelectionField.initialLocation(file.getPath, otherDir) shouldBe (Some(tmpDir), Some("image.png"))
  }

  it should "use the directory itself when path is a directory" in {
    FileSelectionField.initialLocation(tmpDir.getPath, otherDir) shouldBe (Some(tmpDir), None)
  }

  it should "use first existing parent when parent of path does not exist" in {
    val file = new File(tmpDir, "missing/deeper/image.png")
    FileSelectionField.initialLocation(file.getPath, otherDir) shouldBe (Some(tmpDir), Some("image.png"))
  }

  it should "use last directory for empty or null path" in {
    FileSelectionField.initialLocation("", otherDir) shouldBe (Some(otherDir), None)
    FileSelectionField.initialLocation("  ", otherDir) shouldBe (Some(otherDir), None)
    FileSelectionField.initialLocation(null, otherDir) shouldBe (Some(otherDir), None)
  }

  it should "ignore last directory that is not a directory" in {
    val file = File.createTempFile("FileSelectionFieldSpec", ".txt", tmpDir)
    FileSelectionField.initialLocation("", file) shouldBe (None, None)
    FileSelectionField.initialLocation("", new File(tmpDir, "missing")) shouldBe (None, None)
  }
}
