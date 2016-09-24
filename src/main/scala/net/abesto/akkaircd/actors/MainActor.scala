package net.abesto.akkaircd.actors

import akka.actor.{Actor, Props}

class MainActor extends Actor {

  override def preStart(): Unit = {
    val userDb = context.actorOf(Props[UserDatabase], "userDB")
    val tcpListener = context.actorOf(Props[TcpListenerActor], "tcpListener")

    tcpListener ! TcpListenerMessages.Listen
  }

  def receive = {
    case x => ()
  }
}

