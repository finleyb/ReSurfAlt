package com.resurf.main

import java.net.URL
import com.resurf.graph.{ReSurfNode,ReferrerGraph}
import com.twitter.util.{Time,Duration}
import com.twitter.conversions.time._
import com.resurf.common._
import collection.JavaConverters._

object ReSurfApp{

	def main(args: Array[String]) {
		
		//Demonstrate the scenario presented in the ReSurf research paper (ieeexplore.ieee.org/xpls/abs_all.jsp?arnumber=6663499)
		
    def now = Time.fromMilliseconds(1000000)
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
		
    val w1 = WebRequest(now,"GET",url1,None,"text/html",4000)
    val w2 = WebRequest(now + 500.millis,"GET",url3,Some(url1),"text/html",4000)
    val w3 = WebRequest(now + 700.millis,"GET",url4,Some(url3),"text/html",4000)
    val w4 = WebRequest(now + 300.millis,"GET",url5,Some(url1),"text/html",4000)
    val w5 = WebRequest(now + 400.millis,"GET",url6,Some(url1),"text/html",4000)
    val w6 = WebRequest(now + 700.millis,"GET",url7,Some(url6),"text/html",4000)
    val w7 = WebRequest(now + 1000.seconds,"GET",url2,None,"text/html",4000)
    val w8 = WebRequest(now + 1000.seconds + 900.millis,"GET",url5,Some(url2),"text/html",4000)
    val w9 = WebRequest(now + 1000.seconds + 1100.millis,"GET",url6,Some(url2),"text/html",4000)
    val w10 = WebRequest(now + 1000.seconds + 35.seconds,"GET",url8,Some(url2),"text/html",4000)
    val w11 = WebRequest(now + 1000.seconds + 35.seconds + 100.millis,"GET",url9,Some(url8),"text/html",4000)
    val w12 = WebRequest(now + 1000.seconds + 35.seconds + 200.millis,"GET",url10,Some(url8),"text/html",4000)
    
    val graph = new ReferrerGraph("graph-name")
    graph.processRequest(w1)
    graph.processRequest(w2)
    graph.processRequest(w3)
    graph.processRequest(w4)
    graph.processRequest(w5)
    graph.processRequest(w6)
    graph.processRequest(w7)
    graph.processRequest(w8)
    graph.processRequest(w9)
    graph.processRequest(w10)
    graph.processRequest(w11)
    graph.processRequest(w12)
    
    val headNodes = graph.getHeadNodes
    
    headNodes.foreach{node => println(node.getId)}
	}
}


