package arl.graph.impl;

import java.util.ArrayList;
import java.util.Collection;

import arl.core.Edge;
import arl.core.Graph;
import arl.core.Vertex;

/**
 * A mutable graph.  Perf will be worse than a finalized one, but allows for experimentation.
 * @author Oliver
 *
 */
public abstract class MutableGraph<V extends Vertex, E extends Edge> extends Graph<V,E>{

	private ArrayList<V> mVertices;
	private ArrayList<E> mEdges;
	@Override
	public Collection<V> getVertices() {
		return mVertices;
	}

	@Override
	public Collection<E> getEdges() {
		return mEdges;
	}

	@Override
	public Graph<V,E> getDeepCopy() {
		// TODO Auto-generated method stub
		return null;
	}


}
