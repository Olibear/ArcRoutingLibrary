package arl.graph.impl;

import java.util.LinkedHashSet;

import arl.core.Arc;
import arl.vertex.impl.DirectedVertex;

public class DirectedGraph<V extends DirectedVertex, A extends Arc<V>> extends MutableGraph<V,A> {

	@Override
	public void addVertex(V v) {
		getVertices().add(v);
	}

	@Override
	public void addEdge(A e) {
		addToNeighbors(e);
		V toUpdate = e.getTail();
		toUpdate.setOutDegree(toUpdate.getOutDegree() + 1);
		toUpdate = e.getHead();
		toUpdate.setInDegree(toUpdate.getInDegree()+1);
	}

	@Override
	protected void addToNeighbors(A e) throws IllegalArgumentException{
		LinkedHashSet<V> temp = neighbors.get(e.getTail());
		if (temp == null)
			throw new IllegalArgumentException();
		temp.add(e.getHead());
	}




}
