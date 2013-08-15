package arl.graph.impl;

import java.util.LinkedHashSet;

import arl.core.Arc;
import arl.vertex.impl.DirectedVertex;

public class DirectedGraph<A extends Arc> extends MutableGraph<DirectedVertex,A> {

	@Override
	public void addVertex(DirectedVertex v) {
		getVertices().add(v);
	}

	@Override
	public void addEdge(A e) {
		addToNeighbors(e);
		DirectedVertex toUpdate = e.getTail();
		toUpdate.setOutDegree(toUpdate.getOutDegree() + 1);
		toUpdate = e.getHead();
		toUpdate.setInDegree(toUpdate.getInDegree()+1);
	}

	@Override
	protected void addToNeighbors(A e) throws IllegalArgumentException{
		LinkedHashSet<DirectedVertex> temp = neighbors.get(e.getTail());
		if (temp == null)
			throw new IllegalArgumentException();
		temp.add(e.getHead());
	}




}
