package com.resurf.common

import java.net.URL
import com.twitter.util.{Duration, Time}

/** A summary of a web (HTTP) request
 * @param ts the time of the request
 * @param method the method (GET, POST, etc.) of the request
 * @param parameters the parameters of the URL
 * @param contentType the content-type of the request
 * @param size the content size of the reply of the request
 */
final case class RequestSummary(ts: Time, method: String, parameters: Option[String], contentType: Option[String] = None, size: Option[Int] = None)
  extends Comparable[RequestSummary] {
  override def compareTo(o: RequestSummary): Int = ts.compareTo(o.ts)
}

/** A web (HTTP) request
 * @param ts the time of the request
 * @param method the method (GET, POST, etc.) of the request
 * @param url the URL object representing the target of the request
 * @param referrer the URL object representing the referrer of the request
 * @param contentType the content-type of the request
 * @param size the content size of the reply of the request
 * @param rawContent the raw content of the request
 */
final case class WebRequest(ts: Time, method: String, url: URL, referrer: Option[URL],contentType: String, size: Int, rawContent: Option[String] = None) {
  def getSummary: RequestSummary = RequestSummary(ts, method, Option(url.getQuery), Some(contentType),Some(size))
}

/** A summary of a referrer graph
 * @param nodeCount the number of nodes of the graph
 * @param linkCount the number of links of the graph
 * @param connectedComponentCount the number of connected components of the graph
 */
final case class GraphSummary(nodeCount: Int, linkCount: Int, connectedComponentCount: Int)
