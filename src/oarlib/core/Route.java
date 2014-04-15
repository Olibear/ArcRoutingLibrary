package oarlib.core;

import java.util.List;

/**
 * Route abstraction. Most general contract that routes must fulfill.
 * @author oliverlum
 *
 */
public abstract class Route {
	
	protected int mCost; // cost of the route
	
	//constructor
	protected Route(){
		mCost = 0;
	}
	
	/**
	 * @return the cost of the route
	 */
	public int getCost()
	{
		return mCost;
	}
	
	/**
	 * Retrieve a copy of the current route.
	 * @return List of edges to be traversed from first to last
	 */
	public abstract List<? extends Link<? extends Vertex>> getRoute();
	/**
	 * Add a edge to the end of this route.
	 * @param l
	 */
	public abstract void appendEdge(Link<? extends Vertex> l);
	/**
	 * check to make sure that the route is feasible.
	 * @return true if route is feasible in the provided graph
	 */
	public abstract boolean checkRoutes(Graph<? extends Vertex, Link<? extends Vertex>> g);

	
}
