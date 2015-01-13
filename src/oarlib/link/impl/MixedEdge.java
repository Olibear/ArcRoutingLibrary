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
import oarlib.exceptions.WrongEdgeTypeException;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.MixedVertex;

public class MixedEdge extends Link<MixedVertex> {

    /**
     * Constructor for a MixedEdge.  It is unadvisable to use this constructor, as it defaults to assuming this edge is undirected.
     * Instead, one ought to use the constructor where it is explicitly passed in what type of link this is meant to represent
     * in the MixedGraph.
     *
     * @param label     - String for bookkeeping purposes
     * @param endpoints - ordered pair of mixed vertices that are the endpoipnts of the edge.
     * @param cost      - the cost of the edge
     */
    public MixedEdge(String label, Pair<MixedVertex> endpoints, int cost) {
        super(label, endpoints, cost);
        setDirected(false);
    }

    /**
     * Constructor for a MixedEdge.
     *
     * @param label     - String for bookkeeping purposes
     * @param endpoints - ordered pair of mixed vertices that are the endpoipnts of the edge.
     *                  If this link is directed, then it will be from the first vertex to the second vertex.
     * @param cost      - the cost of the edge
     */
    public MixedEdge(String label, Pair<MixedVertex> endpoints, int cost, boolean isDirected) {
        super(label, endpoints, cost);
        setDirected(isDirected);
    }

    public MixedEdge(String label, Pair<MixedVertex> endpoints, int cost, boolean isDirected, boolean required) {
        super(label, endpoints, cost, required);
        setDirected(isDirected);
    }

    /**
     * Getter for the vertex that is at the head of this arc; (if the arc is from v1 to v2, this will return v2)
     *
     * @return - the head vertex
     */
    public MixedVertex getHead() throws WrongEdgeTypeException {
        if (isDirected())
            return getEndpoints().getSecond();
        throw new WrongEdgeTypeException();
    }

    /**
     * Getter for the vertex that is at the tail of this arc; (if the arc is from v1 to v2, this will return v1)
     *
     * @return - the tail vertex
     */
    public MixedVertex getTail() throws WrongEdgeTypeException {
        if (isDirected())
            return getEndpoints().getFirst();
        throw new WrongEdgeTypeException();
    }

    @Override
    public MixedEdge getCopy() {
        return new MixedEdge("copy", this.getEndpoints(), this.getCost(), this.isDirected());
    }

    @Override
    public boolean isWindy() {
        return false;
    }

    @Override
    public Type getLinkType() {
        return Type.MIXED;
    }

}
