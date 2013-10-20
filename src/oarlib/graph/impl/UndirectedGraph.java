package oarlib.graph.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import oarlib.core.Edge;
import oarlib.graph.util.Pair;
import oarlib.graph.util.UnmatchedPair;
import oarlib.vertex.impl.UndirectedVertex;
/**
 * First attempts at an Undirected Graph.
 * @author Oliver
 *
 * @param <V> - Vertex Class
 * @param <E> - Edge Class
 */
public class UndirectedGraph<E extends Edge> extends MutableGraph<UndirectedVertex,E>{
	//constructors
	public UndirectedGraph(){
		super();
	}
	
	
	@Override
	public void addVertex(UndirectedVertex v) {
		super.addVertex(v);
	}

	@Override
	public void addEdge(E e) {
		addToNeighbors(e);
		UndirectedVertex toUpdate = e.getEndpoints().getFirst();
		toUpdate.setDegree(toUpdate.getDegree()+1);
		toUpdate = e.getEndpoints().getSecond();
		toUpdate.setDegree(toUpdate.getDegree()+1);
		super.addEdge(e);
	}
	/**
	 * When adding an edge, add references to the neighbor set
	 * @param e
	 */
	@Override
	protected void addToNeighbors (E e) throws IllegalArgumentException
	{
		//if either of the vertices aren't in the vertex set yet, then throw an error.
		if(!(this.getVertices().contains(e.getEndpoints().getFirst()) || this.getVertices().contains(e.getEndpoints().getSecond())))
			throw new IllegalArgumentException();
		neighbors.put(e.getEndpoints().getFirst(), new UnmatchedPair<UndirectedVertex, E>(e.getEndpoints().getSecond(), e));
		neighbors.put(e.getEndpoints().getSecond(), new UnmatchedPair<UndirectedVertex, E>(e.getEndpoints().getFirst(), e));
	}
	
	@Override
	public Collection<E> findEdges(Pair<UndirectedVertex> endpoints) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public LinkedHashMap<UndirectedVertex, UnmatchedPair<UndirectedVertex,E>> getNeighbors() {
		return neighbors;
	}

}
