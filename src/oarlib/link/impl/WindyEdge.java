package oarlib.link.impl;

import oarlib.core.Link;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.WindyVertex;

/**
 * WindyEdge class, basic class for an undirected link with asymmetric costs.
 *
 * @author Oliver
 */
public class WindyEdge extends Link<WindyVertex> {

    private int mReverseCost; // cost of traversing from endpoint 2 to endpoint 1

    /**
     * Constructor for a WindyEdge.
     *
     * @param label       - the String that can later be used for identification purposes, or to determine when an edge was added.
     * @param endpoints   - the pair of vertices that this edge connects.  Since this is undirected, order does not matter.
     * @param cost        - the cost of traversing the edge.
     * @param reverseCost = the cost of traversing the edge from the endpoint 2 to endpoint 1
     */
    public WindyEdge(String label, Pair<WindyVertex> endpoints, int cost, int reverseCost) {
        super(label, endpoints, cost);
        setReverseCost(reverseCost);
        setDirected(false);
    }

    /**
     * Constructor for a WindyEdge.
     *
     * @param label       - the String that can later be used for identification purposes, or to determine when an edge was added.
     * @param endpoints   - the pair of vertices that this edge connects.  Since this is undirected, order does not matter.
     * @param cost        - the cost of traversing the edge.
     * @param reverseCost = the cost of traversing the edge from the endpoint 2 to endpoint 1
     * @param required    - whether or not this edge must be traversed in the final solution
     */
    public WindyEdge(String label, Pair<WindyVertex> endpoints, int cost, int reverseCost, boolean required) {
        super(label, endpoints, cost, required);
        setReverseCost(reverseCost);
        setDirected(false);
    }

    //==================================
    // Getters and Setters
    // =================================

    public int getReverseCost() {
        return mReverseCost;
    }

    public void setReverseCost(int mReverseCost) {
        this.mReverseCost = mReverseCost;
    }

    @Override
    public WindyEdge getCopy() {
        return new WindyEdge("copy", this.getEndpoints(), this.getCost(), this.getReverseCost(), this.isRequired());
    }

    @Override
    public boolean isWindy() {
        return true;
    }

    @Override
    public Type getLinkType() {
        return Type.WINDY;
    }

}
