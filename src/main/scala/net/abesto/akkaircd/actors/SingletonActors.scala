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

import akka.actor.{ActorContext, ActorSelection, Props}
import akka.routing.RoundRobinPool

object SingletonActors {

  def initialize(implicit context: ActorContext): Unit = {
    context.system.actorOf(Props[UserRegistry], Names.userDb)
    context.system.actorOf(Props[TcpListenerActor], Names.tcpListener)
    context.system.actorOf(RoundRobinPool(10).props(Props[MessageHandler]), Names.messageHandler) // scalastyle:ignore magic.number
  }

  def userDB(implicit context: ActorContext): ActorSelection = get(Names.userDb)

  protected def get(name: String)(implicit context: ActorContext): ActorSelection = context.actorSelection(s"/user/$name")

  def messageHandler(implicit context: ActorContext): ActorSelection = get(Names.messageHandler)

  def tcpListener(implicit context: ActorContext): ActorSelection = get(Names.tcpListener)

  protected object Names {
    val userDb = "user-db"
    val messageHandler = "message-handler"
    val tcpListener = "tcp-listener"
  }
}
