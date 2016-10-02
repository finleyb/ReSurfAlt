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
import com.resurf.strategies.ReSurf

object ReSurfApp {

  def main(args: Array[String]) {

		//val mitmproxyHeaderFile = io.Source.fromFile(args(0))
		//val mitmproxyHeaderFileLines = mitmproxyHeaderFile.getLines().toList
		//mitmproxyHeaderFile.close
		//val requests = mitmproxyHeaderFileLines.map{line => Converters.customMITMProxy2WebEvent(line)}
		//val strategy = new ReSurf(requests,OriginalReSurfHeadNodeConditions)
		//strategy.getUserBrowsing.foreach(println)

    //Demonstrate the scenario presented in the ReSurf research paper (ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=6663499)
    val startTime = 1000000
    val HTTPMethod = "GET"
    val contentType = Some("text/html")
    val size = Some(4000.bytes)

    val now = Time.fromMilliseconds(startTime)
    val url1 = new URL(prepareURLString("http://www.cnn.com"))
    val url2 = new URL(prepareURLString("http://www.gamestop.com"))
    val url3 = new URL(prepareURLString("http://ads.cnn.com/id=210"))
    val url4 = new URL(prepareURLString("http://ad.doubleclick.net/moij"))
    val url5 = new URL(prepareURLString("http://google-analytics.com/__utm.gif"))
    val url6 = new URL(prepareURLString("http://facebook.com/likebox.php"))
    val url7 = new URL(prepareURLString("http://fbcdn.net/138.jpg"))
    val url8 = new URL(prepareURLString("http://www.gamestop.com/events"))
    val url9 = new URL(prepareURLString("http://www.gamestop.com/hplib-min.js"))
    val url10 = new URL(prepareURLString("http://s0.2mdn.net/300x250.swf"))

    val requests = List(WebRequest(now, HTTPMethod, url1, None, contentType, size),
      WebRequest(now + 500.millis, HTTPMethod, url3, Some(url1), contentType, size),
      WebRequest(now + 700.millis, HTTPMethod, url4, Some(url3), contentType, size),
      WebRequest(now + 300.millis, HTTPMethod, url5, Some(url1), contentType, size),
      WebRequest(now + 400.millis, HTTPMethod, url6, Some(url1), contentType, size),
      WebRequest(now + 700.millis, HTTPMethod, url7, Some(url6), contentType, size),
      WebRequest(now + 1000.seconds, HTTPMethod, url2, None, contentType, size),
      WebRequest(now + 1000.seconds + 900.millis, HTTPMethod, url5, Some(url2), contentType, size),
      WebRequest(now + 1000.seconds + 1100.millis, HTTPMethod, url6, Some(url2), contentType, size),
      WebRequest(now + 1000.seconds + 1300.millis, HTTPMethod, url7, Some(url6), contentType, size),
      WebRequest(now + 1000.seconds + 35.seconds, HTTPMethod, url8, Some(url2), contentType, size),
      WebRequest(now + 1000.seconds + 35.seconds + 100.millis, HTTPMethod, url9, Some(url8), contentType, size),
      WebRequest(now + 1000.seconds + 35.seconds + 200.millis, HTTPMethod, url10, Some(url8), contentType, size))

    val strategy = new ReSurf(requests,ReSurf.OriginalReSurfHeadNodeConditions)
    strategy.getUserBrowsing.foreach(println)
    
  }
}
