package arl.graph.impl;

import java.util.LinkedHashSet;

import arl.core.Edge;
import arl.vertex.impl.UndirectedVertex;
/**
 * First attempts at an Undirected Graph.
 * @author Oliver
 *
 * @param <V> - Vertex Class
 * @param <E> - Edge Class
 */
public class UndirectedGraph<V extends UndirectedVertex, E extends Edge<V>> extends MutableGraph<V,E>{
	/**
	 * When adding an edge, add references to the neighbor set
	 * @param e
	 */
	@Override
	protected void addToNeighbors (E e) throws IllegalArgumentException
	{
		LinkedHashSet<V> temp = neighbors.get(e.getEndpoints().getFirst());
		if (temp == null)
			throw new IllegalArgumentException();
		temp.add(e.getEndpoints().getSecond());
		temp = neighbors.get(e.getEndpoints().getSecond());
		temp.add(e.getEndpoints().getFirst());
		
	}
	@Override
	public void addVertex(V v) {
		getVertices().add(v);
	}

	@Override
	public void addEdge(E e) {
		addToNeighbors(e);
		V toUpdate = e.getEndpoints().getFirst();
		toUpdate.setDegree(toUpdate.getDegree()+1);
		toUpdate = e.getEndpoints().getSecond();
		toUpdate.setDegree(toUpdate.getDegree()+1);
	}

}
