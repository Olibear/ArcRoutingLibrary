package oarlib.graph.impl;

import java.util.HashSet;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;

/**
 * A mutable graph.  Perf will be worse than a finalized one, but allows for experimentation.
 * @author Oliver
 *
 */
public abstract class MutableGraph<V extends Vertex, E extends Link<V>> extends Graph<V,E>{

	private HashSet<V> mVertices;
	private HashSet<E> mEdges;
	
	protected MutableGraph(){
		mVertices = new HashSet<V>();
		mEdges = new HashSet<E>();
	}
	
	//==============================
	//Graph overrides
	//==============================
	
	@Override
	public HashSet<V> getVertices() {
		return mVertices;
	}

	@Override
	public HashSet<E> getEdges() {
		return mEdges;
	}

	@Override
	public Graph<V,E> getDeepCopy() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void addEdge(E e) {
		mEdges.add(e);
	}
	@Override
	public void addVertex(V v) {
		mVertices.add(v);
	}
	/**
	 * Intended to be called as a helper method to add edge to help update references to neighbors
	 * @param e - edge containing reference info to be added
	 * @throws IllegalArgumentException - if either of the endpoints of the  edge are null.
	 */
	protected abstract void addToNeighbors(E e) throws IllegalArgumentException;


}
