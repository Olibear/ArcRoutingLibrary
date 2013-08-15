package arl.core;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Graph abstraction.  Provides most general contract for all Graph objects.
 * @author oliverlum
 *
 */
public abstract class Graph<V extends Vertex,E extends Link> {
	/**
	 * Getter for the vertices.
	 * @return a Collection of vertices belonging to this graph
	 */
	public abstract Collection<V> getVertices();
	/**
	 * Getter for the edges.
	 * @return a Collection of edges belonging to this graph
	 */
	public abstract Collection<E> getEdges();
	/**
	 * To add a vertex to the graph.
	 * @param v - vertex to be added
	 */
	public abstract void addVertex(V v);
	/**
	 * To add an edge to the graph.  This updates the degrees of the vertices, and throws to the specific implementation of the graph.
	 * @param e - edge to be added
	 */
	public abstract void addEdge(E e);
	/**
	 * Provides a means of getting a by value copy of this graph
	 * @return a deep copy of the graph
	 */
	public abstract Graph<V, E> getDeepCopy();
}
