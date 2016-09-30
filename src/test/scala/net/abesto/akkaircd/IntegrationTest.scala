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

import akka.actor.{ActorRef, ActorSelection, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.Timeout
import net.abesto.akkaircd.actors.SingletonActors
import org.scalatest.{FlatSpecLike, Matchers}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

class IntegrationTest(actorSystemName: String) extends TestKit(ActorSystem(actorSystemName)) with FlatSpecLike with Matchers with ImplicitSender {
  implicit val timeout = Timeout(5 seconds)

  SingletonActors.initialize(system)

  def awaitAssertAndGet[T](a: ⇒ T, max: Duration = Duration.Undefined, interval: Duration = 100.millis): T = {
    awaitAssert(a, max, interval)
    a
  }

  implicit class ActorRefUtils(ref: ActorRef) {
    // scalastyle:off method.name
    def `??`[T](msg: Any): T = (ref ? msg).value.get.get.asInstanceOf[T]

    // scalastyle:on method.name
  }

  implicit class ActorSelectionUtils(ref: ActorSelection) {
    // scalastyle:off method.name
    def `??`[T](msg: Any): T = Await.ready(ref ? msg, timeout.duration).value.get.get.asInstanceOf[T]

    // scalastyle:on method.name
  }
}
