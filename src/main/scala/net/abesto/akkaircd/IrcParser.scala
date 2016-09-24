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

import net.abesto.akkaircd.model.Message
import net.abesto.akkaircd.model.commands.Command

import scala.language.{implicitConversions, postfixOps}
import org.parboiled2._

// scalastyle:off number.of.methods

/**
 * Implements parsing logic for the ABNF defined in rfc2812
 */
class IrcParser(val input: ParserInput) extends Parser {
  implicit def rangeToString(r: Range): String = r.map(_.toChar).mkString

  ////////////////////////////////////////////////////
  // Section 2.3.1: Message format in Augmented BNF //
  // Page 5                                         //
  ////////////////////////////////////////////////////

  //  message    =  [ ":" prefix SPACE ] command [ params ] crlf
  def message: Rule1[Message] = rule {
    (
      (":" ~ (capture(prefix) ~> (Some(_)) ~ space)  // Capture the string part of the prefix, if it exists
        | push(None))                                // Otherwise push None to the value stack to denote that there's no prefix
        ~ command
        ~ (params | push(Seq.empty))                 // Same trick for params
        ~ crlf ~ EOI) ~> Message
  }

  //  prefix     =  servername / ( nickname [ [ "!" user ] "@" host ] )
  def prefix: Rule0 = rule { servername | (nickname ~ optional(optional("!" ~ user) ~ "@" ~ host)) }

  //  command    =  1*letter / 3digit
  def command: Rule1[Command] = rule {
    (capture(oneOrMore(letter)) ~> Command.fromString _) |
      (capture(3.times(digit)) ~> Command.fromNumeric _)
  }

  //  params     =  *14( SPACE middle ) [ SPACE ":" trailing ]
  //             =/ 14( SPACE middle ) [ SPACE [ ":" ] trailing ]
  def params: Rule1[Seq[String]] = rule {
    longParams | shortParams
  }

  def longParams: Rule1[Seq[String]] = rule {
    14.times(space ~ capture(middle)) ~ optional(space ~ optional(":") ~ trailing)
  }

  def shortParams: Rule1[Seq[String]] = rule {
    (1 to 13).times(space ~ capture(middle)) ~ optional(space ~ ":" ~ trailing)
  }

  //  nospcrlfcl =  %x01-09 / %x0B-0C / %x0E-1F / %x21-39 / %x3B-FF
  //                  ; any octet except NUL, CR, LF, " " and ":"
  def nospcrlfcl: Rule0 = rule {
    anyOf(0x01 to 0x09) | anyOf(0x0B to 0x0C) | anyOf(0x0E to 0x1F) | anyOf(0x21 to 0x39) |
      anyOf(0x3B to 0xFF)
  }

  //  middle     =  nospcrlfcl *( ":" / nospcrlfcl )
  def middle: Rule0 = rule { nospcrlfcl ~ zeroOrMore(":" | nospcrlfcl) }

  //  trailing   =  *( ":" / " " / nospcrlfcl )
  def trailing: Rule0 = rule { zeroOrMore(":" | " " | nospcrlfcl) }

  //  SPACE      =  %x20        ; space character
  def space: Rule0 = rule { 0x20.toChar }

  //  crlf       =  %x0D %x0A   ; "carriage return" "linefeed"
  def crlf: Rule0 = rule { 0x0d.toChar ~ 0x0A.toChar }

  ////////////////////////////////////////////////////
  // Section 2.3.1: Message format in Augmented BNF //
  // Page 6                                         //
  ////////////////////////////////////////////////////

  // target     =  nickname / server
  // Errata 4289: "server" should be "servername", there is no "server" rule
  def target: Rule0 = rule { nickname | servername }

  // msgtarget  =  msgto *( "," msgto )
  def msgtarget: Rule0 = rule { msgto ~ zeroOrMore("," ~ msgto) }

  // msgto      =  channel / ( user [ "%" host ] "@" servername )
  // msgto      =/ ( user "%" host ) / targetmask
  // msgto      =/ nickname / ( nickname "!" user "@" host )
  def msgto: Rule0 = rule {
    channel |
      (user ~ optional("%" ~ host) ~ "@" ~ servername) |
      (user ~ "%" ~ host) | targetmask |
      nickname | (nickname ~ "!" ~ user ~ "@" ~ host)
  }

  // channel    =  ( "#" / "+" / ( "!" channelid ) / "&" ) chanstring
  //               [ ":" chanstring ]
  def channel: Rule0 = rule {
    ("#" | "+" | ("!" ~ channelid) | "&") ~ chanstring ~ optional(":" ~ chanstring)
  }

  // servername =  hostname
  def servername: Rule0 = hostname

  // host       =  hostname / hostaddr
  def host: Rule0 = rule { hostname | hostaddr }

  // hostname   =  shortname *( "." shortname )
  def hostname: Rule0 = rule { shortname ~ zeroOrMore("." ~ shortname) }

  // shortname  =  ( letter / digit ) *( letter / digit / "-" )
  //               *( letter / digit )
  //                 ; as specified in RFC 1123 [HNAME]
  def shortname: Rule0 = rule {
    (letter | digit) ~ zeroOrMore(letter | digit | "-") ~
      zeroOrMore(letter | digit)
  }

  // hostaddr   =  ip4addr / ip6addr
  def hostaddr: Rule0 = rule { ip4addr | ip6addr }

  // ip4addr    =  1*3digit "." 1*3digit "." 1*3digit "." 1*3digit
  def ip4addr: Rule0 = rule {
    (1 to 3).times(digit) ~ "." ~
      (1 to 3).times(digit) ~ "." ~
      (1 to 3).times(digit) ~ "." ~
      (1 to 3).times(digit)
  }

  // ip6addr    =  1*hexdigit 7( ":" 1*hexdigit )
  // ip6addr    =/ "0:0:0:0:0:" ( "0" / "FFFF" ) ":" ip4addr
  def ip6addr: Rule0 = rule {
    oneOrMore(hexdigit) ~ 7.times(":" ~ oneOrMore(hexdigit)) |
      "0:0:0:0:0:" ~ ("0" | "FFFF") ~ ":" ~ ip4addr
  }

  // nickname   =  ( letter / special ) *8( letter / digit / special / "-" )
  def nickname: Rule0 = rule {
    (letter | special) ~ optional((1 to 8).times(letter | digit | special | "-"))
  }

  // scalastyle:off magic.number

  // targetmask =  ( "$" / "#" ) mask
  //                 ; see details on allowed masks in section 3.3.1
  def targetmask: Rule0 = rule { "$" | "#" }

  // chanstring =  %x01-07 / %x08-09 / %x0B-0C / %x0E-1F / %x21-2B
  // chanstring =/ %x2D-39 / %x3B-FF
  //                 ; any octet except NUL, BELL, CR, LF, " ", "," and ":"
  // Errata 385: %x01-07 should be %x01-06
  // Errata 3783 says this should instead be:
  // chanstring = *49(%x01-06 / %x08-09 / %x0B-0C / %x0E-1F / %x21-2B /
  //              %x2D-39 / %x3B-FF)
  def chanstring: Rule0 = rule {
    optional((1 to 49).times(
      anyOf(0x01 to 0x06) | anyOf(0x08 to 0x09) | anyOf(0x0B to 0x0C) | anyOf(0x0E to 0x1F) | anyOf(0x21 to 0x2B) |
        anyOf(0x2D to 0x39) | anyOf(0x3B to 0xFF)
    ))
  }

  // channelid  = 5( %x41-5A / digit )   ; 5( A-Z / 0-9 )
  def channelid: Rule0 = rule { 5.times(anyOf(0x41 to 0x5A) | digit) }

  ////////////////////////////////////////////////////
  // Section 2.3.1: Message format in Augmented BNF //
  // Page 7                                         //
  ////////////////////////////////////////////////////
  // user       =  1*( %x01-09 / %x0B-0C / %x0E-1F / %x21-3F / %x41-FF )
  //                 ; any octet except NUL, CR, LF, " " and "@"
  def user: Rule0 = rule {
    oneOrMore(
      anyOf(0x01 to 0x09) | anyOf(0x0B to 0x0C) | anyOf(0x0E to 0x1F) | anyOf(0x21 to 0x3F) | anyOf(0x41 to 0xFF)
    )
  }

  // key        =  1*23( %x01-05 / %x07-08 / %x0C / %x0E-1F / %x21-7F )
  //                 ; any 7-bit US_ASCII character,
  //                 ; except NUL, CR, LF, FF, h/v TABs, and " "
  def key: Rule0 = rule {
    (1 to 23).times(
      anyOf(0x01 to 0x05) | anyOf(0x07 to 0x08) | 0x0C.toChar | anyOf(0x0E to 0x1F) | anyOf(0x21 to 0x7F)
    )
  }

  // letter     =  %x41-5A / %x61-7A       ; A-Z / a-z
  def letter: Rule0 = rule { anyOf(0x41 to 0x5A) | anyOf(0x61 to 0x7A) }

  // digit      =  %x30-39                 ; 0-9
  def digit: Rule0 = rule { anyOf(0x30 to 0x39) }

  // hexdigit   =  digit / "A" / "B" / "C" / "D" / "E" / "F"
  def hexdigit: Rule0 = rule {
    digit | "A" | "B" | "C" | "D" | "E" | "F"
  }

  // special    =  %x5B-60 / %x7B-7D
  //                  ; "[", "]", "\", "`", "_", "^", "{", "|", "}"
  def special: Rule0 = rule {
    anyOf(0x5B to 0x60) | anyOf(0x7B to 0x7D)
  }

  // scalastyle:on magic.number
}

// scalastyle:on number.of.methods


