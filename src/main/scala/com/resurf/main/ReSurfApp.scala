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

package com.resurf.main

import java.net.URL
import com.resurf.graph.{ ReSurfNode, ReferrerGraph, ReSurfEdge }
import com.resurf.etl.Converters
import com.twitter.util.{ Time, Duration, StorageUnit }
import com.twitter.conversions.time._
import com.twitter.conversions.storage._
import com.resurf.common._
import collection.JavaConverters._

object ReSurfApp {

  def main(args: Array[String]) {

      val mitmproxy_header_file = io.Source.fromFile(args(0))
      val mitmproxy_header_file_lines = mitmproxy_header_file.getLines().toList
      mitmproxy_header_file.close

      val requests = mitmproxy_header_file_lines.map{line => Converters.customMITMProxy2WebEvent(line)}
      val graph = new ReferrerGraph("graph-name")
      requests.foreach{request => graph.processRequest(request)}
      graph.getUserBrowsing.foreach(println)
      //val headNodes = graph.getHeadNodes
      //headNodes.map{node => node.getId}.toSeq.sorted.foreach(nodeId => println(nodeId))

    //Demonstrate the scenario presented in the ReSurf research paper (ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=6663499)
//    val START_TIME = 1000000
//    val HTTP_METHOD = "GET"
//    val CONTENT_TYPE = Some("text/html")
//    val SIZE = Some(4000.bytes)
//
//    val now = Time.fromMilliseconds(START_TIME)
//    val url1 = new URL(prepareURLString("http://www.cnn.com"))
//    val url2 = new URL(prepareURLString("http://www.gamestop.com"))
//    val url3 = new URL(prepareURLString("http://ads.cnn.com/id=210"))
//    val url4 = new URL(prepareURLString("http://ad.doubleclick.net/moij"))
//    val url5 = new URL(prepareURLString("http://google-analytics.com/__utm.gif"))
//    val url6 = new URL(prepareURLString("http://facebook.com/likebox.php"))
//    val url7 = new URL(prepareURLString("http://fbcdn.net/138.jpg"))
//    val url8 = new URL(prepareURLString("http://www.gamestop.com/events"))
//    val url9 = new URL(prepareURLString("http://www.gamestop.com/hplib-min.js"))
//    val url10 = new URL(prepareURLString("http://s0.2mdn.net/300x250.swf"))
//
//    val requests = List(WebRequest(now, HTTP_METHOD, url1, None, CONTENT_TYPE, SIZE),
//      WebRequest(now + 500.millis, HTTP_METHOD, url3, Some(url1), CONTENT_TYPE, SIZE),
//      WebRequest(now + 700.millis, HTTP_METHOD, url4, Some(url3), CONTENT_TYPE, SIZE),
//      WebRequest(now + 300.millis, HTTP_METHOD, url5, Some(url1), CONTENT_TYPE, SIZE),
//      WebRequest(now + 400.millis, HTTP_METHOD, url6, Some(url1), CONTENT_TYPE, SIZE),
//      WebRequest(now + 700.millis, HTTP_METHOD, url7, Some(url6), CONTENT_TYPE, SIZE),
//      WebRequest(now + 1000.seconds, HTTP_METHOD, url2, None, CONTENT_TYPE, SIZE),
//      WebRequest(now + 1000.seconds + 900.millis, HTTP_METHOD, url5, Some(url2), CONTENT_TYPE, SIZE),
//      WebRequest(now + 1000.seconds + 1100.millis, HTTP_METHOD, url6, Some(url2), CONTENT_TYPE, SIZE),
//      WebRequest(now + 1000.seconds + 1300.millis, HTTP_METHOD, url7, Some(url6), CONTENT_TYPE, SIZE),
//      WebRequest(now + 1000.seconds + 35.seconds, HTTP_METHOD, url8, Some(url2), CONTENT_TYPE, SIZE),
//      WebRequest(now + 1000.seconds + 35.seconds + 100.millis, HTTP_METHOD, url9, Some(url8), CONTENT_TYPE, SIZE),
//      WebRequest(now + 1000.seconds + 35.seconds + 200.millis, HTTP_METHOD, url10, Some(url8), CONTENT_TYPE, SIZE))
//
//    val graph = new ReferrerGraph("graph-name")
//    requests.foreach { request => graph.processRequest(request) }
//
//    //val headNodes = graph.getHeadNodes
//    //headNodes.foreach { node => println(node.getId) }
//    graph.assignNodesToHeadNodes.foreach { case (node, headNode) => println(node.getId + " --- " + headNode.getOrElse("Unknown")) }

  }
}
