package arl.graph.util;

import arl.core.Arc;
import arl.core.Edge;
import arl.core.Graph;
import arl.core.Route;
import arl.core.Vertex;
import arl.graph.impl.DirectedGraph;
import arl.graph.impl.UndirectedGraph;
import arl.vertex.impl.DirectedVertex;
import arl.vertex.impl.UndirectedVertex;

public class CommonAlgorithms {

	/**
	 * Fleury's algorithm for determining an Euler tour through an Eulerian graph.
	 * @param eulerianGraph - an eulerian graph on which to construct the tour 
	 * @return a Route object containing the tour.
	 * @throws IllegalArgumentException if the graph passed in is not Eulerian.
	 */
	public static Route tryFleury(DirectedGraph<DirectedVertex, Arc<DirectedVertex>> eulerianGraph) throws IllegalArgumentException{
		if (!isEulerian(eulerianGraph))
			throw new IllegalArgumentException();
		//TODO: Fleury's
		return fleury(eulerianGraph);
	}
	public static Route tryFleury(UndirectedGraph<UndirectedVertex, Edge<UndirectedVertex>> eulerianGraph) throws IllegalArgumentException{
		if (!isEulerian(eulerianGraph))
			throw new IllegalArgumentException();
		return fleury(eulerianGraph);
	}
	public static Route fleury()
	{
		//TODO: Fleury's
		return null;
	}
	public static boolean isEulerian (DirectedGraph<DirectedVertex, Arc<DirectedVertex>> graph)
	{
		for(DirectedVertex v: graph.getVertices())  
		{
			if (v.getInDegree() != v.getOutDegree())
				return false;
		}
		return true;
	}
	public static boolean isEulerian(UndirectedGraph<UndirectedVertex, Edge<UndirectedVertex>> graph) 
	{
		for (UndirectedVertex v:graph.getVertices()) 
		{
			if (v.getDegree() % 2 == 1)
				return false;
		}
		return true;
	}

}
