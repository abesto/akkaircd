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

package net.abesto.akkaircd.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp
import akka.util.ByteString
import net.abesto.akkaircd.model.messages.{MessageInflator, NumericReply}
import net.abesto.akkaircd.parser.MessageFormatParser

import scala.language.postfixOps
import scala.util.{Failure, Success}

object TcpConnectionHandler {
  object Messages {
    case class Connection(c: ActorRef)
    case class Write(msg: String)
  }
}

class TcpConnectionHandler extends Actor with ActorLogging {

  import Tcp._
  import TcpConnectionHandler._
  import context._

  def receive: Receive = uninitialized

  def uninitialized: Receive = {
    case Messages.Connection(connection) =>
      connection ! Register(self)
      become(initialized(connection))
  }

  def initialized(connection: ActorRef): Receive = {
    case Messages.Write(msg) =>
      connection ! Write(ByteString(msg))

    case Received(data) =>
      assert(connection == sender())
      val string: String = data.decodeString("UTF-8")
      log.debug(s"[in] $string")
      new MessageFormatParser(string).message.run() match {
        case Failure(exception) => log.debug(s"Failed to parse message '$string': $exception")
        case Success(value) => try {
          SingletonActors.messageHandler ! MessageInflator.inflate(value)
        } catch {
          case MessageInflator.UnknownCommand(cmd) => log.debug(s"Unknown command: $cmd")
          // TODO: star here needs to be the nickname, if set
          case num: NumericReply => connection ! Write(ByteString(s":akkairc.localhost ${num.num} * :${num.desc}\r\n"))
          case default: Throwable => log.error(default, "whoooo what's going on")
        }
      }

    case PeerClosed =>
      context stop self
  }
}
