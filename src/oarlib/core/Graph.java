package oarlib.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.util.Pair;

/**
 * Graph abstraction.  Provides most general contract for all Graph objects.
 * @author oliverlum
 *
 */
public abstract class Graph<V extends Vertex,E extends Link<V>> {

	private int vidCounter = 1; //for assigning internal ids of vertices
	private int eidCounter = 1;
	public enum Type{
		DIRECTED,
		UNDIRECTED,
		MIXED,
		WINDY
	}
	protected int assignVertexId() //returns the current vidCounter, and increments 
	{
		vidCounter++;
		return vidCounter - 1;
	}
	protected int assignEdgeId() //returns the current eidCounter, and increments
	{
		eidCounter++;
		return eidCounter - 1;
	}
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
	 * Throws an InvalidEndpointsException if the endpoints haven't yet been added to the graph.
	 * @param e - edge to be added
	 */
	public abstract void addEdge(E e) throws InvalidEndpointsException;
	/**
	 * Provides a means of getting a by value copy of this graph
	 * @return a deep copy of the graph
	 */
	public abstract Graph<V, E> getDeepCopy();
	/**
	 * Looks for edges between the two provided endpoints, and returns them in a collection.
	 * @return a collection of edges directly connecting the two vertices
	 */
	public abstract List<E> findEdges(Pair<V> endpoints);
	/**
	 * @return - a hash map that has guids as keys to the vertices
	 */
	public abstract HashMap<Integer,V> getGlobalVertexMap();
	/**
	 * @return - a hash map that has ids as keys to the vertices
	 */
	public abstract HashMap<Integer,V> getInternalVertexMap();
	/**
	 * @return - a hash map that has guids as keys to the edges
	 */
	public abstract HashMap<Integer,E> getGlobalEdgeMap();
	/**
	 * @return - a hash map that has ids as keys to the edges
	 */
	public abstract HashMap<Integer,E> getInternalEdgeMap();
	/**
	 * @return - the type that this graph structure represents
	 */
	public abstract Graph.Type getType();
}
