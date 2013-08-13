package arl.core;

import arl.graph.util.Pair;

/**
 * Arc abstraction.  The most general contract that all arcs must satisfy.
 * @author Oliver
 *
 */
public abstract class Arc extends Edge{

	/**
	 * Constructor for an arc.  The order of the endpoints matters; the first in the pair should be the tail.  
	 * That is, if this an arc from v1 to v2, then the endpoints.getFirst() should be v1
	 * @param label - string identifier for the arc
	 * @param endpoints
	 * @param cost
	 */
	public Arc(String label, Pair<Vertex> endpoints, double cost) {
		super(label, endpoints, cost);
	}
	public Vertex getHead()
	{
		return getEndpoints().getSecond();
	}
	public Vertex getTail()
	{
		return getEndpoints().getFirst();
	}

}
