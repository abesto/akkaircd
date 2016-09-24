package net.abesto.akkaircd.actors

import akka.actor.{Actor, ActorLogging, ActorRef}

case class User(tcp: ActorRef)


object UserDatabase {

  object Messages {

    case class Join(user: User)

    case class Leave(user: User)

    object AllUsers

  }

}


class UserDatabase extends Actor with ActorLogging {

  import UserDatabase.Messages._

  var users: Seq[User] = Seq()

  override def receive = {
    case Join(user) =>
      users :+= user
      user.tcp ! TcpConnectionHandler.Messages.Write(s"welcome! we have ${users.length} users.\n")

    case Leave(user) =>
      users = users.filter(_ != user)

    case AllUsers => sender() ! users
  }
}
