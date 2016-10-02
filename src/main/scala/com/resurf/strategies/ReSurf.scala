/*
 * Copyright (C) 2016 Original Work Benjamin Finley
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
package com.resurf.strategies

import com.resurf.graph.{ReferrerGraph}
import com.resurf.common.WebRequest
import com.resurf.common.ReSurfHeadNodeSelectionCriteria
import com.twitter.util.{Duration, Time, StorageUnit}
import com.twitter.conversions.time._
import com.twitter.conversions.storage._
import com.resurf.graph.ReSurfNode

/**
 * This class provides an implementation of the ReSurf strategy for identifying user generated requests
 */
class ReSurf(requests:List[WebRequest],headNodeSelectionCriteria:ReSurfHeadNodeSelectionCriteria) extends Strategy  {

  private val graph = new ReferrerGraph("graph-name",headNodeSelectionCriteria)
  requests.foreach{request => graph.processRequest(request)}

  /**
   * Get the browsing behavior of the user according to the resurf strategy
   *
   * @return the browsing of the user according to the resurf strategy
   */
  def getUserBrowsing: Seq[Tuple2[Time,String]] = {graph.getUserBrowsing}
}

object ReSurf {

  //These variables control the default values for nodes where this information is missing, thus depending
  //on these defaults the methodology can be more aggressive or more conservative in classifying nodes as headnodes.
  //The current default values are based on the most aggressive classification.
  val DefaultNodeContentType = "text/html"
  val DefaultNodeResponseSize = StorageUnit.infinite
  val DefaultNodeAvgTimeGap = Duration.Top
  val DefaultNodeURIKeywords = ""

  val ValidHTTPMethods = Set("GET","POST","HEAD","PUT","OPTIONS","TRACE","DELETE","CONNECT","PATCH")

  //These variables determine which nodes are classified as head nodes.
  //The current values are directly from the ReSurf research paper
  //(ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=6663499)
  val OriginalReSurfValidHeadNodeContentTypes = Set("text/html", "text/xhtml", "text/xml", "application/xhtml", "application/xml")
  val OriginalReSurfMinHeadNodeResponseSize = 3000.bytes
  val OriginalReSurfMinHeadNodeEmbeddedObjects = 2
  val OriginalReSurfMinHeadNodeAvgTimeGap = 500.millis
  val OriginalReSurfNonHeadNodeURIKeywords = Set("adserver", "ads", "widget", "embed", "banner")

  //These variables determine which nodes are classified as head nodes.
  //The current values are directly from the Hviz research paper
  //(http://www.sciencedirect.com/science/article/pii/S1742287615000067)
  val HvizValidHeadNodeContentTypes = Set("text/html", "text/xhtml", "text/xml", "application/xhtml", "application/xml")
  val HvizMinHeadNodeResponseSize = 3000.bytes
  val HvizMinHeadNodeEmbeddedObjects = 2
  val HvizMinHeadNodeAvgTimeGap = 3.seconds
  val HvizNonHeadNodeURIKeywords = Set("adserver", "ads", "widget", "embed", "banner")

   //These conditions determine which nodes are classified as head nodes.
  //They are directly from the ReSurf research paper
  //(ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=6663499)
  val OriginalReSurfHeadNodeConditions = new ReSurfHeadNodeSelectionCriteria(nodeLocalCriteria = List(
      {node => OriginalReSurfValidHeadNodeContentTypes.contains(node.contentTypeMode.getOrElse(DefaultNodeContentType))},
      _.contentSizeAvg.getOrElse(DefaultNodeResponseSize) >= OriginalReSurfMinHeadNodeResponseSize,
      _.getOutDegree >= OriginalReSurfMinHeadNodeEmbeddedObjects,
      _.timeGapAvg.getOrElse(DefaultNodeAvgTimeGap) >= OriginalReSurfMinHeadNodeAvgTimeGap,
      {node => (!OriginalReSurfNonHeadNodeURIKeywords.exists(node.parametersMode.getOrElse(DefaultNodeURIKeywords).contains))}),
      headNodeReferrerMustBeHeadnode = true)

  //These conditions determine which nodes are classified as head nodes.
  //They are directly from the Hviz research paper
  //(http://www.sciencedirect.com/science/article/pii/S1742287615000067)
  val HvizHeadNodeConditions = new ReSurfHeadNodeSelectionCriteria(nodeLocalCriteria = List(
      {node => HvizValidHeadNodeContentTypes.contains(node.contentTypeMode.getOrElse(DefaultNodeContentType))},
      _.contentSizeAvg.getOrElse(DefaultNodeResponseSize) >= HvizMinHeadNodeResponseSize,
      _.getOutDegree >= HvizMinHeadNodeEmbeddedObjects,
      _.timeGapAvg.getOrElse(DefaultNodeAvgTimeGap) >= HvizMinHeadNodeAvgTimeGap,
      {node => (!HvizNonHeadNodeURIKeywords.exists(node.parametersMode.getOrElse(DefaultNodeURIKeywords).contains))}),
      headNodeReferrerMustBeHeadnode = false)
}