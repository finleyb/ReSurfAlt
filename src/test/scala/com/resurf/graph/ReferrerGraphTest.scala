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

package com.resurf.graph

import java.net.URL
import com.resurf.common.{ WebRequest, RequestSummary, TestTemplate }
import com.twitter.util.{ Duration, Time }
import com.twitter.conversions.time._
import com.resurf.common._
import collection.JavaConverters._

class ReferrerGraphTest extends TestTemplate {

  def fixture =
    new {
      val graph = new ReferrerGraph("graph-name")
      val urla = new URL(prepareURLString("http://www.a.com"))
      val urlb = new URL(prepareURLString("http://www.b.com"))
      val urlc = new URL(prepareURLString("http://www.c.com"))
      val urld = new URL(prepareURLString("http://www.d.com"))
      val urle = new URL(prepareURLString("http://www.e.com"))
      val urlf = new URL(prepareURLString("http://www.f.com"))
      val baseTime = Time.fromMilliseconds(0)
    }

  test("Graph check handing of an empty graph (no nodes or edges)") {
    val f = fixture
    val headNodes = f.graph.getHeadNodes
    headNodes should have size (0)
    val assignments = f.graph.assignNodesToHeadNodes
    assignments should have size (0)
  }

  test("Graph check that request with no referrer adds request to repo but does not add node or link") {
    val f = fixture
    val requests = List(WebRequest(f.baseTime, "GET", f.urla, None, "text/html", 4000))
    requests.foreach { request => f.graph.processRequest(request) }

    val graphInternal = f.graph.getInternalGraph
    graphInternal.getEdgeCount should be(0)
    graphInternal.getNodeCount should be(1)
    val nodea = graphInternal.getNode[ReSurfNode](f.urla.toString())
    nodea.requestRepo.size should be(1)
  }

  test("Graph check handing duplicate request") {
    val f = fixture

    val requests = List(WebRequest(f.baseTime, "GET", f.urla, None, "text/html", 4000),
      WebRequest(f.baseTime, "GET", f.urla, None, "text/html", 4000))

    requests.foreach { request => f.graph.processRequest(request) }

    val graphInternal = f.graph.getInternalGraph
    graphInternal.getEdgeCount should be(0)
    graphInternal.getNodeCount should be(1)
    val nodea = graphInternal.getNode[ReSurfNode](f.urla.toString())
    nodea.requestRepo.size should be(2)
  }

  test("Graph check handing of an empty graph with 3 nodes (no edges)") {

    val f = fixture

    val requests = List(
      WebRequest(f.baseTime, "GET", f.urla, None, "text/html", 4000),
      WebRequest(f.baseTime + 300.millis, "GET", f.urlb, None, "text/html", 4000),
      WebRequest(f.baseTime + 400.millis, "GET", f.urlc, None, "text/html", 4000))

    requests.foreach { request => f.graph.processRequest(request) }
    val headNodes = f.graph.getHeadNodes
    headNodes should have size (0)
    val assignments = f.graph.assignNodesToHeadNodes
    assignments should contain allOf
      ((f.graph.getInternalGraph.getNode[ReSurfNode](f.urla.toString) -> None),
        (f.graph.getInternalGraph.getNode[ReSurfNode](f.urlb.toString) -> None),
        (f.graph.getInternalGraph.getNode[ReSurfNode](f.urlc.toString) -> None))
  }

  test("Graph check that assigns correct headnodes even with a graph cycle") {

    val f = fixture

    val requests = List(
      //create a disconnected head node so that the entire graph has at least one
      WebRequest(f.baseTime, "GET", f.urla, None, "text/html", 4000),
      WebRequest(f.baseTime + 300.millis, "GET", f.urlb, Some(f.urla), "text/html", 4000),
      WebRequest(f.baseTime + 400.millis, "GET", f.urlc, Some(f.urla), "text/html", 4000),
      //create a cycle
      WebRequest(f.baseTime, "GET", f.urld, None, "text/html", 4000),
      WebRequest(f.baseTime + 300.millis, "GET", f.urle, Some(f.urld), "text/html", 4000),
      WebRequest(f.baseTime + 400.millis, "GET", f.urld, Some(f.urle), "text/html", 4000))

    requests.foreach { request => f.graph.processRequest(request) }

    val headNodes = f.graph.getHeadNodes
    headNodes should have size (1)
    headNodes.head.getId should be(f.urla.toString())
    val assignments = f.graph.assignNodesToHeadNodes
    assignments should contain allOf
      ((f.graph.getInternalGraph.getNode[ReSurfNode](f.urla.toString) -> Some(f.graph.getInternalGraph.getNode[ReSurfNode](f.urla.toString))),
        (f.graph.getInternalGraph.getNode[ReSurfNode](f.urlb.toString) -> Some(f.graph.getInternalGraph.getNode[ReSurfNode](f.urla.toString))),
        (f.graph.getInternalGraph.getNode[ReSurfNode](f.urlc.toString) -> Some(f.graph.getInternalGraph.getNode[ReSurfNode](f.urla.toString))),
        (f.graph.getInternalGraph.getNode[ReSurfNode](f.urld.toString) -> None),
        (f.graph.getInternalGraph.getNode[ReSurfNode](f.urle.toString) -> None))
  }

  test("Graph check that selfloops are not allowed") {

    val f = fixture

    val requests = List(WebRequest(f.baseTime, "GET", f.urla, None, "text/html", 4000),
      WebRequest(f.baseTime + 5.seconds, "GET", f.urla, Some(f.urla), "text/html", 4000))

    requests.foreach { request => f.graph.processRequest(request) }

    val graphInternal = f.graph.getInternalGraph
    graphInternal.getEdgeCount should be(0)
    val nodea = graphInternal.getNode[ReSurfNode](f.urla.toString())
    nodea.getInDegree should be(0)
    nodea.getOutDegree should be(0)
    nodea.timeGapAvg should be(None)
  }

  test("Graph check handing of edge and node with invalid (negative) time gap") {
    val f = fixture

    val requests = List(
      WebRequest(f.baseTime, "GET", f.urlb, Some(f.urla), "text/html", 4000),
      WebRequest(f.baseTime + 400.millis, "GET", f.urla, None, "text/html", 4000))

    requests.foreach { request => f.graph.processRequest(request) }
    f.graph.getInternalGraph.getNode[ReSurfNode](f.urla.toString()).timeGapAvg should be(None)
    f.graph.getInternalGraph.getEdge[ReSurfEdge](f.graph.getLinkIdAsString(f.urla.toString, f.urlb.toString)).timeGapAvg should be(None)
  }
}