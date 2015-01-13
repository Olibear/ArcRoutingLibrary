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

    @Override
    public boolean isWindy() {
        return false;
    }

    @Override
    public Type getLinkType() {
        return Type.DIRECTED;
    }

}
