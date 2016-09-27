// Copyright (c) 2016, Zoltán Nagy <abesto@abesto.net>
//
// Permission to use, copy, modify, and/or distribute this software for any
// purpose with or without fee is hereby granted, provided that the above
// copyright notice and this permission notice appear in all copies.
//
// THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
// WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
// ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
// WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
// ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
// OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

package net.abesto.akkaircd.model.messages

import scala.collection.mutable

case class NumericReply(num: Int, desc: String) extends Throwable with Message {}

object NumericReplies {
  val byNumber = mutable.HashMap[Int, NumericReply]()

  // scalastyle:off method.name magic.number
  Map[Int, String](
    431 -> "No nickname given"
  ).foreach {
    case (num, desc) => byNumber += ((num, NumericReply(num, desc)))
  }

  /** Returned when a nickname parameter expected for a command and isn't found. */
  val ERR_NONICKNAMEGIVEN: NumericReply = byNumber(431)
  // scalastyle:on method.name magic.number
}
