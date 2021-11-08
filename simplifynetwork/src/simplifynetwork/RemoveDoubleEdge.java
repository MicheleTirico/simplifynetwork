package simplifynetwork;

import java.util.ArrayList;
import java.util.Collection;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class RemoveDoubleEdge  extends graphTool.GraphTool {
	
	private Graph source , rem ; 
	private double delta ;
	private String id ; 
	
	public RemoveDoubleEdge ( Graph source , double delta ) {
		this.source = source ;
		this.delta = delta ;
	}
	
	public RemoveDoubleEdge (String id, Graph source , double delta ) {
		this.id = id;
		this.source = source ;
		this.delta = delta ;
	}
	
	public void compute ( boolean removeAll  ) {
		rem = getGraphReplay (id , source, true, true);
		int p = 0 ;
		for ( Edge e : rem.getEachEdge() ) {
			Node n0 = e.getNode0() , n1 = e.getOpposite(n0);
			Collection<Edge> edges = n0.getEdgeSet();
			ArrayList<Edge> listMultiEdges = new ArrayList<Edge> () ;
			for ( Edge ed : edges ) {
				if ( ed.getOpposite(n0).equals(n1) )
					listMultiEdges.add(ed);
			}
			if ( listMultiEdges.size() > 1 ) {
//				if ( listMultiEdges.size()  > 2  ) 		System.out.println(listMultiEdges);
				double len0 = listMultiEdges.get(0).getAttribute("length") , len1 = listMultiEdges.get(1).getAttribute("length");
				if ( Math.max( len0,len1) / Math.min(len0,len1) -1  <= delta  && len0 != len1) 	{
					rem.removeEdge(listMultiEdges.get(0));
				}
			}
		}
	}
	
	public Graph getRemGraph ( ) {
		return rem;
	}
}
