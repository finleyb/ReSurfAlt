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

package com.resurf.graph

import com.resurf.common.RequestSummary
import com.twitter.util.Duration
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * A class representing a repository of all the requests of an edge or a node.
 *
 */
class RequestRepository() {

  private val repo: mutable.Buffer[RequestSummary] = new ArrayBuffer()

  /**
   * Adds request to the repository
   *
   * @param t the request to add
   */
  def add(t: RequestSummary):Unit = repo.append(t)

  /**
   * A string representation of the repository
   *
   * @return a string representation of the repository
   */
  override def toString: String = { "{ " + repo.mkString(" , ") + " }" }

  /**
   * The underlying repository as a List
   *
   * @return the underlying repository as a List
   */
  def getRepo: List[RequestSummary] = repo.toList

  /**
   * the size of the repository
   *
   * @return the number of elements in the repository
   */
  def size:Int = repo.size

  /**
   * Tests if the repository is empty
   *
   * @return true if the repository is empty
   */
  def isEmtpy:Boolean = repo.isEmpty
}

/** The companion object of a request repository */
object RequestRepository {

  /**
   * Given a request and a large repository of requests find the time it took from the target request until the closest outgoing request.
   *
   * @param incomingRequest
   * @param orderedOutgoingRequests
   * @return the time between the target request and closest outgoing request
   */
  def getDurationToNextRequest(incomingRequest: RequestSummary, orderedOutgoingRequests: Seq[RequestSummary]): Option[Duration] = {
    val targetTs = incomingRequest.ts
    orderedOutgoingRequests.find(_.ts > targetTs) match {
      case None => None
      case Some(next) => Some(next.ts - targetTs)
    }
  }
}
