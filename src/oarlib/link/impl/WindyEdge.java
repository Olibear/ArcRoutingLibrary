/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015 Oliver Lum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package oarlib.link.impl;

import oarlib.core.Link;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.WindyVertex;

/**
 * WindyEdge class, basic class for an undirected link with asymmetric costs.
 *
 * @author Oliver
 */
public class WindyEdge extends Link<WindyVertex> implements AsymmetricLink {

    private int mReverseCost; // cost of traversing from endpoint 2 to endpoint 1
    private int mReverseServiceCost;
    private boolean mReverseRequired;

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
        setReverseServiceCost(0);
        setDirected(false);
        setReverseRequired(false);
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
        setReverseServiceCost(0);
        setDirected(false);
        setReverseRequired(false);
    }

    //==================================
    // Getters and Setters
    // =================================

    public int getReverseCost() {
        return mReverseCost;
    }

    @Override
    public int getReverseServiceCost() {
        return mReverseServiceCost;
    }

    public void setReverseCost(int mReverseCost) {
        this.mReverseCost = mReverseCost;
    }

    public void setReverseServiceCost(int mReverseServiceCost) { this.mReverseServiceCost = mReverseServiceCost; }

    @Override
    public boolean isReverseRequired() {
        return mReverseRequired;
    }

    public void setReverseRequired(boolean mReverseRequired) {
        this.mReverseRequired = mReverseRequired;
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
