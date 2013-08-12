package arl.core;

import java.util.Collection;

/**
 * Graph abstraction.  Provides most general contract for all Graph objects.
 * @author oliverlum
 *
 */
public abstract class Graph<Vertex,Edge> {
	/**
	 * Getter for the vertices.
	 * @return a Collection of vertices belonging to this graph
	 */
	public abstract Collection<Vertex> getVertices();
	/**
	 * Getter for the edges.
	 * @return a Collection of edges belonging to this graph
	 */
	public abstract Collection<Edge> getEdges();
	/**
	 * To add a vertex to the graph.
	 * @param v - vertex to be added
	 */
	public abstract void addVertex(Vertex v);
	/**
	 * To add an edge to the graph.
	 * @param e - edge to be added
	 */
	public abstract void addEdge(Edge e);
}
