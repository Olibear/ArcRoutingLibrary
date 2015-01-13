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

    @Override
    public boolean isWindy() {
        return false;
    }

    @Override
    public Type getLinkType() {
        return Type.UNDIRECTED;
    }

}
