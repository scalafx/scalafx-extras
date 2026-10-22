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

package org.scalafx.extras.batch

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.{Failure, Success}

class ParallelBatchRunnerSpec extends AnyFlatSpec with Matchers {

  private class TestTask[T](val name: String, op: => T) extends ItemTask[T] {
    override def run(): T = op
  }

  private val noProgress: BatchRunner.ProgressUpdater = (_, _, _, _, _, _, _, _, _) => ()

  private def runner[T](tasks: Seq[ItemTask[T]]) =
    new ParallelBatchRunner[T, ItemTask[T]](tasks, noProgress, useParallelProcessing = true)

  "ParallelBatchRunner" should "report results and counts of successful and failed tasks" in {
    val r = runner[Int](Seq(
      new TestTask("one", 1),
      new TestTask("fail", throw new IllegalStateException("expected")),
      new TestTask("three", 3)
    ))

    val results = r.execute()

    results.map(_._1) shouldBe Seq("one", "fail", "three")
    results(0)._2 shouldBe Success(Some(1))
    results(1)._2 shouldBe a[Failure[?]]
    results(2)._2 shouldBe Success(Some(3))

    r.successfulCount shouldBe 2
    r.failedCount shouldBe 1
    r.canceledCount shouldBe 0
    r.executedCount shouldBe 3
    r.runningCount shouldBe 0
  }

  it should "treat a `null` result as success, not as cancellation" in {
    val r = runner[String](Seq(new TestTask[String]("null", null)))

    val results = r.execute()

    results shouldBe Seq(("null", Success(Some(null))))
    r.successfulCount shouldBe 1
    r.canceledCount shouldBe 0
  }

  it should "mark tasks as cancelled after cancel()" in {
    val r = runner[Int](Seq(new TestTask("one", 1), new TestTask("two", 2)))
    r.cancel()

    val results = r.execute()

    results.map(_._2) shouldBe Seq(Success(None), Success(None))
    r.canceledCount shouldBe 2
    r.successfulCount shouldBe 0
  }

  it should "handle empty list of tasks" in {
    val r = runner[Int](Seq.empty)

    r.execute() shouldBe empty
    r.executedCount shouldBe 0
  }
}
