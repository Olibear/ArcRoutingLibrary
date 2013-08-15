package arl.graph.util;

import arl.core.Arc;
import arl.core.Edge;
import arl.core.Link;
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
	public static Route tryFleury(DirectedGraph<Arc> eulerianGraph) throws IllegalArgumentException{
		if (!isEulerian(eulerianGraph))
			throw new IllegalArgumentException();
		//TODO: Fleury's
		return fleury(eulerianGraph);
	}
	public static Route tryFleury(UndirectedGraph<Edge> eulerianGraph) throws IllegalArgumentException{
		if (!isEulerian(eulerianGraph))
			throw new IllegalArgumentException();
		return fleury(eulerianGraph);
	}
	/**
	 * business logic for Fleury's algorithm
	 * @return the Eulerian cycle
	 */
	private static Route fleury()
	{
		return null;
	}
	/**
	 * FindRoute algorithm (alternative to Fleury's given in Dussault et al. Plowing with Precedence
	 * @return the Eulerian cycle
	 */
	public static Route findRoute()
	{
		return null;
	}
	/**
	 * Checks to see if the directed graph is weakly connected
	 * @return true if the graph is  weakly connected, false oth.
	 */
	public static boolean isWeaklyConnected() 
	{
		return false;
	}
	/**
	 * Checks to see if the directed graph is strongly connected
	 * @return true if the graph is strongly  connected, false oth.
	 */
	public static boolean isStronglyConnected()
	{
		return false;
	}
	/**
	 * Checks to see if the undirected graph is connected
	 * @return true if the graph is connected, false oth.
	 */
	public static boolean isConnected()
	{
		return false;
	}
	public static boolean isEulerian (DirectedGraph<Arc> graph)
	{
		for(DirectedVertex v: graph.getVertices())  
		{
			if (v.getInDegree() != v.getOutDegree())
				return false;
		}
		return true;
	}
	public static boolean isEulerian(UndirectedGraph<Edge> graph) 
	{
		for (UndirectedVertex v:graph.getVertices()) 
		{
			if (v.getDegree() % 2 == 1)
				return false;
		}
		return true;
	}

}
