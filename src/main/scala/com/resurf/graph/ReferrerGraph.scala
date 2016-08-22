/*
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

import java.lang
import com.resurf.common._
import com.twitter.util.Duration
import org.graphstream.graph.{ Graph, EdgeFactory, NodeFactory, Node }
import org.graphstream.graph.implementations.{ MultiGraph, AbstractEdge, AbstractNode, AbstractGraph }
import org.graphstream.algorithm.ConnectedComponents
import org.graphstream.ui.view.Viewer
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory
import scalaz.Memo

/**
 * This class provides a representation of a referrer graph.
 * Typically there will be one referrer graph for each device or IP address, etc.
 *
 * @constructor create a new referrer graph with the specified identifier
 * @param id the identifier
 */
class ReferrerGraph(id: String) {

  private[this] lazy val logger = LoggerFactory.getLogger(this.getClass)
  //the internal graphstream multigraph that holds our custom nodes and edges
  private val internalGraph: Graph = new MultiGraph(s"RG:$id", false, true)

  //set the node factory to the custom node class
  internalGraph.setNodeFactory(new NodeFactory[ReSurfNode] {
    def newInstance(id: String, graph: Graph): ReSurfNode = {
      new ReSurfNode(graph.asInstanceOf[AbstractGraph], id)
    }
  })

  //set the edge factory to the custom edge class
  internalGraph.setEdgeFactory(new EdgeFactory[ReSurfEdge] {
    def newInstance(id: String, src: Node, dst: Node, directed: Boolean): ReSurfEdge = {
      new ReSurfEdge(id, src.asInstanceOf[AbstractNode], dst.asInstanceOf[AbstractNode], directed)
    }
  });

  def getLinkIdAsString(src: String, dst: String): String = s"$src->$dst"

  /**
   * Add a node to the referrer graph based on the specified RequestSummary
   *
   * @param nodeId the id of the node (typically the URL of the request)
   * @param details the request summary object of the request
   */
  def addNode(nodeId: String, details: Option[RequestSummary] = None): Unit = {
    details match {
      case None =>
        internalGraph.addNode(nodeId)
        ()
      case Some(request) =>
        logger.debug(s"Adding node $nodeId with details $details")
        //node is not in internalGraph
        val node = Option(internalGraph.getNode[ReSurfNode](nodeId))
        node match {
          case None =>
            logger.debug("First time seeing node: {}", nodeId)
            this.addNode(nodeId)
            internalGraph.getNode[ReSurfNode](nodeId).requestRepo.add(request)
          case Some(node) =>
            node.requestRepo.add(request)
        }
    }
  }

  /**
   * Add a link to the referrer graph based on the specified source node, destination node, and request
   *
   * @param srcId the id of the source node (typically the referrer URI of the request)
   * @param dstId the id of the destination node (typically the target URI of the request)
   * @param details the request summary object of the request
   */
  def addLink(srcId: String, dstId: String, details: RequestSummary): Unit = {
    logger.debug(s"Adding edge from $srcId to $dstId")
    val edgeId = getLinkIdAsString(srcId, dstId)
    this.addNode(srcId)
    //nodes store their incoming requests as the repository
    this.addNode(dstId, Some(details))
    // Gets the edge if it exists, else it returns null
    val edge = Option(internalGraph.getEdge[ReSurfEdge](edgeId)) //if the edge (link) does not exist then create it!
    match {
      case None =>
        logger.debug(s"New edge from $srcId to $dstId")
        internalGraph.addEdge(edgeId.toString, srcId, dstId, true)
        val e = internalGraph.getEdge[ReSurfEdge](edgeId.toString)
        e.requestRepo.add(details)
      //else add the request summary to the link's request repo
      case Some(edge) =>
        edge.requestRepo.add(details)
    }
  }

  /**
   * Get a summary of the referrer graph
   * @return a graph summary for the referrer graph
   */
  def getGraphSummary: GraphSummary = {
    val cc = new ConnectedComponents()
    cc.init(internalGraph)
    GraphSummary(nodeCount = internalGraph.getNodeCount, linkCount = internalGraph.getEdgeCount, connectedComponentCount = cc.getConnectedComponentsCount)
  }

  /** Display the referrer graph */
  def viz: Viewer = internalGraph.display()

  /**
   * Processes the specified web request by creating the specific node(s) and link in the referrer graph
   * @param newEvent the HTTP request to process
   */
  def processRequest(newEvent: WebRequest): Unit = {
    //Deal with HTTP redirection 302 statuses ????
    val referrer = newEvent.referrer
    referrer match {
      case None =>
        // There is no referrer, so we just update the node
        addNode(newEvent.url.toString, Some(newEvent.getSummary))
      case Some(ref) =>
        //check to make sure the url and referrer are not the same since this causes self-loop
        if (ref == newEvent.url) {
          val newEventCopyWOReferrer = newEvent.copy(referrer = None)
          addNode(newEventCopyWOReferrer.url.toString, Some(newEventCopyWOReferrer.getSummary))
        } else {
          // There is a referrer, so we can update the link (from referrer to target)
          addLink(ref.toString, newEvent.url.toString, newEvent.getSummary)
        }
    }
  }

  private def candidateHNCriteria(node: ReSurfNode): Boolean = {
    VALID_HEADNODE_CONTENT_TYPES.contains(node.contentTypeMode.getOrElse("text/html")) &&
      node.contentSizeAvg.getOrElse(Double.MaxValue) >= MIN_OBJECT_SIZE &&
      node.getOutDegree >= MIN_EMBEDDED_OBJECTS &&
      node.timeGapAvg.getOrElse(Duration.Top) >= MIN_PASS_THROUGH_DELAY &&
      (!URI_KEYWORDS.exists(node.parametersMode.getOrElse("").contains))
  }

  def getInternalGraph = internalGraph

  /**
   * Get the head node that each node maps to according to the ReSurf methodology.
   * Headnodes naturally map to themselves. Nodes that are not head nodes and do not map to a
   * headnode are considered unknown and map to None.
   *
   * @return a map in the form (node => Option[headnode])
   */
  def assignNodesToHeadNodes: Map[ReSurfNode, Option[ReSurfNode]] = {

    //get the headnodes
    val headNodes = getHeadNodes
    
    //store the IDs of taken edges while searching so that we don't end up trapped in a cycle
    var takenEdgeIDs = Set.empty[String]
    
    //define recursive function that traverses backward toward the head node
    lazy val traverseToHeadNode: Option[ReSurfNode] => Option[ReSurfNode] =
      //use memoization from scalaz to improve performance
      Memo.mutableHashMapMemo {
        case None => throw new Exception("A node along the chain is null, this should never happen!")
        case Some(node) => {
          //node is head node thus we found the headnode to map to! 
          if (headNodes.contains(node)) {
            Some(node)
            //else if node still has incoming edges and we haven't take all of them yet then follow the shortest one backward
          } else if (node.getInDegree > 0 && node.getEachEnteringEdge[ReSurfEdge].asScala.filter{node => !takenEdgeIDs.contains(node.getId)}.size > 0) {
          	val enteringEdgesNotTaken = node.getEachEnteringEdge[ReSurfEdge].asScala.filter{node => !takenEdgeIDs.contains(node.getId)}
            val smallestEdgeNotTaken = enteringEdgesNotTaken.minBy {edge => edge.timeGapAvg}
            takenEdgeIDs += smallestEdgeNotTaken.getId
            val sourceNodeOfShortestEdgeNotTaken = Some(smallestEdgeNotTaken.getSourceNode[ReSurfNode])
            traverseToHeadNode(sourceNodeOfShortestEdgeNotTaken)
            //node is not a headnode and does not have any incoming edges or untaken incoming edges thus is classified as unknown
          } else { None }
        }
      }

    //find nodes that are not head nodes since we will follow these backwards to the headnodes
    val nonHeadNodes = internalGraph.getNodeSet[ReSurfNode].asScala.toSet.diff(headNodes)
    //call the function for each node that is not a headnode
    nonHeadNodes.map { node => 
    	//reset the taken edge set for each node
    	 takenEdgeIDs = Set.empty[String]
    	(node, traverseToHeadNode(Some(node)))
    }
      //transform to map and add the headnodes as mapping to themselves
      .toMap ++ headNodes.map { headNode => (headNode, Some(headNode)) }
  }

  /**
   * Get the nodes that are considered head nodes through the ReSurf methodology
   * @return the head nodes
   */
  def getHeadNodes: Set[ReSurfNode] = {

    val nodes = internalGraph.getNodeSet[ReSurfNode].asScala
    var total_HN = Set[ReSurfNode]()

    val initial_HN = nodes.filter { node =>
      //check to make sure that node has no referrer (parent node)
      candidateHNCriteria(node) && node.getInDegree == 0
    }.toSet

    var last_HN = initial_HN
    total_HN ++= last_HN

    while (last_HN.size > 0) {

      //get the candidate children and make sure the candidate children are not already head nodes
      //(this could happen in case of graph cycle)
      val children_of_last_HN = last_HN.flatMap { node => node.childNodeSet }.toSet.diff(total_HN)

      last_HN = children_of_last_HN.filter { node =>
        //check to make sure at least one of the referrer (parent) nodes is a head node
        candidateHNCriteria(node) && total_HN.intersect(node.parentNodeSet).size > 0
      }.toSet
      total_HN ++= last_HN
    }
    total_HN
  }

}