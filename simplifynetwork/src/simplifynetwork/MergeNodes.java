package simplifynetwork;

import java.util.Collection;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;

public class MergeNodes extends graphTool.GraphTool {

	private Graph source , merGraph   , moveGraph ;
	private double dist ;
	private int  idNodeInt = 0 , idEdgeInt = 0 ;
	private String id ;
	
	public MergeNodes ( String id , Graph source , double dist) {
		this.id  = id ;
		this.source = source ;
		this.dist = dist ;
		merGraph = new SingleGraph(id);
		merGraph.setStrict(false);
		computeMerge() ;
	}
	
	public void computeMove (   ) {	
		moveGraph = getGraphReplay(id, source, true, false);
		for ( Node n : moveGraph .getEachNode() ) 
			for ( Node neig : getNeigs(n) ) {
				double d = getDistGeom(n, neig);
				if ( d <  dist ) {		
					double [] nCoords = GraphPosLengthUtils.nodePosition(n);
					neig.addAttribute("xyz", nCoords[0], nCoords[1] , 0 );
				}
			}
	}
	
	public void  computeMerge () {
		merGraph = compute1Step (source);
		int p = 0 ;
		int numNodesBefore = merGraph.getNodeCount() , numNodes = 0 ;
	
		while (  numNodesBefore != numNodes && p < 100 ) {
			numNodesBefore = merGraph.getNodeCount();
			merGraph = compute1Step (merGraph);
			numNodes = merGraph.getNodeCount();
		}
	}	
 	
	private Graph compute1Step ( Graph source ) {   
		Graph mer = getGraphReplay(id, source, true, false);   
		for ( Edge ed : mer.getEachEdge() ) {		
				Node n0 = ed.getNode0() , n1 = ed.getOpposite(n0);
				double d = getDistGeom(n0, n1);
				if ( d <  dist ) {
					double [] 	nCoords0 = GraphPosLengthUtils.nodePosition(n0) ,
								nCoords1 = GraphPosLengthUtils.nodePosition(n1) ,
								nCoords  = { (nCoords0[0]+nCoords1[0])/2 , (nCoords0[1]+nCoords1[1]) /2 } ;// nCoords0 ; 
					Collection<Node> neigs0 = getNeigs(n0);
					Collection<Node> neigs1 = getNeigs(n1);
					
					Node newNode = mer.addNode("n"+ Integer.toString(idNodeInt++)) ;
					newNode.addAttribute("xyz", nCoords[0], nCoords[1] , 0 );

					for ( Node n : neigs0 )
						mer.addEdge("n"+Integer.toString(idEdgeInt++), n, newNode).addAttribute("new", 1);
					
					for ( Node n : neigs1 )
						mer.addEdge("n"+Integer.toString(idEdgeInt++), n, newNode).addAttribute("new", 1);
		
					mer.removeNode(n1);
					mer.removeNode(n0);	
				}
			ed.addAttribute("length", d);	
		} 
		return mer ;
	}
	
	public Graph getGraphMove ( ) {
		return moveGraph ;
	}
	
	public Graph getGraphMerge () {
		return merGraph ;
	}

//	public void compute_6 ( ) {
//		mer = g ;
//		int p = 0 ;
//		Collection<Edge> edgeToVisited = mer.getEdgeSet();
//		Collection<Node[]> edgeToAdd = new HashSet<> ();
//		
////		Map <Node , ArrayList<Node> > edgeToAdd = new HashMap<Node,ArrayList<Node>> () ;
//		for ( Edge ed : edgeToVisited ) {
//	//		System.out.println(ed + " " + p++);
//			Node n0 = ed.getNode0() , n1 = ed.getOpposite(n0);
//			double d = getDistGeom(n0, n1);
//			if ( d <  dist ) {
//		//		System.out.println("ciao");
//				Collection<Node> neigs = getNeigs(n1);
//			//	neigs.remove(n0);
//				
//				for ( Node n1neig : neigs ) {
//					if ( ! n1neig.equals(n0)  ) {
//						System.out.println(n0 +" " + n1neig+ " " + neigs);
////						ArrayList<Node> list = edgeToAdd.get(n0) ;
////						list.add(n1neig);
////						edgeToAdd.put(n0, list);
//						edgeToAdd.add(new Node[] {n0, n1neig}) ;
////						mer.addEdge("n"+Integer.toString(idEdgeInt++), n0, n1neig);
//					}
//				}
//				mer.removeEdge(ed);
//				
//			}
//		 }
//		for (Node [] edges :edgeToAdd) {
//			mer.addEdge("n"+Integer.toString(idEdgeInt++), edges[0], edges[1]);
//			
//		}
//	}
//	
//	public void compute_5 ( ) {
//		for ( Edge edG : g.getEachEdge() ) {
//			Node noG0 = edG.getNode0() , noG1 = edG.getOpposite(noG0);
//		
//		
//			double d = getDistGeom(noG0, noG1);
//			if ( d <  dist ) {
//				Collection<Node> neigs = getNeigs(noG1);
//
//				for ( Node neigG1 : neigs ) {
//					Node neigMer1 = neigG1.getAttribute("nMer");
//					if ( neigMer1 == null ) {
//
//					}
//						
//						
//					 
//				}
////				Node noMer1 = noG1.getAttribute("nMer");
////				if ( noMer1 == null ) {
////					noMer1 = mer.addNode(noG1.getId() /*Integer.toString(idNodeInt++)*/) ;
////					double [] noG1Coords = GraphPosLengthUtils.nodePosition(noG1);
////					noMer1.addAttribute("xyz", noG1Coords[0],noG1Coords[1],0);
////					noG1.addAttribute("nMer", noMer1);
////				}
//			} else {
//				Node noMer0 ;
//				try {
//					noMer0 = mer.addNode(noG0.getId() /*Integer.toString(idNodeInt++)*/) ;
//					double [] noG0Coords = GraphPosLengthUtils.nodePosition(noG0);
//					noMer0.addAttribute("xyz", noG0Coords[0],noG0Coords[1],0);
//					noG0.addAttribute("nMer", noMer0);
//				} catch (IdAlreadyInUseException e) {
//					noMer0 = noG0.getAttribute("nMer");
//				}
//				
//				Node noMer1 ;
//				try {
//					noMer1 = mer.addNode(noG1.getId() /*Integer.toString(idNodeInt++)*/) ;
//					double [] noG1Coords = GraphPosLengthUtils.nodePosition(noG1);
//					noMer1.addAttribute("xyz", noG1Coords[0],noG1Coords[1],0);
//					noG1.addAttribute("nMer", noMer1);
//				} catch (IdAlreadyInUseException e) {
//					noMer1 = noG1.getAttribute("nMer");
//				}
//				
////				System.out.println(noMer0 + " " + noMer1);
//				mer.addEdge(Integer.toString(idEdgeInt++),noMer0, noMer1);
//			}
//		}
//	}
//	public void compute_04 ( ) {
//		mer = g ;
//		int p = 0 ;
//		System.out.println(g.getEdgeCount());
//		
//		for ( Edge e : mer.getEachEdge() ) {
//			System.out.println(e + " " + p++);
//			Node n0 = e.getNode0() , n1 = e.getOpposite(n0);
//			double d = getDistGeom(n0, n1);
//			if ( d <  dist ) {
//				Collection<Node> neigs = getNeigs(n1);
//				for ( Node n1neig : neigs ) {
//					if ( !n1neig.equals(n0) ) {
//						try {
//							mer.addEdge("n"+Integer.toString(idEdgeInt++), n0, n1neig);
//						} catch (EdgeRejectedException ex) {
//							// TODO: handle exception
//						}
//					}
//				}
//				mer.removeEdge(e);
//			}
//		}
//		
//	}
//	
//	public void compute_03 ( ) {
//		mer = g ;
//		int p = 0 ;
//		for ( Node n : mer.getEachNode() ) {
//			System.out.println(p++);
//			Iterator<Edge> itEdge = n.getEdgeIterator();
//			while ( itEdge.hasNext() ) {
//				Edge eNext = itEdge.next();
//				Node neig = eNext.getOpposite(n);
//				double d = getDistGeom(n, neig);
//				if ( d <  dist ) {
//					Collection<Node> neigs = getNeigs(neig);
//					for ( Node neig2 :neigs ) {
//						if ( ! n.equals(neig2) ) {
//							try {
//								mer.addEdge("n"+Integer.toString(idEdgeInt++), n, neig2);
////								System.out.println("ciao");
//								mer.removeEdge(eNext);
//							} catch (Exception e) {
//								// TODO: handle exception
//							}
//						}
//					}
//				}
//			}
//		}
//	}
//	
//	public void compute_2 ( ) {
////		mer = this.source ;       
//		GraphReplay replay = new GraphReplay("replay");
//	    replay.addElementSink(mer);
//	    replay.replay(source);
//	    replay.removeElementSink(mer);
//	        
//	    for ( Node nSource : source.getEachNode() ) {
//	    	double [] nCoords = GraphPosLengthUtils.nodePosition(nSource);
//	    	Node nMer = mer.getNode(nSource.getId());
//	    	nMer.addAttribute("xyz", nCoords[0], nCoords[1] , 0 );
//	    }
//		System.out.println("g   " + source.getEdgeCount());
//		System.out.println("mer " + mer.getEdgeCount());
//
//		Set<Node> setNodeToRemove = new HashSet<Node>();
//		Set<Edge> setEdgeToRemove = new HashSet<Edge>();		
//		for ( Edge ed : mer.getEachEdge() ) {
//			Node n0 = ed.getNode0() , n1 = ed.getOpposite(n0);
//			double d = getDistGeom(n0, n1);
//			if ( d <  dist ) {
//				double [] nCoords = GraphPosLengthUtils.nodePosition(n0);
//				n1.addAttribute("xyz", nCoords[0], nCoords[1] , 0 );
//				setEdgeToRemove.add(ed);
//			}
//			
//		}
//	 	
//		setEdgeToRemove.stream().forEach(e -> mer.removeEdge(e));
//
//	}


	 
	
//	
 
//	
//	public void compute_02 () {
//	
//		ArrayList<Node> nodeVisitedG = new  ArrayList<Node> () ;
//		for ( Node nG : source.getEachNode()) {
//			
//			if ( ! nodeVisitedG.contains(nG)) {
//				nodeVisitedG.add(nG);
//
//				Node nMer = mer.addNode(Integer.toString(idNodeInt++) /*nG.getId()*/ ) ;
//				double [] nGcoords = GraphPosLengthUtils.nodePosition(nG);
//				nMer.addAttribute("xyz", nGcoords[0], nGcoords[1] , 0 );
//				Iterator<Node> itNeig = nG.getNeighborNodeIterator();
//				while( itNeig.hasNext() ) {
//					Node neigG = itNeig.next();
//					double d = getDistGeom(nG, neigG);
//					if ( d < dist ) {
//						Collection<Node> neigs = getNeigs(nG);
//						for ( Node neigNeigG : neigs) {
//							if ( ! nodeVisitedG.contains(neigNeigG)) {
//								Node neigMer = mer.addNode(Integer.toString(idNodeInt++)/* neigNeigG.getId() */) ;
//								double [] neigNeigGcoords = GraphPosLengthUtils.nodePosition(neigNeigG);
//								neigMer.addAttribute("xyz", neigNeigGcoords[0], neigNeigGcoords[1] , 0 );
//								mer.addEdge(Integer.toString(idEdgeInt++), neigMer, nMer);
//								nodeVisitedG.add(neigNeigG);
//							} else {
//								nodeVisitedG.add(neigG);
//								
//							}
//						}
//					} else {
//						if ( ! nodeVisitedG.contains(neigG)) {
//							double [] neigNeigGcoords = GraphPosLengthUtils.nodePosition(neigG);
//						//	Node neigMer = mer.addNode(Integer.toString(idNodeInt++));
//						//	neigMer.addAttribute("xyz", neigNeigGcoords[0], neigNeigGcoords[1] , 0 );
//						//	mer.addEdge(Integer.toString(idEdgeInt++), neigMer, nMer);
//						}
//					}
//					
//				}
//			}
//		}
//	}
	
}
