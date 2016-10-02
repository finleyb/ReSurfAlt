/*
 * Copyright (C) 2015 Original Work Marios Iliofotou
 * Copyright (C) 2016 Modified Work Benjamin Finley
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
import com.resurf.common.WebRequest
import com.twitter.util.{ Time, StorageUnit }
import com.resurf.common._

object Converters {
  private[etl] val customSquidPattern = """(\d+\.\d+) (\d+\.\d+\.\d+\.\d+) (\d+) (\w+) "(.+)" (HTTP/\d\.\d) (\d{3}) (\d+) "(.+)" "(.+)" ([\w/\.\-]+)[\n]*""".r

  def customSquid2WebEvent(logLine: String): WebRequest =
    logLine match {
      case customSquidPattern(ts, ip, srcPort, method, url, version, code, bytes, referrer, agent, content) =>
        val optionalReferrer = referrer match {
          case "-" | "" => None
          case x => Some(new URL(x))
        }
        WebRequest(Time.fromMilliseconds((ts.toDouble * 1000).toLong), method, new URL(url),
          optionalReferrer, Some(content), Some(new StorageUnit(bytes.toInt)), Some(logLine))
    }

  def customMITMProxy2WebEvent(logLine: String): WebRequest =
    logLine.split("\t", -1).map(_.trim) match {
      case Array(rawTime, rawMethod, rawURL, rawReferrer, rawContentType, rawSize, code) =>
        val time = Time.fromMilliseconds((rawTime.toDouble * 1000.0).toLong)
        val method = rawMethod
        val url = new URL(prepareURLString(rawURL))
        val referrer = { if (rawReferrer.isEmpty) { None } else { Some(new URL(prepareURLString(rawReferrer))) } }
        val contentType = { if (rawContentType.isEmpty) { None } else { Some(rawContentType.takeWhile(_ != ';')) } }
        val size = { if (rawSize.isEmpty) { None } else { Some(new StorageUnit(rawSize.toLong)) } }
        WebRequest(time, method, url, referrer, contentType, size)
    }

}
