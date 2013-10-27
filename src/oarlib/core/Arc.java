package oarlib.core;

import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;

/**
 * Arc abstraction.  The most general contract that all arcs must satisfy.
 * @author Oliver
 *
 */
public class Arc extends Link<DirectedVertex>{

	/**
	 * Constructor for an arc.  The order of the endpoints matters; the first in the pair should be the tail.  
	 * That is, if this an arc from v1 to v2, then the endpoints.getFirst() should be v1
	 * @param label - string identifier for the arc
	 * @param endpoints
	 * @param cost
	 */
	public Arc(String label, Pair<DirectedVertex> endpoints, int cost) {
		super(label, endpoints, cost);
	}
	/**
	 * Getter for the vertex that is at the head of this arc; (if the arc is from v1 to v2, this will return v2)
	 * @return - the head vertex
	 */
	public DirectedVertex getHead()
	{
		return getEndpoints().getSecond();
	}
	/**
	 * Getter for the vertex that is at the tail of this arc; (if the arc is from v1 to v2, this will return v1)
	 * @return - the tail vertex
	 */
	public DirectedVertex getTail()
	{
		return getEndpoints().getFirst();
	}

}
