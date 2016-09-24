package net.abesto.akkaircd.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp
import akka.pattern.ask
import akka.util.{ByteString, Timeout}
import net.abesto.akkaircd.actors.UserDatabase.Messages.AllUsers

import scala.concurrent.duration._

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

  def receive = uninitialized

  def uninitialized: Receive = {
    case Messages.Connection(connection) =>
      connection ! Register(self)
      become(initialized(connection))
  }

  def initialized(connection: ActorRef): Receive = {
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

  def userDB = context.actorSelection("akka://Main/user/app/userDB")
}
