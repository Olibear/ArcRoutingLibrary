package oarlib.graph.impl;

import java.util.Collection;

import oarlib.core.Arc;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;
/**
 * Reperesentation of a Directed Graph; that is, it can only contain arcs, and directed vertices
 * @author Oliver
 *
 * @param <A> Arc that this graph will use
 */
public class DirectedGraph<A extends Arc> extends MutableGraph<DirectedVertex,A> {

	//constructors
	public DirectedGraph(){
		super();
	}
	
	@Override
	public void addVertex(DirectedVertex v) {
		super.addVertex(v);
	}

	@Override
	public void addEdge(A e) {
		e.getTail().addToNeighbors(e.getHead(), e);
		DirectedVertex toUpdate = e.getTail();
		toUpdate.setOutDegree(toUpdate.getOutDegree() + 1);
		toUpdate = e.getHead();
		toUpdate.setInDegree(toUpdate.getInDegree()+1);
		super.addEdge(e);
	}

	@Override
	public Collection<A> findEdges(Pair<DirectedVertex> endpoints) {
		// TODO Auto-generated method stub
		return null;
	}

}
