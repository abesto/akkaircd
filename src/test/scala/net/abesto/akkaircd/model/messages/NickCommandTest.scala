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

package net.abesto.akkaircd.model.messages

import java.net.InetSocketAddress

import akka.io.Tcp.{Connected, Received, Register, Write}
import akka.util.ByteString
import net.abesto.akkaircd.IntegrationTest
import net.abesto.akkaircd.actors.SingletonActors
import net.abesto.akkaircd.actors.UserRegistry.Messages.GetByTcpConnection
import net.abesto.akkaircd.model.UserRef

import scala.concurrent.duration._
import scala.language.{implicitConversions, postfixOps}
import scala.util.Random


class NickCommandTest extends IntegrationTest("NickCommandTest") {

  "NICK" should "return ERR_NONICKNAMEGIVEN if called without parameters" in {
    val client = new TestClient("nick-errnonick")
    client connect()
    client send "NICK"
    client.probe.expectMsg(Register(client.user.tcpHandler))
    client assertReceived ":akkairc.localhost 431 * :No nickname given"
  }
}
