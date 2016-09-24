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

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Tcp}
import net.abesto.akkaircd.Settings
import net.abesto.akkaircd.actors.UserDatabase.Messages.Join

object TcpListenerMessages {

  case class UserDB(db: ActorRef)

  object Listen

}

class TcpListenerActor extends Actor with ActorLogging {

  import Tcp._
  import context.system

  val settings = Settings(system)

  def receive: Receive = {
    case TcpListenerMessages.Listen =>
      IO(Tcp) ! Bind(self, new InetSocketAddress(settings.Tcp.Host, settings.Tcp.Port))

    case b@Bound(localAddress) =>
      log.info(s"Listening on $localAddress")

    case CommandFailed(_: Bind) => context stop self

    case c@Connected(remote, local) =>
      log.info(s"Received connection $remote $local")
      val handler = context.actorOf(Props[TcpConnectionHandler])
      handler ! TcpConnectionHandler.Messages.Connection(sender())
      userDB ! Join(User(handler))
  }

  protected def userDB = context.actorSelection("akka://Main/user/app/userDB")
}


