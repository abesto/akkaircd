package net.abesto.akkaircd.actors

import scala.concurrent.duration._
import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.ask
import akka.io.Tcp
import akka.util.{ByteString, Timeout}
import net.abesto.akkaircd.actors.UserDatabaseActorMessages.AllUsers

object TcpConnectionHandler {

  object Messages {

    case class Connection(c: ActorRef)

    case class Write(msg: String)

  }

}

class TcpConnectionHandler extends Actor with ActorLogging {
  import Tcp._
  import TcpConnectionHandler._
  import context.dispatcher

  var connection: ActorRef = _

  def userDB = context.actorSelection("akka://Main/user/app/userDB")

  def receive = {
    case Messages.Connection(c) =>
      connection = c
      connection ! Register(self)
    case Messages.Write(msg) =>
      connection ! Write(ByteString(msg))
    case Received(data) =>
      val string: String = data.decodeString("UTF-8")
      log.info(s"[in] $string")
      assert(connection == sender())
      implicit val timeout = Timeout(1 second)
      for { users <- (userDB ? AllUsers).mapTo[Seq[User]] }
        yield users.foreach{ it =>
          log.info(s"broadcasting to $it")
          it.tcp ! Messages.Write(string)
        }
    case PeerClosed =>
      context stop self
  }
}
