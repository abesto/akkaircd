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

/**
  * rfc2812 - 3.1.2 Nick message
  * *
  * Command: NICK
  * Parameters: <nickname>
  * *
  * NICK command is used to give user a nickname or change the existing
  * one.
  * *
  * Numeric Replies:
  * *
  * ERR_NONICKNAMEGIVEN             ERR_ERRONEUSNICKNAME
  * ERR_NICKNAMEINUSE               ERR_NICKCOLLISION
  * ERR_UNAVAILRESOURCE             ERR_RESTRICTED
  * *
  * Examples:
  * *
  * NICK Wiz                ; Introducing new nick "Wiz" if session is
  * still unregistered, or user changing his
  * nickname to "Wiz"
  * *
  * :WiZ!jto@tolsun.oulu.fi NICK Kilroy
  * ; Server telling that WiZ changed his
  * nickname to Kilroy.
  */
case class NickCommand(raw: RawMessage) extends Message {
  if (raw.params.isEmpty) {
    throw NumericReplies.ERR_NONICKNAMEGIVEN
  }

  val nick = raw.params.head
}
