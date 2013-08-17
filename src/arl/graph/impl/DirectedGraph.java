package arl.graph.impl;

import java.util.LinkedHashSet;

import arl.core.Arc;
import arl.vertex.impl.DirectedVertex;
/**
 * Reperesentation of a Directed Graph; that is, it can only contain arcs, and directed vertices
 * @author Oliver
 *
 * @param <A> Arc that this graph will use
 */
public class DirectedGraph<A extends Arc> extends MutableGraph<DirectedVertex,A> {

	@Override
	public void addVertex(DirectedVertex v) {
		super.addVertex(v);
	}

	@Override
	public void addEdge(A e) {
		addToNeighbors(e);
		DirectedVertex toUpdate = e.getTail();
		toUpdate.setOutDegree(toUpdate.getOutDegree() + 1);
		toUpdate = e.getHead();
		toUpdate.setInDegree(toUpdate.getInDegree()+1);
		super.addEdge(e);
	}

	@Override
	protected void addToNeighbors(A e) throws IllegalArgumentException{
		LinkedHashSet<DirectedVertex> temp = neighbors.get(e.getTail());
		if (temp == null)
			throw new IllegalArgumentException();
		temp.add(e.getHead());
	}




}
