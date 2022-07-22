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

import scala.compiletime.{constValueTuple, summonAll}
import scala.deriving.Mirror

/** Decode current fields from a `GenericDialog` corresponding to type `T` */
trait DialogDecoder[T]:
  def decode(dialog: GenericDialogFX): T

object DialogDecoder:

  /**
    * Decode numeric field as integer
    */
  given DialogDecoder[Boolean] with
    override def decode(dialog: GenericDialogFX): Boolean =
      dialog.nextBoolean()

  /**
    * Decode numeric field as integer
    */
  given DialogDecoder[Int] with
    override def decode(dialog: GenericDialogFX): Int =
      math.round(dialog.nextNumber()).toInt

  /**
    * Decode numeric field as double
    */
  given DialogDecoder[Double] with
    override def decode(dialog: GenericDialogFX): Double =
      dialog.nextNumber()

  given DialogDecoder[String] with
    override def decode(dialog: GenericDialogFX): String =
      dialog.nextString()

  /**
    * Decode set of fields corresponding to member of a case class
    */
  inline given[A <: Product](using m: Mirror.ProductOf[A]): DialogDecoder[A] =
    new DialogDecoder[A] :
      type ElemDecoder = Tuple.Map[m.MirroredElemTypes, DialogDecoder]
      private val elemDecoders: List[DialogDecoder[Any]] =
        summonAll[ElemDecoder].toList.asInstanceOf[List[DialogDecoder[Any]]]

      def decode(dialog: GenericDialogFX): A =
        val decoded = elemDecoders.map(_.decode(dialog))
        val tuple = decoded.foldRight[Tuple](EmptyTuple)(_ *: _)
        m.fromProduct(tuple)
