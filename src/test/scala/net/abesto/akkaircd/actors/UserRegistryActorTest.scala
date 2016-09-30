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

import akka.pattern.ask
import akka.testkit.{TestActorRef, TestProbe}
import net.abesto.akkaircd.IntegrationTest
import net.abesto.akkaircd.actors.UserRegistry.Messages.{AllUsers, GetByTcpConnection, Join, Leave}
import net.abesto.akkaircd.model.UserRef

import scala.language.postfixOps

class UserRegistryActorTest extends IntegrationTest("UserRegistryActorTest") {

  import scala.concurrent.ExecutionContext.Implicits.global

  "A UserDatabase" should "should not have any users when created" in {
    val db = TestActorRef(new UserRegistry)
    val users: Seq[UserRef] = db ?? AllUsers
    users.isEmpty should be(true)
  }

  it should "correctly maintain the list of users" in {
    val db = TestActorRef(new UserRegistry)

    val Seq(u1, u2, u3) = Seq(UserRef(testActor, TestProbe().ref), UserRef(testActor, TestProbe().ref), UserRef(testActor, TestProbe().ref))
    db ! Join(u1)
    db ! Join(u2)
    db ! Join(u3)

    val usersBeforeLeave: Seq[UserRef] = db ?? AllUsers
    usersBeforeLeave should equal(Seq(u1, u2, u3))

    db ! Leave(u2)
    val usersAfterLeave: Seq[UserRef] = db ?? AllUsers
    usersAfterLeave should equal(Seq(u1, u3))
  }

  it should "be able to look up users by TCP connection ActorRef" in {
    val db = TestActorRef(new UserRegistry)
    db ! Join(UserRef(testActor, TestProbe().ref))
    for {
      result <- db ? GetByTcpConnection(testActor)
    } yield result should equal(testActor)
  }
}
