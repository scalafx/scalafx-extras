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

import java.util
import java.util.concurrent.atomic.{AtomicBoolean, AtomicLong, AtomicReference}
import java.util.concurrent.{Callable, Executors, ThreadPoolExecutor, Future as JFuture}
import scala.jdk.FutureConverters.*
import scala.util.{Failure, Success, Try}

class ParallelBatchRunner[T, I <: ItemTask[T]](
  protected val itemTasks: Seq[I],
  protected val progressUpdater: BatchRunner.ProgressUpdater,
  protected val useParallelProcessing: Boolean
) extends BatchRunner[T, I]:

  // TODO: should it be just `BatchRunner` since parallel execution is an optional?

  private val executor =
    val processors: Int = Runtime.getRuntime.availableProcessors
    val processorsToUse = if useParallelProcessing then Math.max(1, processors - 1) else 1
    //    println(s"Using $processorsToUse processors")
    Executors.newFixedThreadPool(processorsToUse).asInstanceOf[ThreadPoolExecutor]

  private val total = itemTasks.length

  private enum TaskState:
    case NotStarted, Running, Succeeded, Failed, Cancelled

  private class TaskHelper(val itemTask: I) extends Callable[Try[Option[T]]]:

    // TODO: redesign return type to be a 3-state `Try`:
    //  `Success`, `Failure`, `Cancelled` or `Result`, `Error`, `Cancelled`

    private val _state = new AtomicReference[TaskState](TaskState.NotStarted)

    private def state: TaskState = _state.get()

    private def state_=(v: TaskState): Unit = _state.set(v)

    /**
     * @return `Success` when ended without exception or `Failure` with the exception.
     *         The value wrapped by `Success` will be the result computed by the task.
     *         If the task is able to complete and compute a result, it will return a computed value (non-empty Option).
     *         If the task is canceled, it will return `None` (`Success(None)`)
     *         If the task failed, it will return `Failure`.
     */
    override def call(): Try[Option[T]] =
      if isCanceled then
        //        println(s"Task Cancelled: ${itemTask.name}")
        state = TaskState.Cancelled
        incrementCancelled()

        // Mark cancellation with `None`
        Success(None)
      else
        try
          state = TaskState.Running
          incrementRunning()

          val result = itemTask.run()

          state = TaskState.Succeeded
          incrementSucceeded()

          Success(Option(result))
        catch
          case t: Throwable =>
            state = TaskState.Failed
            incrementFailed()

            Failure(t)
  end TaskHelper

  private val _runningCount    = new AtomicLong(0)
  private val _successfulCount = new AtomicLong(0)
  private val _failedCount     = new AtomicLong(0)
  private val _executedCount   = new AtomicLong(0)
  private val _canceledCount   = new AtomicLong(0)
  private val _cancelFlag      = new AtomicBoolean(false)

  private def incrementRunning(): Unit =
    _runningCount.incrementAndGet()
    updateState()

  private def incrementSucceeded(): Unit =
    _runningCount.decrementAndGet()
    _successfulCount.incrementAndGet()
    _executedCount.incrementAndGet()
    updateState()

  private def incrementCancelled(): Unit =
    _canceledCount.incrementAndGet()
    _executedCount.incrementAndGet()
    updateState()

  private def incrementFailed(): Unit =
    _runningCount.decrementAndGet()
    _failedCount.incrementAndGet()
    _executedCount.incrementAndGet()
    updateState()

  private def updateState(): Unit =

    // TODO: Update state on a separate thread to avoid blocking by the callback `progressUpdate` of current thread
    // TODO: `perc` and `message` are simple derived values, do we need pass them explicitly?

    val perc = _executedCount.get().toDouble / total.toDouble * 100
    progressUpdater.update(
      running = runningCount,
      successful = successfulCount,
      failed = failedCount,
      executed = executedCount,
      canceled = canceledCount,
      total = total,
      isCanceled = isCanceled,
      perc = perc,
      message = s"${_executedCount.get()}/$total"
    )

  def runningCount: Long = _runningCount.get()

  def successfulCount: Long = _successfulCount.get()

  def failedCount: Long = _failedCount.get()

  def canceledCount: Long = _canceledCount.get()

  def executedCount: Long = _executedCount.get()

  def isCanceled: Boolean = _cancelFlag.get()

  /**
   * Cancel execution.
   */
  def cancel(): Unit =
    // Prevent new tasks from tanning computations
    _cancelFlag.set(true)

    // TODO: set cancel flag for each itemTask
    //    itemTasks.foreach(i => i.name)
    //    updateState()

  /**
   * @return results returned by each task or error.
   *         The first part of a tuple is task name, the second part is the result of processing.
   */
  def execute(): Seq[(String, Try[Option[T]])] =
    val batchTasks: Seq[TaskHelper] = itemTasks.map(item => new TaskHelper(item))

    //      val futures = batchTasks.map { t => executor.submit(t).asInstanceOf[JFuture[T]] }

    // Submit tasks
    val namedFutures: Seq[(String, JFuture[Try[Option[T]]])] =
      batchTasks.map(t => (t.itemTask.name, executor.submit(t)))
    //    _futures = Option(futures)

    // Mark for executor shutdown when all tasks finished
    executor.shutdown()

    while executor.getActiveCount > 0 do
      Thread.sleep(100)

    //    println(s"Executor getTaskCount         : ${executor.getTaskCount}")
    //    println(s"Executor getActiveCount       : ${executor.getActiveCount}")
    //    println(s"Executor getCompletedTaskCount: ${executor.getCompletedTaskCount}")

    val result = namedFutures.map((name, f) => (name, Try(f.get()).flatten))
    //    println("Completed waiting for the futures")
    result
end ParallelBatchRunner
