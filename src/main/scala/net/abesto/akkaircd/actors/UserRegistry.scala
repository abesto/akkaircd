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

import akka.actor.{Actor, ActorLogging}
import net.abesto.akkaircd.model.UserRef


object UserRegistry {
  object Messages {

    case class Join(ref: UserRef)

    case class Leave(ref: UserRef)
    object AllUsers
  }
}


class UserRegistry extends Actor with ActorLogging {

  import UserRegistry.Messages._

  var users: Seq[UserRef] = Seq()

  def receive: Receive = {
    case Join(user) =>
      users :+= user
      user.tcp ! TcpConnectionHandler.Messages.Write(s"welcome! we have ${users.length} users.\n")

    case Leave(user) =>
      users = users.filter(_ != user)

    case AllUsers => sender() ! users
  }
}
