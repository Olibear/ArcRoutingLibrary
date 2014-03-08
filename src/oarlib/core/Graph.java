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
	public int getEidCounter()
	{
		return eidCounter;
	}
	public int getVidCounter()
	{
		return vidCounter;
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
	 * Erases all edges in the graph, leaving only the vertices behind
	 */
	public abstract void clearEdges();
	/**
	 * Resets the Edge counter / id assignment to start over.
	 */
	protected void resetEdgeCounter()
	{
		eidCounter = 1;
	}
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
	 * A more notationally elegant way of adding an edge to a graph, it will create a new edge from vertex i to vertex j,
	 * with the appropriate cost and description.  If this is a mixed graph, it will default to adding an edge, but look for
	 * a version that takes directedness as an argument
	 * @param i - add edge from vertex i
	 * @param j - add edge to vertex j
	 * @param desc - description for the edge
	 * @param cost - cost of traversing the edge
	 */
	public abstract void addEdge(int i, int j, String desc, int cost) throws InvalidEndpointsException;
	/**
	 * To remove an edge from the graph.  This updates the degrees of the vertices, and throws to the specific implementation of the graph.
	 * Throws an IllegalArgumentException if the edge isn't a member of the edge collection belonging to the graph.
	 * @param e - edge to be removed from the graph
	 * @throws IllegalArgumentException
	 */
	public abstract void removeEdge(E e) throws IllegalArgumentException;
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
	/**
	 * Factory method for generating an edge.
	 * @param i - first endpoint index for the edge created
	 * @param j - second endpoint index for the edge created
	 * @param desc - description of edge to be created
	 * @param cost - cost of edge to be created
	 * @return - an instance of an edge satisfying these properties
	 * @throws InvalidEndpointsException - if i or j > number of vertices
	 */
	public abstract E constructEdge(int i, int j, String desc, int cost) throws InvalidEndpointsException;
	/**
	 * Factory method for generating a vertex.
	 * @param desc - description for the vertex created
	 * @return - a vertex satisfying these properties
	 */
	public abstract V constructVertex(String desc);
}
