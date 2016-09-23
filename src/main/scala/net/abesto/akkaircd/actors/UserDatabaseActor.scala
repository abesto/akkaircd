package net.abesto.akkaircd.actors

import akka.actor.{Actor, ActorLogging, ActorRef}

case class User(tcp: ActorRef)


object UserDatabaseActorMessages {
  case class Join(user: User)
  object AllUsers
}

class UserDatabaseActor extends Actor with ActorLogging {
  var users: Seq[User] = Seq()

  override def receive = {
    case UserDatabaseActorMessages.Join(user) =>
      users :+= user
      user.tcp ! TcpConnectionHandler.Messages.Write(s"welcome! we have ${users.length} users.\n")
    case UserDatabaseActorMessages.AllUsers => sender() ! users
  }
}
