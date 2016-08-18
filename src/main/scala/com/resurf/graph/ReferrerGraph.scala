package com.resurf.graph

import java.lang
import com.resurf.common._
import com.twitter.util.Duration
import org.graphstream.graph.{Graph,EdgeFactory,NodeFactory,Node}
import org.graphstream.graph.implementations.{MultiGraph,AbstractEdge,AbstractNode,AbstractGraph}
import org.graphstream.algorithm.ConnectedComponents
import scala.collection.JavaConverters._
import org.slf4j.LoggerFactory

/** This class provides a representation of a referrer graph.
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
  
  def getLinkIdAsString(src: String, dst: String) = s"$src->$dst"

	/** Add a node to the referrer graph based on the specified RequestSummary
    *
    * @param nodeId the id of the node (typically the URL of the request)
    * @param details the request summary object of the request
    */
   def addNode(nodeId: String, details: Option[RequestSummary] = None):Unit = {
    details match {
      case None =>
        internalGraph.addNode(nodeId)
        ()
      case Some(request) =>
        logger.debug(s"Adding node $nodeId with details $details")
        //node is not in internalGraph
        val node = internalGraph.getNode[ReSurfNode](nodeId)
        if (node == null) {
          logger.debug("First time seeing node: {}", nodeId)
          this.addNode(nodeId)
          internalGraph.getNode[ReSurfNode](nodeId).requestRepo.add(request)
        } else {
          node.requestRepo.add(request)
        }
    }
  }

 /** Add a link to the referrer graph based on the specified source node, destination node, and request
   *
   * @param srcId the id of the source node (typically the referrer URI of the request)
   * @param dstId the id of the destination node (typically the target URI of the request)
   * @param details the request summary object of the request
   */
   def addLink(srcId: String, dstId: String, details: RequestSummary) = {
    logger.debug(s"Adding edge from $srcId to $dstId")
    val edgeId = getLinkIdAsString(srcId, dstId)
    this.addNode(srcId)
    //nodes store their incoming requests as the repository
    this.addNode(dstId, Some(details))
    // Gets the edge if it exists, else it returns null
    val edge = internalGraph.getEdge[ReSurfEdge](edgeId)
    //if the edge (link) does not exist then create it!
    if (edge == null) {
      logger.debug(s"New edge from $srcId to $dstId")
      internalGraph.addEdge(edgeId.toString, srcId, dstId, true)
      val e = internalGraph.getEdge[ReSurfEdge](edgeId.toString)
      assert(e != null, "Edge was just added, it should not be null")
      e.requestRepo.add(details)
      //else add the request summary to the link's request repo
    } else {
      edge.requestRepo.add(details)
    }
  }

 /** Get a summary of the referrer graph
   * @return a graph summary for the referrer graph
   */
  def getGraphSummary: GraphSummary = {
    val cc = new ConnectedComponents()
    cc.init(internalGraph)
    GraphSummary(nodeCount = internalGraph.getNodeCount,linkCount = internalGraph.getEdgeCount,connectedComponentCount = cc.getConnectedComponentsCount)
  }

 /** Display the referrer graph */
  def viz = internalGraph.display()

 /** Processes the specified web request by creating the specific node(s) and link in the referrer graph
   * @param newEvent the HTTP request to process
   */
   def processRequest(newEvent: WebRequest) = {
  	//Deal with HTTP redirection 302 statuses ????
    val referrer = newEvent.referrer
    referrer match {
      case None =>
        // There is no referrer, so we just update the node
        addNode(newEvent.url.toString, Some(newEvent.getSummary))
      case Some(ref) =>
        // There is a referrer, so we can update the link (from referrer to target)
        addLink(ref.toString, newEvent.url.toString, newEvent.getSummary)
    }
  }
  
  private def candidateHNCriteria(node:ReSurfNode):Boolean = {
  	VALID_HEADNODE_CONTENT_TYPES.contains(node.contentTypeMode.getOrElse("text/html")) && 
    	node.contentSizeAvg.getOrElse(Double.MaxValue) >= MIN_OBJECT_SIZE && 
    	node.getOutDegree >= MIN_EMBEDDED_OBJECTS &&
    	node.timeGapAvg.getOrElse(Duration.Top) >= MIN_PASS_THROUGH_DELAY && 
    	(!URI_KEYWORDS.exists(node.parametersMode.getOrElse("").contains))
  }
  
/** Get the nodes that are considered head nodes through the ReSurf methodology 
  * @return the head nodes
  */
   def getHeadNodes:Iterable[ReSurfNode] = {
  
    val nodes = internalGraph.getNodeSet[ReSurfNode].asScala
    var total_HN = Set[ReSurfNode]()
    
    val initial_HN = 
    	nodes.filter{node =>
    	//check to make sure that node has no referrer (parent node)
    	candidateHNCriteria(node) && node.getInDegree == 0
    }.toSet
		
		var last_HN = initial_HN
		total_HN ++= last_HN
		
		while(last_HN.size > 0){
		val children_of_last_HN = last_HN.flatMap{node => node.getChildNodeSet}.toSet
				
    last_HN = children_of_last_HN.filter{node =>
    	//check to make sure at least one of the referrer (parent) nodes is a head node
    	candidateHNCriteria(node) && total_HN.intersect(node.getParentNodeSet).size > 0
    }.toSet
    total_HN ++= last_HN
		}
    total_HN
  }
  
}