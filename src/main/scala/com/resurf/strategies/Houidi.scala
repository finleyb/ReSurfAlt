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

import com.twitter.util.{Time}
import com.resurf.common.WebRequest
import java.net.URL

/**
 * This class provides an implementation of the Houidi strategy for identifying user generated requests
 * (http://link.springer.com/chapter/10.1007%2F978-3-642-54999-1_8)
 */
class Houidi(requests:List[WebRequest]) extends Strategy {

  val HouidiMinHeadNodeEmbeddedObjects = 2
  val HouidiNonHeadNodeFileExtensions = Set("js","css","swf")
  val HouidiDefaultURLString = "http://none"
  val HouidiDefaultFileExtension = ""

  /**
   * Get the browsing behavior of the user according to the Houidi strategy
   * (http://link.springer.com/chapter/10.1007%2F978-3-642-54999-1_8)
   *
   * @return the browsing of the user according to the Houidi strategy
   */
  def getUserBrowsing: Seq[(Time, String)] = {

    val requestsByReferrer = requests.groupBy{req => req.referrer.getOrElse(new URL(HouidiDefaultURLString)).toString}.-(HouidiDefaultURLString)

    val requestsFChildren = requestsByReferrer.filter{case(url,requests) => requests.size > HouidiMinHeadNodeEmbeddedObjects}

    val requestsFType = requestsFChildren.filter{case(url,requests) => !HouidiNonHeadNodeFileExtensions
      .contains(if(url.lastIndexOf(".") != -1){url.substring(url.lastIndexOf(".")).toLowerCase()}else{HouidiDefaultFileExtension})
    }

    val requestsFinal = requestsFType.keySet

    requests.filter{request => requestsFinal.contains(request.url.toString())}
    .map{request => (request.ts, request.url.toString())}.toSeq.sortBy(request => request._1)
  }
}