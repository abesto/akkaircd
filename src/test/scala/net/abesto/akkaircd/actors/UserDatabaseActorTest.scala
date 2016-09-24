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

import akka.actor.{Actor, ActorSystem}
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import net.abesto.akkaircd.actors.TcpConnectionHandler.Messages.Write
import net.abesto.akkaircd.actors.UserDatabase.Messages.{AllUsers, Join, Leave}
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.duration._
import scala.language.postfixOps

class UserDatabaseActorTest extends TestKit(ActorSystem("UserDatabaseActorTest")) with FlatSpecLike with Matchers {
  implicit val timeout = Timeout(5 seconds)

  implicit class TestActorRefUtils[T <: Actor](ref: TestActorRef[T]) {
    // scalastyle:off method.name
    def `??`[R](msg: Any): R = (ref ? msg).value.get.get.asInstanceOf[R]
    // scalastyle:on method.name
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
