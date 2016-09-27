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

package net.abesto.akkaircd.parser

import net.abesto.akkaircd.model.RawMessage
import org.parboiled2.Rule0
import org.scalatest.{FlatSpec, Matchers}

class MessageFormatParserTest extends FlatSpec with Matchers {

  class TestParser(input: String) extends MessageFormatParser(input) {
    def test0(r: Rule0): Rule0 = rule { r ~ EOI }
  }

  protected def testRule0(toRule: (TestParser => Rule0), cases: Map[String, Boolean]) = {
    cases.foreach {
      case (input, success) =>
        val p = new TestParser(input)
        withClue(s"For input '$input'") {
          p.test0(toRule(p)).run().isSuccess should equal(success)
        }
    }
  }

  "nickname parser" should "match valid usernames only" in {
    testRule0(
      _.nickname,
      Map(
        "arst" -> true,
        "[]\\`_^{|}" -> true,
        "333" -> false
      )
    )
  }

  "command parser" should "parse valid commands into Command objects" in {
    Map(
      "100" -> Right(100), // scalastyle:ignore magic.number
      "fooBAR" -> Left("fooBAR")
    ).foreach {
      case (input, expected) =>
        new MessageFormatParser(input).command.run().get should equal(expected)
    }
  }

  "command parser" should "reject invalid commands" in {
    Seq("1", "10", "2fo", "foo2").foreach { input =>
      withClue(input) {
        new MessageFormatParser(input).commandWithEOI.run().isSuccess should equal(false)
      }
    }
  }

  "params parser" should "capture a Seq[String] for valid inputs" in {
    Map(
      "" -> Seq.empty,
      " a" -> Seq("a"),
      " a b" -> Seq("a", "b"),
      " a b :asdf qwer" -> Seq("a", "b", "asdf qwer"),
      " 1 2 3 4 5 6 7 8 9 10 11 12 13 14 asdf qwer" -> ((1 to 14).map(_.toString) :+ "asdf qwer"),
      " 1 2 3 4 5 6 7 8 9 10 11 12 13 14 :asdf qwer" -> ((1 to 14).map(_.toString) :+ "asdf qwer")
    ).foreach {
      case (input, expected) =>
        withClue(s"'$input'") {
          val result = new MessageFormatParser(input).paramsWithEOI.run()
          result.isSuccess should be(true)
          result.get should equal(expected)
        }
    }
  }

  "params parser" should "reject invalid inputs" in {
    Seq(" ", "a  b").foreach {
      new MessageFormatParser(_).paramsWithEOI.run().isSuccess should be(false)
    }
  }

  "message parser" should "parse valid messages into Message objects" in {
    Map(
      ":pre JOIN #foobar #barbaz\r\n" -> Some(RawMessage(Some("pre"), Left("JOIN"), Seq("#foobar", "#barbaz"))),
      "QUIT\r\n" -> Some(RawMessage(None, Left("QUIT"), Seq())),
      "join" -> None
    ).foreach {
      case (input, expectedOutput) =>
        new MessageFormatParser(input).message.run().toOption should equal(expectedOutput)
    }
  }

  "shortname parser" should "accept valid inputs only" in {
    testRule0(_.shortname, Map(
      "" -> false,
      "a" -> true,
      "foobar" -> true,
      "a-foob-baz" -> true
    ))
  }

  "hostname parser" should "accept valid inputs only" in {
    testRule0(_.hostname, Map(
      "" -> false,
      "a" -> true,
      "a-b" -> true,
      "a-b." -> false,
      "a-b.c" -> true,
      "a.b-c" -> true,
      // This looks wrong, but the grammar in RFC2812 allows it
      "a-b.c-" -> true,
      "a-b.c-d.ef" -> true
    ))
  }

  "ip4addr parser" should "accept valid inputs only" in {
    testRule0(_.ip4addr, Map(
      "" ->  false,
      "1." ->  false,
      "1.2" ->  false,
      "1.2." ->  false,
      "1.2.3" ->  false,
      "1.2.3." ->  false,
      "1.2.3.4" ->  true,
      "1.2.3.4." ->  false,
      ".1.2.3.4" ->  false,
      "127.0.0.1" ->  true,
      "0.0.0.0" ->  true,
      "999.999.999.999" ->  true
    ))
  }

  "user parser" should "accept valid inputs only" in {
    testRule0(_.user, Map(
      "a b" -> false,
      "a\rb" -> false,
      "a\nb" -> false,
      "a@b" -> false,
      "asdf" -> true,
      "!#^QWER" -> true
    ))
  }
}
