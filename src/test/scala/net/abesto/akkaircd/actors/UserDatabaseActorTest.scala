package net.abesto.akkaircd.actors

import akka.actor.{Actor, ActorSystem}
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import net.abesto.akkaircd.actors.TcpConnectionHandler.Messages.Write
import net.abesto.akkaircd.actors.UserDatabase.Messages.{AllUsers, Join, Leave}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._

class UserDatabaseActorTest extends TestKit(ActorSystem("UserDatabaseActorTest")) with FlatSpecLike with Matchers {
  implicit val timeout = Timeout(5 seconds)

  implicit class TestActorRefUtils[T <: Actor](ref: TestActorRef[T]) {
    def `??`[R](msg: Any): R = (ref ? msg).value.get.get.asInstanceOf[R]
  }

  "A UserDatabase" should "should not have any users when created" in {
    val db = TestActorRef(new UserDatabase)
    val users: Seq[User] = db ?? AllUsers
    users.isEmpty should be(true)
  }

  it should "welcome joining users with the user count" in {
    val db = TestActorRef(new UserDatabase)

    val probe1 = TestProbe()
    db ! Join(User(probe1.ref))
    probe1.expectMsg(Write("welcome! we have 1 users.\n"))

    val probe2 = TestProbe()
    db ! Join(User(probe2.ref))
    probe1.expectNoMsg()
    probe2.expectMsg(Write("welcome! we have 2 users.\n"))
  }

  it should "correctly maintain the list of users" in {
    val db = TestActorRef(new UserDatabase)

    val Seq(u1, u2, u3) = Seq(User(TestProbe().ref), User(TestProbe().ref), User(TestProbe().ref))
    db ! Join(u1)
    db ! Join(u2)
    db ! Join(u3)

    val usersBeforeLeave: Seq[User] = db ?? AllUsers
    usersBeforeLeave should equal(Seq(u1, u2, u3))

    db ! Leave(u2)
    val usersAfterLeave: Seq[User] = db ?? AllUsers
    usersAfterLeave should equal(Seq(u1, u3))
  }
}
