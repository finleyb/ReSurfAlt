/*
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
import com.twitter.util.{Time, StorageUnit}

object Converters {
  private[etl] val customSquidPattern = """(\d+\.\d+) (\d+\.\d+\.\d+\.\d+) (\d+) (\w+) "(.+)" (HTTP/\d\.\d) (\d{3}) (\d+) "(.+)" "(.+)" ([\w/\.\-]+)[\n]*""".r

  def customSquid2WebEvent(logLine: String): WebRequest =
    logLine match {
      case customSquidPattern(ts, ip, srcPort, method, url, version, code, bytes, referrer, agent, content) =>
        val optionalReferrer = referrer match {
          case "-" | "" => None
          case x => Some(new URL(x))
        }
        WebRequest(Time.fromMilliseconds((ts.toDouble*1000).toLong),method,new URL(url),
          optionalReferrer,content,new StorageUnit(bytes.toInt), Some(logLine) )
    }
}
