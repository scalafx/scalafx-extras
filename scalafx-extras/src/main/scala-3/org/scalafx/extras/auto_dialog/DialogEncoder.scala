/*
 * Copyright (c) 2011-2022, ScalaFX Project
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

package org.scalafx.extras.auto_dialog


import org.scalafx.extras.generic_dialog.GenericDialogFX

import scala.collection.mutable
import scala.compiletime.{constValueTuple, summonAll}
import scala.deriving.Mirror

trait DialogEncoder[T]:
  /**
    * Adds editor for `value` to the `dialog`.
    *
    * @param dialog to which edit control will be added
    * @param value  initial value that will be edited
    */
  def addEditor(dialog: GenericDialogFX, label: String, value: T): Unit

object DialogEncoder:

  private case class LabeledBuilder(label: String, builder: DialogEncoder[Any])

  given DialogEncoder[Boolean] with
    override def addEditor(dialog: GenericDialogFX, label: String, value: Boolean): Unit =
      dialog.addCheckbox(label, value)

  given DialogEncoder[Int] with
    override def addEditor(dialog: GenericDialogFX, label: String, value: Int): Unit =
      dialog.addNumericField(label, value, 0, 8, "")

  given DialogEncoder[Double] with
    override def addEditor(dialog: GenericDialogFX, label: String, value: Double): Unit =
      dialog.addNumericField(label, value, 4, 8, "")

  given DialogEncoder[String] with
    override def addEditor(dialog: GenericDialogFX, label: String, value: String): Unit =
      dialog.addStringField(label, value, math.max(value.length, 8))

  inline given[A <: Product](using m: Mirror.ProductOf[A]): DialogEncoder[A] =
    new DialogEncoder[A] :
      type ElemGenericBuilder = Tuple.Map[m.MirroredElemTypes, DialogEncoder]
      private val elemGenericBuilders: List[DialogEncoder[Any]] =
        summonAll[ElemGenericBuilder].toList.asInstanceOf[List[DialogEncoder[Any]]]

      private val labels: List[String] =
        constValueTuple[m.MirroredElemLabels]
          .toList
          .asInstanceOf[List[String]]
          .map(fromCamelCase)

      println(s"Labels: " + labels.mkString(", "))

      assert(labels.length == elemGenericBuilders.length)

      private val labeledBuilders: Seq[LabeledBuilder] =
        labels.zip(elemGenericBuilders).map(v => LabeledBuilder(v._1, v._2))

      def addEditor(dialog: GenericDialogFX, label: String, a: A): Unit =
        // TODO use `label` as section header, if not empty
        val elems: Seq[Any] = a.productIterator.toList
        assert(elems.length == labeledBuilders.length)
        elems
          .zip(labeledBuilders)
          .foreach { (elem, lb) => lb.builder.addEditor(dialog, lb.label, elem) }

  def fromCamelCase(s: String): String =
    val sb = mutable.StringBuilder()
    val length = s.length
    var currIndex = 0
    var prevWasLower = false
    var wasBoundary = false
    while currIndex < length do
      val curr = s.charAt(currIndex)
      val nextIndex = currIndex + 1
      val nextIsLower = (nextIndex < length) && s.charAt(nextIndex).isLower
      val isBoundary = curr.isUpper && (prevWasLower || nextIsLower)
      val isDelimitation = isBoundary && !wasBoundary
      val out =
        if currIndex == 0 then curr.toUpper
        else if isDelimitation then ' '
        else if isBoundary && nextIsLower then curr.toLower
        else curr
      sb.append(out)
      prevWasLower = curr.isLower
      wasBoundary = isBoundary
      if !isDelimitation then currIndex = nextIndex
    end while
    sb.toString
  end fromCamelCase
