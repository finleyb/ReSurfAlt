package com.resurf

import com.twitter.util.{Duration, Time}
import java.net.URLDecoder
import com.twitter.conversions.time._

/** Contains common utility methods and Constants */
package object common {
	
	val VALID_HEADNODE_CONTENT_TYPES = Set("text/html","text/xhtml","text/xml","application/xhtml","application/xml")
	val MIN_OBJECT_SIZE = 3000.0
	val MIN_EMBEDDED_OBJECTS = 2.0
	val MIN_PASS_THROUGH_DELAY = 500.millis
	val URI_KEYWORDS = Set("adserver","ads","widget","embed","banner")
	
  def writeDataToDisk[T](data: Seq[T], fname: String) = {
    import java.io._
    val pw = new PrintWriter(new File(fname))
    data.foreach(x => pw.write(x + "\n"))
    pw.close()
  }

  def getCurrentTime = Time.now
  
  /** Calculates the mode of an collection of objects
   * @param list the collection of objects
   * @return the mode object
   */
  def listMode[T](list:Iterable[T]):Option[T] = {
  	list match {
  		case Nil => None
  		case list:Iterable[T] => Some(list.groupBy(i => i).mapValues(_.size).maxBy(_._2)._1)
  	}
  	
  }

  /** Calculates the average duration from a sequence of durations 
   * @param data the sequence of durations 
   * @return the average duration
   */
  def averageDuration(data: Seq[Duration]): Duration = Duration.fromNanoseconds(data.map(_.inNanoseconds).sum / data.size)
    
 /** Prepares a URL string for comparison against other URL string by decoding the URL encoding, downcasing, and finally striping trailing slashes
  *  @param str the URL string to prepare
  *  @return the prepared URL string
  */
  def prepareURLString(str:String):String = URLDecoder.decode(str, "UTF-8").toLowerCase().stripSuffix("/")
}
