package oarlib.link.impl;

import oarlib.core.Link;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.UndirectedVertex;

/**
 * Edge class, basic class for an undirected link.
 *
 * @author Oliver
 */
public class Edge extends Link<UndirectedVertex> {

    /**
     * Constructor for an Edge.
     *
     * @param label     - the String that can later be used for identification purposes, or to determine when an edge was added.
     * @param endpoints - the pair of vertices that this edge connects.  Since this is undirected, order does not matter.
     * @param cost      - the cost of traversing the edge.
     */
    public Edge(String label, Pair<UndirectedVertex> endpoints, int cost) {
        super(label, endpoints, cost);
        setDirected(false);
    }

    /**
     * Constructor for an Edge.
     *
     * @param label     - the String that can later be used for identification purposes, or to determine when an edge was added.
     * @param endpoints - the pair of vertices that this edge connects.  Since this is undirected, order does not matter.
     * @param cost      - the cost of traversing the edge.
     * @param required  - whether or not this edge must be traversed in the final solution
     */
    public Edge(String label, Pair<UndirectedVertex> endpoints, int cost, boolean required) {
        super(label, endpoints, cost, required);
        setDirected(false);
    }

    @Override
    public Edge getCopy() {
        return new Edge("copy", this.getEndpoints(), this.getCost());
    }

}
