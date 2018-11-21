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

package org.scalafx.extras

import java.util.concurrent.Future

import javafx.{scene => jfxs}
import org.scalafx.extras.BusyWorker.SimpleTask
import scalafx.Includes._
import scalafx.application.Platform
import scalafx.beans.property._
import scalafx.concurrent.Worker
import scalafx.scene.{Cursor, Node}
import scalafx.stage.Window

import scala.language.implicitConversions
import scala.runtime.NonLocalReturnControl

object BusyWorker {

  implicit def apply(nodes: Seq[Node]): Seq[jfxs.Node] = nodes.map(_.delegate)

  /**
    * A simple wrapper for a task that mas a status message property and a progress property.
    * Intended for use with [[org.scalafx.extras.BusyWorker#doTask(java.lang.String, org.scalafx.extras.BusyWorker.SimpleTask) BusyWorker#doTask]] method
    *
    * @tparam R returned value type.
    */
  trait SimpleTask[R] {

    /**
      * Message that can be updated while task is executed.
      */
    val message: StringProperty = new StringProperty(this, "message", "")
    /**
      * Progress indicator that can be updated when task is executed.
      */
    val progress: DoubleProperty = new DoubleProperty(this, "progress", 0)

    /**
      * Perform the main actions of this task.
      */
    def call(): R

    /**
      * Perform some actions after after `call()` completed.
      * This is executed regardless of success or failure of `call()`.
      * Use this to prevent blocking while waiting for `call()` to finish.
      * The default implementation does nothing.
      *
      * @param result     a future containing result returned by `call()`.
      *                   The result can be obtained using `result.get()`.
      *                   Only valid if `call()` completed successfully.
      * @param successful will be `true` if call completed successfully (without exceptions and was not cancelled).
      */
    def onFinish(result: Future[R], successful: Boolean): Unit = {}
  }

}

/**
  * BusyWorker helps running UI tasks a separate threads (other than the JavaFX Application thread).
  * It will show busy cursor and disable specified nodes while task is performed.
  * It gives an option to show progress and status messages.
  * `BusyWorker` run tasks and takes care of handling handling exceptions and displaying error dialogs.
  * There is also option to perform custom finish actions after task is completed.
  *
  * While task is performed property `busy` is set to true.
  * Only one task, for a given worker, can be run at the time.
  * When a task in being performed `busyDisabledNode` will be disabled and its cursor will be set to `Wait`/`Busy` cursor.
  *
  * Progress and messages from the running task can be monitored using `progressValue` and `progressMessage` properties.
  *
  * Below is an example of using using BusyWorker that updates a progress message and progress indicator.
  * The full example can be found in the `BusyWorkerDemo` of the ScalaFX Extras Demo project.
  * {{{
  *   val buttonPane: Pane = ...
  *   val progressLabel: Label = ...
  *   val progressBar: ProgressBar = ...
  *
  *   val busyWorker = new BusyWorker("BusyWorker Demo", buttonPane) {
  *     progressLabel.text <== progressMessage
  *     progressBar.progress <== progressValue
  *   }
  *
  *   val button = new Button("Click Me") {
  *         onAction = () => busyWorker.doTask("Task 1")(
  *           new SimpleTask[String] {
  *             override def call(): String = {
  *               val maxItems = 10
  *               for (i <- 1 to maxItems) {
  *                 println(i)
  *                 message() = s"Processing item $i/$maxItems"
  *                 progress() = (i - 1) / 10.0
  *                 Thread.sleep(250)
  *               }
  *               progress() = 1
  *               "Done"
  *             }
  *           }
  *         )
  *   }
  * }}}
  *
  * @author Jarek Sacha
  */
class BusyWorker private(val title: String,
                         private var _parentWindow: Option[Window] = None,
                         private var _disabledNodes: Seq[jfxs.Node] = Seq.empty[jfxs.Node]) extends ShowMessage {

  /**
    * Creates a busy worker with a title and nodes to disable when performing tasks.
    * The root node of the parentWindow will be disabled when task is being executed.
    *
    * The input is a collection of JavaFX or ScalaFX nodes.
    * {{{
    *   val parent: Window = ...
    *   val busyWorker = new BusyWorker("My Task", parent))
    * }}}
    *
    * @param title  title used for unexpected error dialogs.
    * @param parent window that will be used to display dialogs (if any).
    */
  def this(title: String, parent: Window) =
    this(title, _parentWindow = Option(parent), _disabledNodes = Seq.empty[jfxs.Node])

  /**
    * Creates a busy worker with a title and nodes to disable when performing tasks.
    * The root node of the parentWindow will be disabled when task is being executed.
    *
    * The input is a collection of JavaFX or ScalaFX nodes.
    * {{{
    *   val parent: Option[Window] = ...
    *   val busyWorker = new BusyWorker("My Task", parent))
    * }}}
    *
    * @param title  title used for unexpected error dialogs.
    * @param parent window that will be used to display dialogs (if any).
    */
  def this(title: String, parent: Option[Window]) =
    this(title, _parentWindow = parent, _disabledNodes = Seq.empty[jfxs.Node])

  /**
    * Creates a busy worker with a title and nodes to disable when performing tasks.
    * The parent window is the parent window of the node.
    *
    * The input is a collection of JavaFX or ScalaFX nodes.
    * {{{
    *   val node: scalafx.scene.Node] = ...
    *   val busyWorker = new BusyWorker("My Task", node))
    * }}}
    *
    * @param title        title used for unexpected error dialogs.
    * @param disabledNode node that will be disabled when performing a task, cannot be null.
    */
  def this(title: String, disabledNode: Node) =
    this(title, _parentWindow = None, _disabledNodes = Seq(disabledNode))

  /**
    * Creates a busy worker with a title and nodes to disable when performing tasks.
    * The parent window is the parent window of the first node.
    *
    * The input is a collection of JavaFX or ScalaFX nodes.
    * {{{
    *   val nodes = Seq[scalafx.scene.Node] = ...
    *   val busyWorker = new BusyWorker("My Task", nodes))
    * }}}
    *
    * @param title         title used for unexpected error dialogs.
    * @param disabledNodes nodes that will be disabled when performing a task,
    *                      if not specified it will be set to root pane of the `parentWindow`.
    */
  def this(title: String, disabledNodes: Seq[jfxs.Node]) =
    this(title, _parentWindow = None, _disabledNodes = disabledNodes)

  def disabledNodes: Seq[jfxs.Node] = _disabledNodes
  def disabledNodes_=(implicit v: Seq[jfxs.Node]): Unit = _disabledNodes = v

  override def parentWindow: Option[Window] = _parentWindow match {
    case Some(_) => _parentWindow
    case None =>
      if (disabledNodes.nonEmpty) {
        disabledNodes.map {
          n =>
            val w: Window = n.scene().window()
            w
        }.headOption
      } else {
        None
      }
  }

  def parentWindow_=(v: Option[Window]): Unit = {
    _parentWindow = v
  }
  def parentWindow_=(v: Window): Unit = {
    _parentWindow = Option(v)
  }

  private val _progressValue = new ReadOnlyDoubleWrapper(this, "progressValue", 0)
  private val _progressMessage = new ReadOnlyStringWrapper(this, "progressMessage", "")
  private var _busyWorkloadName = "[NONE]"

  /**
    * `busy` property is `true` when worker is performing a task. Only one task can be done at a time.
    */
  final val busy: BooleanProperty = BooleanProperty(false)
  /**
    * Progress indicator of a running task, if any, value are between [0 and 1].
    * Current running task's `progress` property is bound to this property (only when task is running).
    */
  final val progressValue: ReadOnlyDoubleProperty = _progressValue.readOnlyProperty
  /**
    * Progress message posted by running task, if any.
    * Current running task's `message` property is bound to this property (only when task is running).
    */
  final val progressMessage: ReadOnlyStringProperty = _progressMessage.readOnlyProperty

  /**
    * Run a `task` on a separate thread. Returns immediately (before `task` is completed).
    * If the task returns a value is can be retrieved through the returned `Future`.
    *
    * Example of running a task without waiting to complete, using a lambda
    * {{{
    *   worker.doTask{ () =>
    *      // Some workload code, does not produce value ot it is discard
    *      Thread.sleep(1000)
    *      print(1 + 1)
    *   }
    * }}}
    *
    * Example of stating a task (with a lambda) and waiting till it finishes and returns a result
    * {{{
    *   // This code will return before workload is completed
    *   val future = worker.doTask{ () =>
    *      // Some workload code producing final value
    *      Thread.sleep(1000)
    *      1 + 1
    *   }
    *   // This will block till workload competes and the result is retrieved
    *   val result = future.get()
    *   print(result)
    * }}}
    *
    * Example of running task that updates `progress` and `message`, for more details see `BusyWorkerDemo`.
    * {{{
    *   busyWorker.doTask(
    *            new SimpleTask[String] {
    *              override def call(): String = {
    *                val maxItems = 10
    *                for (i <- 1 to maxItems) {
    *                  println(i)
    *                  message() = s"Processing item $i/$maxItems"
    *                  progress() = (i - 1) / 10.0
    *                  Thread.sleep(250)
    *                }
    *                progress() = 1
    *                "Done"
    *              }
    *            }
    *          )
    * }}}
    *
    * @param task actions to perform, can be provided a as a lambda op: => R, see examples above.
    * @return `Future` that can be used to retrieve result produced the workload, if any.
    */
  def doTask[R](task: SimpleTask[R]): Future[R] = {
    doTask(title)(task)
  }


  /**
    * Run a `task` on a separate thread. Returns immediately (before `task` is completed).
    * If the task returns a value is can be retrieved through the returned `Future`.
    *
    * Example of running a task without waiting to complete, using a lambda
    * {{{
    *   worker.doTask("My Task") { () =>
    *      // Some workload code, does not produce value ot it is discard
    *      Thread.sleep(1000)
    *      print(1 + 1)
    *   }
    * }}}
    *
    * Example of stating a task (with a lambda) and waiting till it finishes and returns a result
    * {{{
    *   // This code will return before workload is completed
    *   val future = worker.doTask("My Task") { () =>
    *      // Some workload code producing final value
    *      Thread.sleep(1000)
    *      1 + 1
    *   }
    *   // This will block till workload competes and the result is retrieved
    *   val result = future.get()
    *   print(result)
    * }}}
    *
    * Example of running task that updates `progress` and `message`, for more details see `BusyWorkerDemo`.
    * {{{
    *   busyWorker.doTask("Task 1")(
    *            new SimpleTask[String] {
    *              override def call(): String = {
    *                val maxItems = 10
    *                for (i <- 1 to maxItems) {
    *                  println(i)
    *                  message() = s"Processing item $i/$maxItems"
    *                  progress() = (i - 1) / 10.0
    *                  Thread.sleep(250)
    *                }
    *                progress() = 1
    *                "Done"
    *              }
    *            }
    *          )
    * }}}
    *
    * @param name name used for thread that runs the task. May be useful in debugging.
    * @param task actions to perform, can be provided a as a lambda op: => R, see examples above.
    * @return `Future` that can be used to retrieve result produced the workload, if any.
    */
  def doTask[R](name: String)(task: SimpleTask[R]): Future[R] = {
    val jfxTask = new javafx.concurrent.Task[R] {
      override def call(): R = {
        task.call()
      }

      task.message.onChange((_, _, newValue) => updateMessage(newValue))
      task.progress.onChange((_, _, newValue) => updateProgress(newValue.doubleValue(), 1.0))
    }
    _doTask(jfxTask, task.onFinish, name)
  }

  /**
    *
    * @param task    task to run
    * @param cleanup operation to perform after task completed (success or failure)
    * @param name    name of the thread on which to run the task/
    * @tparam R type of the task return value
    * @return future representing value returned by the task.
    */
  private def _doTask[R](task: javafx.concurrent.Task[R],
                         cleanup: (Future[R], Boolean) => Unit,
                         name: String = title): Future[R] = {

    if (busy()) {
      throw new IllegalStateException("Internal error: Cannot run two workload at the same time. " +
        s"Running workload name = '${_busyWorkloadName}', new request name = '$name'.")
    }

    onFX {
      busy() = true
    }
    _busyWorkloadName = name

    def resetProgress(): Unit = {
      Platform.runLater {
        _progressValue.unbind()
        _progressMessage.unbind()
        onFX {
          _progressValue() = 0
          _progressMessage() = ""
          nodesToDisable.foreach { node =>
            node.disable = false
            // Use Option to guard against null values
            Option(node.scene()).map(_.root()).foreach(_.cursor = Cursor.Default)
          }
          busy() = false
          _busyWorkloadName = "[None]"
        }
      }

      cleanup(task, task.state.value == Worker.State.Succeeded.delegate)
    }

    // Prepare task for execution
    onFX {
      _progressMessage <== task.messageProperty()
      _progressValue <== task.progressProperty()
      nodesToDisable.foreach { node =>
        node.disable = true
        // Use Option to guard against null values
        Option(node.scene()).map(_.root()).foreach(_.cursor = Cursor.Wait)
      }
    }

    task.onSucceeded = () => resetProgress()
    task.onCancelled = () => resetProgress()
    task.onFailed = () => {
      task.getException match {
        case _: NonLocalReturnControl[_] =>
        // `NonLocalReturnControl` seems to be thrown by Scala in control statement context rather than error.
        case t: Throwable =>
          val message = s"Unexpected error while performing a UI task: '$name'. " // + Option(t.getMessage).getOrElse("")
          showException(title, message, t)
      }
      resetProgress()
    }

    // Run task on a separate thread
    val th = new Thread(task, name)
    th.setDaemon(true)
    th.start()

    task
  }

  private def nodesToDisable: Seq[jfxs.Node] = if (disabledNodes.nonEmpty) disabledNodes else {
    parentWindow.map(_.scene()).map(_.root()).toSeq
  }
}
