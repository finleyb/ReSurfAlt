/*
 * Copyright (C) 2015 Original Work Marios Iliofotou
 *
 * This file is part of ReSurfAlt.
 *
 * ReSurfAlt is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ReSurfAlt is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ReSurfAlt. If not, see <http://www.gnu.org/licenses/>.
 */

package com.resurf.etl

import java.net.URL
import Converters._
import com.resurf.common.TestTemplate


class ConvertersTest extends TestTemplate {

  test("Basic URL parsing") {
    val url1 = "http://www.ninja.com/a/hello.js?a=2&b=3"
    val myURL = new URL(url1)
    myURL.getQuery shouldEqual "a=2&b=3"
  }

  val ExampleLog =
    """
      |1419826260.846 192.168.1.104 54824 GET "http://www.google-analytics.com/r/__utm.gif?" HTTP/1.1 200 548 "http://www.in.gr/" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36" image/gif
    """.stripMargin.trim

  test("Custom Squid: log simple line parsing") {
    """123.34 192.168.1.104 2345 HEAD "www.get.com/k/l.js" HTTP/1.1 200 1234 "www.from.com" "StrangeClient" image/gif""" match {
      case customSquidPattern(ts, ip, srcPort, method, url, version, code, bytes, referrer, agent, content) =>
        Seq(ts, ip, srcPort, method, url, version, code, bytes, referrer, agent, content).mkString("-")
        assert(agent === "StrangeClient")
    }
  }

  test("Custom Squid: log complex line") {
    ExampleLog match {
      case customSquidPattern(ts, ip, srcPort, method, url, version, code, bytes, referrer, agent, content) =>
        assert(srcPort === "54824")
    }
  }

  test("Creating a WebRequest from toy line") {
    val event = Converters.customSquid2WebEvent(ExampleLog)
    event.contentType shouldBe "image/gif"
  }

  val ExampleLogWithNoReferrer =
    """
      |1419826260.846 192.168.1.104 54824 GET "http://www.google-analytics.com/r/__utm.gif?" HTTP/1.1 200 548 "-" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36" image/gif
    """.stripMargin.trim
  test("Creating a WebRequest without Referrer") {
    val event = Converters.customSquid2WebEvent(ExampleLogWithNoReferrer)
    event.referrer shouldBe None
  }
}
