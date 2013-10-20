package oarlib.graph.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import oarlib.core.Arc;
import oarlib.graph.util.Pair;
import oarlib.graph.util.UnmatchedPair;
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
		addToNeighbors(e);
		DirectedVertex toUpdate = e.getTail();
		toUpdate.setOutDegree(toUpdate.getOutDegree() + 1);
		toUpdate = e.getHead();
		toUpdate.setInDegree(toUpdate.getInDegree()+1);
		super.addEdge(e);
	}

	@Override
	protected void addToNeighbors(A e) throws IllegalArgumentException{
		neighbors.put(e.getTail(), new UnmatchedPair<DirectedVertex,A>(e.getHead(),e));
	}

	@Override
	public Collection<A> findEdges(Pair<DirectedVertex> endpoints) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LinkedHashMap<DirectedVertex, UnmatchedPair<DirectedVertex,A>> getNeighbors() {
		return neighbors;
	}




}
