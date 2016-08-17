package com.resurf.graph

import com.resurf.common.RequestSummary
import com.twitter.util.Duration
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/** A repository of all the requests of an edge or a node. */
class RequestRepository() {
	
  private val repo: mutable.Buffer[RequestSummary] = new ArrayBuffer()

  /** Adds request to the repository */
  def add(t: RequestSummary) = repo.append(t)

  /** Returns a string representation of the repository */
  override def toString: String = {
    "{ " + repo.mkString(" , ") + " }"
  }

  /** Returns the underlying repository as a List */
  def getRepo: List[RequestSummary] = repo.toList

  /** Returns the size of the repository */
  def size = repo.size
 
  /** Returns true if the repository is empty */
  def isEmtpy = repo.isEmpty
}

/** The companion object of the repository of all the requests of an edge or a node. */
object RequestRepository {
	
  /** Given a request and a large repository of requests find the time it took from the target request until the closest outgoing request.
    * @param incomingRequest RequestSummary
    * @param orderedOutgoingRequests Seq[RequestSummary]
    * @return Option[Duration]
    */
  def getDurationToNextRequest(incomingRequest: RequestSummary,orderedOutgoingRequests: Seq[RequestSummary]): Option[Duration] = {
    val targetTs = incomingRequest.ts
    orderedOutgoingRequests.find(_.ts > targetTs) match {
      case None => None
      case Some(next) => Some(next.ts - targetTs)
    }
  }
}