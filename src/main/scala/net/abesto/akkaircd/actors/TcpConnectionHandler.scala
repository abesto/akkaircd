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
import akka.pattern.ask
import akka.util.{ByteString, Timeout}
import net.abesto.akkaircd.actors.UserDatabase.Messages.AllUsers

import scala.concurrent.duration._
import scala.language.postfixOps

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

  protected def uninitialized: Receive = {
    case Messages.Connection(connection) =>
      connection ! Register(self)
      become(initialized(connection))
  }

  protected def initialized(connection: ActorRef): Receive = {
    case Messages.Write(msg) =>
      connection ! Write(ByteString(msg))
    case Received(data) =>
      val string: String = data.decodeString("UTF-8")
      log.info(s"[in] $string")
      assert(connection == sender())
      implicit val timeout = Timeout(1 second)
      for {users <- (userDB ? AllUsers).mapTo[Seq[User]]}
        yield users.foreach { it =>
          log.info(s"broadcasting to $it")
          it.tcp ! Messages.Write(string)
        }
    case PeerClosed =>
      context stop self
  }

  protected def userDB = context.actorSelection("akka://Main/user/app/userDB")
}
