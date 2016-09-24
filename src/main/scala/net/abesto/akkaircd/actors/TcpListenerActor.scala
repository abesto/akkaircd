package net.abesto.akkaircd.actors

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.{IO, Tcp}
import net.abesto.akkaircd.Settings

object TcpListenerMessages {

  case class UserDB(db: ActorRef)

  object Listen

}

class TcpListenerActor extends Actor with ActorLogging {

  import Tcp._
  import context.system

  val settings = Settings(system)
  def userDB = context.actorSelection("akka://Main/user/app/userDB")

  override def receive = {
    case TcpListenerMessages.Listen =>
      IO(Tcp) ! Bind(self, new InetSocketAddress(settings.Tcp.Host, settings.Tcp.Port))

    case b@Bound(localAddress) =>
      log.info(s"Listening on $localAddress")

    case CommandFailed(_: Bind) => context stop self

    case c@Connected(remote, local) =>
      log.info(s"Received connection $remote $local")
      val handler = context.actorOf(Props[TcpConnectionHandler])
      handler ! TcpConnectionHandler.Messages.Connection(sender())
      userDB ! UserDatabaseActorMessages.Join(User(handler))
  }
}

