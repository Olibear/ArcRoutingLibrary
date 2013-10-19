package arl.core;

import java.util.List;

/**
 * Route abstraction. Most general contract that routes must fulfill.
 * @author oliverlum
 *
 */
public abstract class Route {
	/**
	 * Retrieve a copy of the current route.
	 * @return List of vertices to be traversed from first to last
	 */
	public abstract List<Vertex> getRoute();
	/**
	 * Add a vertex to the end of this route.
	 * @param v
	 */
	public abstract void appendVertex(Vertex v);
	/**
	 * Add a vertex to the beginning of this route.
	 * @param v
	 */
	public abstract void prependVertex(Vertex v);
	/**
	 * Insert a vertex at a specified position in the route (0 is first)
	 * @param v
	 * @param position - where to insert the vertex, (i.e. 0 will do the same as prependVertex)
	 */
	public abstract void insertVertex(Vertex v, int position) throws IndexOutOfBoundsException;
	/**
	 * check to make sure that the route is feasible.
	 * @return true if route is feasible in the provided graph
	 */
	public abstract boolean checkRoutes(Graph<Vertex, Link<Vertex>> g);
	/**
	 * attempt to add a vertex to the route at the specified index
	 */
	public abstract void tryAddVertex(int position);
	
}
