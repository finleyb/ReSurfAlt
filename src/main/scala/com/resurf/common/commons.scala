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

package com.resurf.common

import java.net.URL
import com.twitter.util.{Duration, Time, StorageUnit}

/**
 * A summary of a web (HTTP) request
 *
 * @param ts the time of the request
 * @param method the method (GET, POST, etc.) of the request
 * @param parameters the parameters of the URL or None is there were no parameters
 * @param contentType the content-type of the request (defaults to None)
 * @param size the content size of the reply of the request (defaults to None)
 */
case class RequestSummary(ts: Time, method: String, parameters: Option[String], contentType: Option[String] = None, size: Option[StorageUnit] = None)
  extends Comparable[RequestSummary] {
  override def compareTo(o: RequestSummary): Int = ts.compareTo(o.ts)
}

/**
 * A web (HTTP) request
 *
 * @param ts the time of the request
 * @param method the method (GET, POST, etc.) of the request
 * @param url the URL object representing the target of the request
 * @param referrer the URL object representing the referrer of the request or None if no referrer
 * @param contentType the content-type of the request
 * @param size the content size of the reply of the request
 * @param rawContent the raw content of the request (defaults to None)
 */
case class WebRequest(ts: Time, method: String, url: URL, referrer: Option[URL],contentType: Option[String], size: Option[StorageUnit], rawContent: Option[String] = None) {
  def getSummary: RequestSummary = RequestSummary(ts, method, Option(url.getQuery), contentType, size)
}

/**
 * A summary of a referrer graph
 *
 * @param nodeCount the number of nodes of the graph
 * @param linkCount the number of links of the graph
 * @param connectedComponentCount the number of connected components of the graph
 */
case class GraphSummary(nodeCount: Int, linkCount: Int, connectedComponentCount: Int)
