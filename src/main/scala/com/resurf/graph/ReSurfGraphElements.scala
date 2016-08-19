package com.resurf.graph

import org.graphstream.graph.Element
import org.graphstream.graph.implementations.{ MultiNode, AbstractNode, AbstractEdge, AbstractGraph }
import java.lang
import scala.collection.JavaConverters._
import com.twitter.util.Duration
import com.resurf.common._

/** A trait representing an element in a ReSurf referrer graph.
 * 
 * All ReSurfElement objects have a request repository.
 */
trait ReSurfElement extends Element {
	val requestRepo = new RequestRepository()
}

/** A class representing a node in a ReSurf referrer graph. 
 * 
 * Each node in a ReSurf referrer graph typically represents a URL (the URL could have been the target of a request, a referrer, or both). 
 * ResurfNode extends the basic graphstream MultiNode class and the graph element trait ReSurfElement. The RequestRepository of a ReSurfNode
 * stores all requests with the specific node as the target (regardless of whether the request had a referrer or not).
 *
 * @constructor create a new ReSurfNode
 * @param graph
 * @param id
 */
class ReSurfNode(graph: AbstractGraph, id: String) extends MultiNode(graph: AbstractGraph, id: String) with ReSurfElement {

	/** The average time gap between requests targeting this node and requests targeting this nodes parent nodes (referrers)
	 * 
	 * @return the average time gap
	 */
	def timeGapAvg: Option[Duration] = {
		//incoming requests to parent of node including those without referrer
		val reqsToParentNodes = parentNodeSet.toList.flatMap { node => node.requestRepo.getRepo }
		//incoming requests excluding those without referrer (since these won't be able to be matched)
		val reqsToNodeSorted = getEnteringEdgeSet[ReSurfEdge].asScala.flatMap { edge => edge.requestRepo.getRepo }.toSeq.sorted
		(!reqsToParentNodes.isEmpty && !reqsToNodeSorted.isEmpty) match {
			case false => None
			case true =>
				reqsToParentNodes.flatMap { parentReq => RequestRepository.getDurationToNextRequest(parentReq, reqsToNodeSorted).toList } match {
					case Nil => None
					case k: Seq[Duration] => Some(averageDuration(k))
				}
		}
	}

	/** The content type of this node
	 * 
	 * The content type is calculated as the mode of content types specified by requests targeting this node
	 * 
	 * @return the content type of this node
	 */
	def contentTypeMode: Option[String] = requestRepo.isEmtpy match {
		case true => None
		case false => listMode(requestRepo.getRepo.flatMap(_.contentType.toList))
	}

	/** The parameters of this node
	 * 
	 * The parameters is calculated as the mode of parameters specified by requests targeting this node
	 * 
	 * @return the parameter of this node
	 */
	def parametersMode: Option[String] = requestRepo.isEmtpy match {
		case true => None
		case false => listMode(requestRepo.getRepo.flatMap(_.parameters.toList))
	}

	/** The content size of this node
	 * 
	 * The content size is calculated as the average of content sizes specified by replies of requests targeting this node
	 * 
	 * @return the content size of this node
	 */
	def contentSizeAvg: Option[Double] = requestRepo.isEmtpy match {
		case true => None
		case false => {
			val sizes = requestRepo.getRepo.flatMap(_.size.toList)
			sizes match {
				case Nil => None
				case sizesn: List[Int] => Some((sizesn.sum.toDouble) / (sizesn.size.toDouble))
			}
		}
	}

	/** Get the set of children nodes of this node (nodes for which this node is a referrer) 
	 *
	 * @return the set of children nodes of this node
	 */
	def childNodeSet: Set[ReSurfNode] = { getLeavingEdgeSet[ReSurfEdge].asScala.map { edge => edge.getTargetNode[ReSurfNode] }.toSet }
	
	/**  Get the set of parents nodes of this node (nodes that are referrers of this node) 
	 *   
	 *   @return the set of parent nodes of this node
	 */
	def parentNodeSet: Set[ReSurfNode] = { getEnteringEdgeSet[ReSurfEdge].asScala.map { edge => edge.getSourceNode[ReSurfNode] }.toSet }

}

/** A class representing an edge in a ReSurf referrer graph.
 *
 * Each edge in a ReSurf referrer graph represents a request with a specific target and specific referrer.
 * ResurfEdge extends the basic graphstream AbstractEdge class and the graph element trait ReSurfElement. The RequestRepository of a ReSurfEdge
 * stores all requests with the specific target and referrer.
 *
 * @constructor create a new ReSurfEdge
 * @param id the id of edge
 * @param source the id of the source node
 * @param target the id of the target node
 * @param directed whether the node is directed
 */
class ReSurfEdge(id: String, source: AbstractNode, target: AbstractNode, directed: Boolean) extends
AbstractEdge(id: String, source: AbstractNode, target: AbstractNode, directed: Boolean) with ReSurfElement
