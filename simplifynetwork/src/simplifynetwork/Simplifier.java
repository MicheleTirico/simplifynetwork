package simplifynetwork;

import java.io.IOException;

import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.GraphReplay;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;

public class Simplifier extends SinkAdapter {
    // to compare edge length
    protected static double EPS = 1.0e-6;

    protected Graph source;
    protected Graph simplifiedGraph;
    protected String idSource , idSimplifiedGraph ;

    // "Global" variables
    protected int edgeId = 0;
    protected Edge newEdge;
    protected static boolean storeCoordNodes ;

    public void setStoreCoordsNode ( boolean storeCoordNodes  ) {
    	 Simplifier.storeCoordNodes =storeCoordNodes ;
    }
    
    protected Simplifier(Graph source) {
        this.source = source;  
        idSource = source.getId();
        idSimplifiedGraph = source.getId() + "_sim" ;
        simplifiedGraph = new MultiGraph(idSimplifiedGraph);
        GraphReplay replay = new GraphReplay("replay");
        replay.addElementSink(simplifiedGraph);
        replay.replay(source);
        replay.removeElementSink(simplifiedGraph);
        for (Edge edge : source.getEachEdge()) {
            simplifiedGraph.getEdge(edge.getId()).addAttribute("length", GraphPosLengthUtils.edgeLength(edge));
        }
        for (Node node : source) {
        	if ( storeCoordNodes)
        		checkAndRemoveNode( node );
        	else 
        		checkAndAddNode(node.getId());
        }
        source.addElementSink(this);
    }

    public static Graph getSimplifiedGraph(Graph source , boolean store ) {
   	 	storeCoordNodes = store ;
        return new Simplifier(source).simplifiedGraph;
    }

    @Override
    public void edgeAdded(String sourceId, long timeId, String edgeId, String fromNodeId, String toNodeId, boolean directed) {
        String toRemove = null;
        if (simplifiedGraph.getNodeCount() == 1 && simplifiedGraph.getEdgeCount() == 1) toRemove = simplifiedGraph.getNode(0).getId();
        newEdge = source.getEdge(edgeId);
        checkAndAddNode(fromNodeId);
        checkAndAddNode(toNodeId);
        simplifiedGraph.addEdge(idSimplifiedGraph + this.edgeId++, fromNodeId, toNodeId).addAttribute("length", GraphPosLengthUtils.edgeLength(newEdge));
        checkAndRemoveNode(fromNodeId);
        checkAndRemoveNode(toNodeId);
        if (toRemove != null && simplifiedGraph.getNode(toRemove) != null) checkAndRemoveNode(toRemove);
        newEdge = null;
    }

    @Override
    public void edgeRemoved(String sourceId, long timeId, String edgeId) {
    	try {
	        Edge deleted = source.getEdge(edgeId);
	        String fromNodeId = deleted.getSourceNode().getId();
	        String toNodeId = deleted.getTargetNode().getId();
	        checkAndAddNode(fromNodeId);
	        checkAndAddNode(toNodeId);
	        removeEdgeWithLength(simplifiedGraph.getNode(fromNodeId), simplifiedGraph.getNode(toNodeId), GraphPosLengthUtils.edgeLength(deleted));
	        checkAndRemoveNode(fromNodeId);
	        checkAndRemoveNode(toNodeId);
    	} catch (NullPointerException e) {
    	//	e.printStackTrace();
    		// TODO: handle exception
		}
    }

    // helpers
    protected void checkAndRemoveNode( Node source ) {
        Node node = simplifiedGraph.getNode(source.getId());
        if (node.getDegree() == 2 && !node.getEdge(0).isLoop() && !node.getEdge(1).isLoop()) {
            Edge edge0 = node.getEdge(0);
            Edge edge1 = node.getEdge(1);
            Node neighbor0 = edge0.getOpposite(node);
            Node neighbor1 = edge1.getOpposite(node);
            Edge edge = simplifiedGraph.addEdge(idSimplifiedGraph + edgeId++, neighbor0, neighbor1);
            edge.addAttribute("length", edge0.getNumber("length") + edge1.getNumber("length"));
            simplifiedGraph.removeNode(node);
        } else if (node.getDegree() == 0) {
            simplifiedGraph.removeNode(node);
        }
        else {	 
            double[] sourceCoords = GraphPosLengthUtils.nodePosition(source);
            node.addAttribute("xyz", sourceCoords[0], sourceCoords[1], sourceCoords[2]);
        }
     }
    
    protected void checkAndRemoveNode(String nodeId) {
        Node node = simplifiedGraph.getNode(nodeId);
        if (node.getDegree() == 2 && !node.getEdge(0).isLoop() && !node.getEdge(1).isLoop()) {
            Edge edge0 = node.getEdge(0);
            Edge edge1 = node.getEdge(1);
            Node neighbor0 = edge0.getOpposite(node);
            Node neighbor1 = edge1.getOpposite(node);
            Edge edge = simplifiedGraph.addEdge(idSimplifiedGraph+ edgeId++, neighbor0, neighbor1);
            edge.addAttribute("length", edge0.getNumber("length") + edge1.getNumber("length"));
            simplifiedGraph.removeNode(node);
        } else if (node.getDegree() == 0) {
            simplifiedGraph.removeNode(node);
        }
    }

    protected void checkAndAddNode(String nodeId) {
        if (simplifiedGraph.getNode(nodeId) != null) return;
        Node simplifiedNode = simplifiedGraph.addNode(nodeId);
        Node sourceNode = source.getNode(nodeId);
        if (sourceNode.getDegree() == 1) return;
        Edge in0 = firstAllowed(sourceNode, null);
        double[] l0 = {0};
        Node neighbor0 = follow(in0, in0.getOpposite(sourceNode), l0);
        Edge in1 = firstAllowed(sourceNode, in0);
        double[] l1 = {0};
        Node neighbor1 = follow(in1, in1.getOpposite(sourceNode), l1);
        removeEdgeWithLength(neighbor0, neighbor1, l0[0] + l1[0]);
        simplifiedGraph.addEdge(idSimplifiedGraph + edgeId++, simplifiedNode, neighbor0).addAttribute("length", l0[0]);
        simplifiedGraph.addEdge(idSimplifiedGraph + edgeId++, simplifiedNode, neighbor1).addAttribute("length", l1[0]);
        // System.out.printf("%s -> %s %f ; %s -> %s %f%n", nodeId, neighbor0.getId(), l0[0], nodeId, neighbor1.getId(), l1[0]);  
        if ( storeCoordNodes) {
	        double[] sourceCoords = GraphPosLengthUtils.nodePosition(sourceNode);
	        simplifiedNode.addAttribute("xyz", sourceCoords[0], sourceCoords[1], sourceCoords[2]);
        }
    }

    protected Node follow(Edge in, Node current, double[] pathLength) {
        pathLength[0] = GraphPosLengthUtils.edgeLength(in);
        while (simplifiedGraph.getNode(current.getId()) == null) {
            in = firstAllowed(current, in);
            pathLength[0] += GraphPosLengthUtils.edgeLength(in);
            current = in.getOpposite(current);
        }
        return simplifiedGraph.getNode(current.getId());
    }

    protected Edge firstAllowed(Node node, Edge forbidden) {
        for (Edge edge : node) {
            if (edge != newEdge && edge != forbidden) return edge;
        }
        return null;
    }

    protected void removeEdgeWithLength(Node node0, Node node1, double length) {
        for (Edge edge : node0) {
            if (edge.getOpposite(node1) == node0 && Math.abs(edge.getNumber("length") - length) < EPS ) {
                simplifiedGraph.removeEdge(edge);
                return;
            }
        }
    }
    public static void main(String[] args) throws IOException{
        String pathIN = args[0], pathOUT = args[1];
        Graph g = new graphTool.ReadDGS(pathIN).getGraph();
        g = Simplifier.getSimplifiedGraph(g, true);
        g.write(pathOUT);

/**        
        FileSource fs = new FileSourceDGS();
        Graph g = new MultiGraph("g");
        g.setStrict(false);
//      fs.addSink(g);
        fs.readAll(path);
    	System.out.println(g.getNodeCount());
        Simplifier s = new Simplifier(g) ;
        g.display(false);
    	System.out.println(g.getNodeCount());
   **/
    }
}
