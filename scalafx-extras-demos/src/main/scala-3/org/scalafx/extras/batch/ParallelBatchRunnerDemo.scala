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

package org.scalafx.extras.batch

import scala.util.{Failure, Success}


object ParallelBatchRunnerDemo {
  class DemoTaskItem(n: Int) extends ItemTask[Int] {
    val name = s"Demo TaskItem $n"

    def run(): Int = {
      //      println(s"Item ${n} - start")
      Thread.sleep(300)
      if n == 7 then
        //        println(s"Item ${n} - error")
        throw new IllegalArgumentException(s"Don't give me $n")

      //      println(s"Item ${n} - end")
      n
    }
  }


  def main(args: Array[String]): Unit = {
    val items: Seq[DemoTaskItem] = Range(0, 10).map { i => new DemoTaskItem(i) }

    val batchHelper = new ParallelBatchRunner(items, progressUpdate, useParallelProcessing = true)

    val results = batchHelper.execute()

    println()
    println("Summarize processing")
    results.foreach {
      case (name, Success(r)) => println(s"$name: SUCCESS: $r")
      case (name, Failure(e)) => println(s"$name: ERROR  : ${e.getMessage}")
    }
  }

  def progressUpdate(running   : Long,
                     successful: Long,
                     failed    : Long,
                     canceled : Long,
                     executed  : Long,
                     total     : Long,
                     isCanceled  : Boolean,
                     perc      : Double,
                     message   : String) : Unit = {
    val m =
      f"R:$running%2d, S:$successful%2d, F:$failed%2d, E:$executed%2d, C:$canceled, T:$total%d, " +
        f"C:$isCanceled, perc:${perc.toInt}%3d, $message"
    println(m)
  }

}
