package oarlib.link.impl;

import oarlib.core.Link;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;

/**
 * Arc abstraction.  The most general contract that all arcs must satisfy.
 *
 * @author Oliver
 */
public class Arc extends Link<DirectedVertex> {

    /**
     * Constructor for an arc.  The order of the endpoints matters; the first in the pair should be the tail.
     * That is, if this an arc from v1 to v2, then the endpoints.getFirst() should be v1
     *
     * @param label     - string identifier for the arc
     * @param endpoints - the pair of directed vertices that form the endpoints of the arc.  The arc is
     *                  from the first vertex in the pair to the second.
     * @param cost      - the cost of traversing this edge.
     */
    public Arc(String label, Pair<DirectedVertex> endpoints, int cost) {
        super(label, endpoints, cost);
        setDirected(true);
    }

    /**
     * Constructor for an arc.  The order of the endpoints matters; the first in the pair should be the tail.
     * That is, if this an arc from v1 to v2, then the endpoints.getFirst() should be v1
     *
     * @param label     - string identifier for the arc
     * @param endpoints - the pair of directed vertices that form the endpoints of the arc.  The arc is
     *                  from the first vertex in the pair to the second.
     * @param cost      - the cost of traversing this edge
     * @param required  - whether or not this edge must be traversed in the final solution
     */
    public Arc(String label, Pair<DirectedVertex> endpoints, int cost, boolean required) {
        super(label, endpoints, cost, required);
        setDirected(true);
    }

    /**
     * Getter for the vertex that is at the head of this arc; (if the arc is from v1 to v2, this will return v2)
     *
     * @return - the head vertex
     */
    public DirectedVertex getHead() {
        return getEndpoints().getSecond();
    }

    /**
     * Getter for the vertex that is at the tail of this arc; (if the arc is from v1 to v2, this will return v1)
     *
     * @return - the tail vertex
     */
    public DirectedVertex getTail() {
        return getEndpoints().getFirst();
    }

    @Override
    public Arc getCopy() {
        return new Arc("copy", this.getEndpoints(), this.getCost());
    }

}
