package arl.core;

import java.util.List;

/**
 * Route abstraction. Most general contract that routes must fulfill.
 * @author oliverlum
 *
 */
public abstract class Route {
	//List of vertices to be traversed from first to last
	protected List<Vertex> orderedTraversal;
	/**
	 * check to make sure that the route is feasible.
	 * @return true if route is feasible in the provided graph
	 */
	public abstract boolean checkRoutes(Graph g);
	/**
	 * attempt to add a vertex to the route at the specified index
	 */
	public abstract void tryAddVertex(int position);
	
}
