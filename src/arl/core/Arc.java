package arl.core;

import arl.graph.util.Pair;
import arl.vertex.impl.DirectedVertex;
import arl.vertex.impl.UndirectedVertex;

/**
 * Arc abstraction.  The most general contract that all arcs must satisfy.
 * @author Oliver
 *
 */
public abstract class Arc extends Link<DirectedVertex>{

	/**
	 * Constructor for an arc.  The order of the endpoints matters; the first in the pair should be the tail.  
	 * That is, if this an arc from v1 to v2, then the endpoints.getFirst() should be v1
	 * @param label - string identifier for the arc
	 * @param endpoints
	 * @param cost
	 */
	public Arc(String label, Pair<DirectedVertex> endpoints, double cost) {
		super(label, endpoints, cost);
	}
	public DirectedVertex getHead()
	{
		return getEndpoints().getSecond();
	}
	public DirectedVertex getTail()
	{
		return getEndpoints().getFirst();
	}

}
