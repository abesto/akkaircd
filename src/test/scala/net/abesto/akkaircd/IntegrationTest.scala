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

package net.abesto.akkaircd

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import akka.io.Tcp.{Connected, Received, Write}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.{ByteString, Timeout}
import net.abesto.akkaircd.actors.SingletonActors
import net.abesto.akkaircd.actors.UserRegistry.Messages.GetByTcpConnection
import net.abesto.akkaircd.model.UserRef
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Random

class IntegrationTest(actorSystemName: String) extends TestKit(ActorSystem(actorSystemName)) with FlatSpecLike with Matchers with ImplicitSender {
  implicit val timeout = Timeout(5 seconds)

  class TestClient(actorName: String) {
    val random = new Random()
    val clientInetSocketAddress: InetSocketAddress = new InetSocketAddress(random.nextInt(65534 - 10000) + 10000)
    val serverInetSocketAddress: InetSocketAddress = new InetSocketAddress("test-server", random.nextInt(10000)) // scalastyle:ignore magic.number

    val probe = TestProbe(actorName)
    val actor = probe.ref
    var user: UserRef = _

    def connect(): Unit = {
      SingletonActors.tcpListener.tell(Connected(clientInetSocketAddress, serverInetSocketAddress), actor)
      user = awaitAssertAndGet(
        SingletonActors.userDB.tAsk[Option[UserRef]](GetByTcpConnection(actor)).get
      )
    }

    def send(msg: String): Unit = user.tcpHandler ! Received(ByteString(s"$msg\r\n"))

    def assertReceived(msg: String, timeout: Duration = 100.millis): Unit =
      assert(probe.receiveOne(timeout).asInstanceOf[Write].data.utf8String == s"$msg\r\n")
  }

  SingletonActors.initialize(system)

  def awaitAssertAndGet[T](a: ⇒ T, max: Duration = Duration.Undefined, interval: Duration = 100.millis): T = {
    awaitAssert(a, max, interval)
    a
  }

  trait TypedAsk {
    def tAsk[T](msg: Any): T
    def `??`[T](msg: Any): T = tAsk(msg)  // scalastyle:ignore method.name
  }

  implicit class ActorRefUtils(val ref: ActorRef) extends TypedAsk {
    override def tAsk[T](msg: Any): T = (ref ? msg).value.get.get.asInstanceOf[T]
  }

  implicit class ActorSelectionUtils(val ref: ActorSelection) extends TypedAsk {
    override def tAsk[T](msg: Any): T = {
      val future = ref ? msg
      (ref ? msg).value.get.get.asInstanceOf[T]
    }
  }
}
