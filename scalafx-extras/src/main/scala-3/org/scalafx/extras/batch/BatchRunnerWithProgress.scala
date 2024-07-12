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

import org.scalafx.extras.progress_dialog.ProgressStatusDialog
import org.scalafx.extras.{offFX, onFX, onFXAndWait}
import scalafx.geometry.{Insets, Pos}
import scalafx.stage.Window

import java.util.concurrent.atomic.AtomicBoolean
import scala.util.{Failure, Success, Try}

abstract class BatchRunnerWithProgress[T](
  val title: String,
  val parentWindow: Option[Window],
  val useParallelProcessing: Boolean
):

  import BatchRunnerWithProgress.TaskResult

  def run(): Seq[TaskResult[T]] =
    // TODO: handle exceptions

    val itemTasks: Seq[ItemTask[T]] = createTasks()

    processItems(itemTasks)

  private def processItems(items: Seq[ItemTask[T]]): Seq[TaskResult[T]] =

    val abort           = new AtomicBoolean(false)
    val abortingMessage = title + " - processing aborted by user. Waiting to complete..."

    var progressStatus: ProgressStatusDialog = null

    @FunctionalInterface
    def progressUpdate(
      running: Long,
      successful: Long,
      failed: Long,
      canceled: Long,
      executed: Long,
      total: Long,
      isCanceled: Boolean,
      perc: Double,
      message: String
    ): Unit =
      //      val m =
      //        f"R:$running%2d, S:$successful%2d, F:$failed%2d, E:$executed%2d, T:$total%d, C:$canceled%d" +
      //          f"C:$isCanceled, perc:${perc.toInt}%3d, $message"
      //      println(m)

      onFX {
        progressStatus.progress.value = perc / 100d
        progressStatus.statusText.value =
          if abort.get then abortingMessage else s"Processed ${executed.toInt} of $total - $message"
        progressStatus.totalCount.value = f"$total%d"
        progressStatus.processedCount.value = f"$executed%d"
        progressStatus.successfulCount.value = f"$successful%d"
        progressStatus.failedCount.value = f"$failed%d"
        progressStatus.cancelledCount.value = f"$canceled%d"
      }

    try

      val runner = new ParallelBatchRunner(items, progressUpdate, useParallelProcessing)

      // Initialize status updates
      {
        progressStatus = onFXAndWait {
          new ProgressStatusDialog(s"$title - Batch processing progress", parentWindow)
        }
        progressStatus.abortFlag.onChange { (_, _, newValue) =>
          //          println(s"abortFlag changed to $newValue")
          if newValue then
            offFX {
              runner.cancel()
            }
        }

        onFX {
          progressStatus.show()
          progressStatus.progress.value = -0.01
          progressStatus.statusText.value = s"Processed 0 of ${items.length}..."
        }
      }

      // TODO deal with canceled execution
      val results: Seq[(String, Try[Option[T]])] = runner.execute()

      //      println()
      //      println("Summarize processing")
      //      results.foreach {
      //        case (name, Success(r)) => println(s"$name: SUCCESS: $r")
      //        case (name, Failure(e)) => println(s"$name: ERROR  : ${Option(e.getMessage).getOrElse(e.getClass.getName)}")
      //      }

      val counts = CountSummary(
        total = progressStatus.totalCount.value,
        successful = progressStatus.successfulCount.value,
        failed = progressStatus.failedCount.value,
        cancelled = progressStatus.cancelledCount.value
      )

      val errorDetails: Seq[String] =
        results.flatMap {
          case (name, Success(r)) =>
            r match
              case None    => Option(s"$name: Cancelled")
              case Some(_) => None
          case (name, Failure(e)) =>
            Option(s"$name: ERROR: ${Option(e.getMessage).getOrElse(e.getClass.getName)}")
        }

      // Flatten the results, keep task name
      val completedResults: Seq[TaskResult[T]] = results.flatMap { (name, t) =>
        for ov <- t.toOption; v <- ov yield TaskResult(name, v)
      }

      assert(completedResults.length == counts.successful.toInt)

      showFinalSummary(counts, errorDetails, Option(progressStatus.window))
      //
      //      results.foreach {
      //        case Success(r) =>
      //          // TODO implement details
      //          println(r)
      //        case Failure(e) =>
      //          println(s"ERROR  : ${Option(e.getMessage).getOrElse(e.getClass.getName)}")
      //          e.printStackTrace()
      //      }
      completedResults
    catch
      case t: Throwable =>
        t.printStackTrace()
        throw t
    finally
      onFX {
        Option(progressStatus).foreach(_.close())
      }
    end try
  end processItems

  private def showFinalSummary(counts: CountSummary, errorDetails: Seq[String], parentWindow: Option[Window]): Unit =

    import scalafx.Includes.*
    import scalafx.scene.control.Alert.AlertType
    import scalafx.scene.control.{Alert, Label, TextArea}
    import scalafx.scene.layout.{GridPane, Priority}

    // Rename `title` to avoid name clashes
    val dialogTitle = title

    // Pane with count of each outcome type
    val contentPane: GridPane = new GridPane:
      private var rowCount = 0

      def nLabel(s: String): Label =
        new Label(s):
          alignmentInParent = Pos.CenterRight

      def addRow(label: String, value: String): Unit =
        add(Label(label), 0, rowCount)
        add(nLabel(value), 1, rowCount)
        rowCount += 1

      padding = Insets(14, 14, 14, 28)
      hgap = 14
      addRow("Total", counts.total)
      addRow("Successful", counts.successful)
      addRow("Failed", counts.failed)
      addRow("Cancelled", counts.cancelled)

    val noErrors = counts.total == counts.successful

    // Optional pane showing list of errors
    val errorDetailPane =
      if noErrors then
        None
      else
        val label = new Label("Processing errors:")
        val textArea = new TextArea:
          text = errorDetails.mkString("\n")
          editable = false
          wrapText = true
          maxWidth = Double.MaxValue
          maxHeight = Double.MaxValue
          vgrow = Priority.Always
          hgrow = Priority.Always
        val expContent = new GridPane:
          maxWidth = Double.MaxValue
          add(label, 0, 0)
          add(textArea, 0, 1)
        Some(expContent)

    val alertType = if noErrors then AlertType.Information else AlertType.Warning

    // Create and show the dialog
    onFXAndWait {
      new Alert(alertType) {
        initOwner(parentWindow.orNull)
        this.title = dialogTitle
        headerText = "Item processing summary"
        dialogPane().content = contentPane
        parentWindow.foreach { w => dialogPane().stylesheets = w.scene().stylesheets }

        // Set expandable Exception into the dialog pane, if there are errors top report
        errorDetailPane.foreach(p => dialogPane().expandableContent = p)

      }.showAndWait()
    }
  end showFinalSummary

  def createTasks(): Seq[ItemTask[T]]

  private case class CountSummary(total: String, successful: String, failed: String, cancelled: String)
end BatchRunnerWithProgress

object BatchRunnerWithProgress:
  case class TaskResult[T](taskName:String, result:T)
