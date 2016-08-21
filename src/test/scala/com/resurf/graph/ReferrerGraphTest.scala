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
import com.resurf.common.{WebRequest, RequestSummary, TestTemplate}
import com.twitter.util.{Duration, Time}
import com.twitter.conversions.time._
import com.resurf.common._
import collection.JavaConverters._

class ReferrerGraphTest extends TestTemplate {

	
	 test("Graph check that selfloops are not allowed") {
    
    val now = Time.fromMilliseconds(0)
    val urla = new URL(prepareURLString("http://www.a.com"))
    
    val w1 = WebRequest(now, "GET", urla, None, "text/html", 4000)
    val w2 = WebRequest(now + 5.seconds, "GET", urla, Some(urla), "text/html", 4000)

    val graph = new ReferrerGraph("graph-name")
    graph.processRequest(w1)
    graph.processRequest(w2)

    val graphInternal = graph.getInternalGraph
    graphInternal.getEdgeCount should be (0)
    val nodea = graphInternal.getNode[ReSurfNode]("http://www.a.com")
    nodea.getInDegree should be (0)
    nodea.getOutDegree should be (0)
    nodea.timeGapAvg should be (None)

  }
	
}

