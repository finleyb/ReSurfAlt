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

package com.resurf

import com.twitter.util.{ Duration, Time, StorageUnit}
import java.net.URLDecoder
import com.twitter.conversions.time._
import com.twitter.conversions.storage._
import java.io.{PrintWriter,File}
import com.resurf.graph.ReSurfNode

/** Contains common utility methods and Constants */
package object common {

  val DefaultReferrerGraphNodeCapacity = 1024
  val DefaultReferrerGraphEdgeCapacity = 2048
  
  //This variable controls the default edge time gap when that time gap cannot be calculated, for instance when
  //all of the requests to the source node of the edge were before the actual edge requests.
  //This should not be changed unless you know what you are doing.
  val DefaultEdgeAvgTimeGap = Duration.Top
  
  def writeDataToDisk[T](data: Seq[T], fname: String):Unit = {
    val pw = new PrintWriter(new File(fname))
    data.foreach(x => pw.write(x + "\n"))
    pw.close()
  }

  def getCurrentTime:Time = Time.now

  /**
   * Calculates the mode of an collection of objects
   *
   * @param list the collection of objects
   * @return the mode object
   */
  def listMode[T](list: Iterable[T]): Option[T] = {
    list match {
      case Nil => None
      case list: Iterable[T] => Some(list.groupBy(i => i).mapValues(_.size).maxBy(_._2)._1)
    }

  }

  /**
   * Calculates the average duration from a sequence of durations
   *
   * @param data the sequence of durations
   * @return the average duration
   */
  def averageDuration(data: Seq[Duration]): Option[Duration] = {
    if(data.nonEmpty){
      Some(Duration.fromNanoseconds(data.map(_.inNanoseconds).sum / data.size))
    }else{
      None
    }
  }

  /**
   * Calculates the average duration from a sequence of durations
   *
   * @param data the sequence of durations
   * @return the average duration
   */
  def averageStorageSize(data: Seq[StorageUnit]): Option[StorageUnit] = {
    if(data.nonEmpty){
      Some(new StorageUnit(data.map(_.inBytes).sum/data.size))
    }else{
      None
    }
  }

  /**
   * Prepares a URL string for comparison against other URL string by decoding the URL encoding, downcasing, and finally striping trailing slashes
   *
   * @param str the URL string to prepare
   * @return the prepared URL string
   */
  def prepareURLString(str: String): String = URLDecoder.decode(str, "UTF-8").toLowerCase().stripSuffix("/")
}
