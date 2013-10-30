package oarlib.graph.impl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;
/**
 * First attempts at an Undirected Graph.
 * @author Oliver
 *
 * @param <V> - Vertex Class
 * @param <E> - Edge Class
 */
public class UndirectedGraph extends MutableGraph<UndirectedVertex,Edge>{
	//constructors
	public UndirectedGraph(){
		super();
	}
	
	
	@Override
	public void addVertex(UndirectedVertex v) {
		super.addVertex(v);
	}

	@Override
	public void addEdge(Edge e) throws InvalidEndpointsException{
		Pair<UndirectedVertex> endpoints = e.getEndpoints();
		endpoints.getFirst().addToNeighbors(endpoints.getSecond(), e);
		endpoints.getSecond().addToNeighbors(endpoints.getFirst(), e);
		UndirectedVertex toUpdate = endpoints.getFirst();
		toUpdate.setDegree(toUpdate.getDegree()+1);
		toUpdate = e.getEndpoints().getSecond();
		toUpdate.setDegree(toUpdate.getDegree()+1);
		super.addEdge(e);
	}
	
	@Override
	public List<Edge> findEdges(Pair<UndirectedVertex> endpoints) {
		UndirectedVertex first = endpoints.getFirst();
		HashMap<UndirectedVertex, ArrayList<Edge>> firstNeighbors = first.getNeighbors();
		return firstNeighbors.get(endpoints.getSecond());
	}

}
