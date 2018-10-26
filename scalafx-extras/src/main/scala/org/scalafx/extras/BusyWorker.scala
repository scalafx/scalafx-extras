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
import scalafx.scene.{Cursor, Node}
import scalafx.stage.Window

import scala.language.implicitConversions
import scala.runtime.NonLocalReturnControl

object BusyWorker {

  //noinspection ConvertExpressionToSAM
  implicit def apply[R](op: => R): SimpleTask[R] = new SimpleTask[R] {
    def call(): R = op
  }

  implicit def apply(nodes: Seq[Node]): Seq[jfxs.Node] = nodes.map(_.delegate)

  /**
    * A simple wrapper for a task that mas a status message property and a progress property.
    * Intended for use with [[org.scalafx.extras.BusyWorker#doTask(java.lang.String, org.scalafx.extras.BusyWorker.SimpleTask) BusyWorker#doTask]] method
    *
    * @tparam R returned value type.
    */
  trait SimpleTask[R] {

    val message: StringProperty = new StringProperty(this, "message", "")
    val progress: DoubleProperty = new DoubleProperty(this, "progress", 0)

    def call(): R
  }

}

/**
  * Runs a workload on a separate thread. While workload is performed property `busy` is set to true.
  * Only one workload can be run at the time. `workload` needs to handle its own exceptions.
  * When a task in being performed `busyDisabledNode` will be disabled and its cursor will be set to `Wait`/`Busy` cursor.
  *
  * Progress and messages from the running task can be monitored using `progressValue` and `progressMessage` properties.
  *
  * Below is an example of using using BusyWorker that updates a progress message and progress indicator.
  * The full example can be found in the `BusyWorkerDemo` of the ScalaFX Extras Demo project.
  * {{{
  *
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
  * }}}
  *
  *
  * {{{
  *
  * }}}
  *
  * @param parent            parent window used for unexpected error dialogs.
  * @param title             title used for unexpected error dialogs.
  * @param busyDisabledNodes nodes that will be disabled when performing a task,
  *                          if not specified it will be set to root pane of the `parentWindow`.
  * @author Jarek Sacha
  */
class BusyWorker private(val title: String,
                         val parent: Option[Window] = None,
                         val busyDisabledNodes: Seq[jfxs.Node] = Seq[jfxs.Node]()) extends ShowMessage {

  def this(title: String, parent: Window) = this(title, parent = Option(parent), busyDisabledNodes = Seq.empty[jfxs.Node])

  def this(title: String, parent: Option[Window]) = this(title, parent = parent, busyDisabledNodes = Seq.empty[jfxs.Node])

  def this(title: String, busyDisabledNode: Node) = this(title, parent = None, busyDisabledNodes = Seq(busyDisabledNode))

  /** Creates a busy worker with a title and nodes to disable when performing tasks.
    *
    * The input is a collection of JavaFX nodes.
    * If you have a sequence of ScalaFX nodes, you may need to map them to underlying JavaFX nodes (delegates).
    * {{{
    *   val nodes = Seq[scalafx.scene.Node] = ...
    *   new BusyWorker("My Task", nodes.map(_.delegate))
    * }}}
    *
    * @param title             title used for unexpected error dialogs.
    * @param busyDisabledNodes nodes that will be disabled when performing a task,
    *                          if not specified it will be set to root pane of the `parentWindow`.
    */
  def this(title: String, busyDisabledNodes: Seq[jfxs.Node]) = this(title, parent = None, busyDisabledNodes = busyDisabledNodes)
  //  def this(title: String, busyDisabledNodes: Seq[Node]) = this(title, parent = None, busyDisabledNodes = busyDisabledNodes.map(_.delegate))

  override def parentWindow: Option[Window] = parent match {
    case Some(_) => parent
    case None =>
      if (busyDisabledNodes.nonEmpty) {
        busyDisabledNodes.map {
          n =>
            val w: Window = n.scene().window()
            w
        }.headOption
      } else {
        None
      }
  }

  //  require(nodeToDisable.nonEmpty,
  //    s"Cannot determine value for `nodeToDisable`. busyDisabledNode=$busyDisabledNodes, parentWindow=$parentWindow.")

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
    *   worker.doTask{
    *      // Some workload code, does not produce value ot it is discard
    *      Thread.sleep(1000)
    *      print(1 + 1)
    *   }
    * }}}
    *
    * Example of stating a task (with a lambda) and waiting till it finishes and returns a result
    * {{{
    *   // This code will return before workload is completed
    *   val future = worker.doTask{
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
  def doTask[R](implicit task: SimpleTask[R]): Future[R] = {
    val t = new javafx.concurrent.Task[R] {
      override def call(): R = {
        task.call()
      }

      task.message.onChange((_, _, newValue) => updateMessage(newValue))
      task.progress.onChange((_, _, newValue) => updateProgress(newValue.doubleValue(), 1.0))
    }
    _doTask(t, title)
  }


  /**
    * Run a `task` on a separate thread. Returns immediately (before `task` is completed).
    * If the task returns a value is can be retrieved through the returned `Future`.
    *
    * Example of running a task without waiting to complete, using a lambda
    * {{{
    *   worker.doTask("My Task") {
    *      // Some workload code, does not produce value ot it is discard
    *      Thread.sleep(1000)
    *      print(1 + 1)
    *   }
    * }}}
    *
    * Example of stating a task (with a lambda) and waiting till it finishes and returns a result
    * {{{
    *   // This code will return before workload is completed
    *   val future = worker.doTask("My Task") {
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
  def doTask[R](name: String)(implicit task: SimpleTask[R]): Future[R] = {
    val t = new javafx.concurrent.Task[R] {
      override def call(): R = {
        task.call()
      }

      task.message.onChange((_, _, newValue) => updateMessage(newValue))
      task.progress.onChange((_, _, newValue) => updateProgress(newValue.doubleValue(), 1.0))
    }
    _doTask(t, name)
  }

  private def _doTask[R](task: javafx.concurrent.Task[R], name: String = title): Future[R] = {

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
          nodeToDisable.foreach { node =>
            node.disable = false
            node.scene().root().cursor = Cursor.Default
          }
          busy() = false
          _busyWorkloadName = "[None]"
        }
      }
    }

    onFX {
      _progressMessage <== task.messageProperty()
      _progressValue <== task.progressProperty()
      nodeToDisable.foreach { node =>
        node.disable = true
        node.scene().root().cursor = Cursor.Wait
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

    val th = new Thread(task, name)
    th.setDaemon(true)
    th.start()

    task
  }

  private def nodeToDisable: Seq[jfxs.Node] = if (busyDisabledNodes.nonEmpty) busyDisabledNodes else {
    parentWindow.map(_.scene().root()).toSeq
  }
}
