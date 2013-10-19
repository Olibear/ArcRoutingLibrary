package arl.graph.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import arl.core.Edge;
import arl.graph.util.Pair;
import arl.vertex.impl.UndirectedVertex;
/**
 * First attempts at an Undirected Graph.
 * @author Oliver
 *
 * @param <V> - Vertex Class
 * @param <E> - Edge Class
 */
public class UndirectedGraph<E extends Edge> extends MutableGraph<UndirectedVertex,E>{
	/**
	 * When adding an edge, add references to the neighbor set
	 * @param e
	 */
	@Override
	protected void addToNeighbors (E e) throws IllegalArgumentException
	{
		LinkedHashSet<UndirectedVertex> temp = neighbors.get(e.getEndpoints().getFirst());
		if (temp == null)
			throw new IllegalArgumentException();
		temp.add(e.getEndpoints().getSecond());
		temp = neighbors.get(e.getEndpoints().getSecond());
		temp.add(e.getEndpoints().getFirst());
		
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
	@Override
	public Collection<E> findEdges(Pair<UndirectedVertex> endpoints) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public LinkedHashMap<UndirectedVertex, LinkedHashSet<UndirectedVertex>> getNeighbors() {
		// TODO Auto-generated method stub
		return null;
	}

}
