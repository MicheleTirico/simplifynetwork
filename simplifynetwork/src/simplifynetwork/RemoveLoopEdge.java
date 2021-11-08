package simplifynetwork;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import graphTool.GraphTool;

public class RemoveLoopEdge {

	private Graph source  , output ;
	private String id ;
	
	public RemoveLoopEdge ( String id , Graph source ) {
		this.id  = id ;
		this.source = source ;
		compute() ;
	}
	
	private void compute () {
		output = GraphTool.getGraphReplay(id, source, true ,true ) ;
		boolean noSup = false ;
		while ( noSup == false ) {
			int i = 0 ;
			for ( Edge e : output.getEachEdge() ) {
				Node n0 = e.getNode0() , n1 = e.getNode1() ;
				if( n0.equals(n1)) {
					output.removeEdge(e);
					i++;
				}
			}
			if ( i == 0 ) 
				noSup = true ;	//		System.out.println(i +" " + "ciao "+ simplGr1.getEdgeCount());
		}
	}
	
	public Graph getGraph ( ) {
		return output; 
	}
}