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

import net.abesto.akkaircd.model.RawMessage

object MessageInflator {

  protected val byString = Map[String, (RawMessage) => Message](
    "NICK" -> (new NickCommand(_))
  )

  def inflate(raw: RawMessage): Message = raw.command match {
    case Left(s) => byString.get(s).map(_ (raw)).getOrElse(throw UnknownCommand(s))
    case Right(n) => NumericReplies.byNumber(n)
  }

  case class UnknownCommand(cmd: String) extends Throwable {}
}
