package arl.graph.util;

import arl.core.Graph;
import arl.core.Route;
import arl.core.Vertex;

public class CommonAlgorithms {

	/**
	 * Fleury's algorithm for determining an Euler tour through an Eulerian graph.
	 * @param eulerianGraph - an eulerian graph on which to construct the tour 
	 * @return a Route object containing the tour.
	 * @throws IllegalArgumentException if the graph passed in is not Eulerian.
	 */
	public static Route tryFleury(Graph eulerianGraph) throws IllegalArgumentException{
		if (!isEulerian(eulerianGraph))
			throw new IllegalArgumentException();
		//TODO: Fleury's
		eulerianGraph.
		return null;
	}
	public static boolean isEulerian (DirectedGraph graph)
	{
		for(Vertex v: graph.getVertices())  
		{
			if (v.getInDegree() != v.getOutDegree())
				return false;
		}
		return true;
	}
	public static boolean isEulerian(UndirectedGraph graph) 
	{
		for (Vertex v:graph.getVertices()) 
		{
			if (v.getDegree() % 2 == 1)
				return false;
		}
		return true;
	}

}
