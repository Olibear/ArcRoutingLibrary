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
package oarlib.vertex.impl;

import oarlib.core.Vertex;
import oarlib.link.impl.Arc;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Vertex representation for Directed Graphs.
 *
 * @author Oliver
 */
public class DirectedVertex extends Vertex {

    private int inDegree;
    private int outDegree;
    private HashMap<DirectedVertex, ArrayList<Arc>> neighbors;

    public DirectedVertex(String label) {
        super(label);
        setInDegree(0);
        setOutDegree(0);
        neighbors = new HashMap<DirectedVertex, ArrayList<Arc>>();
    }

    /**
     * Adds an arc joining this vertex with v.
     *
     * @param v - the other endpoint of the arc.
     * @param a - the arc to be added.
     * @throws IllegalArgumentException - if the vertex isn't the other endpoint of the arc provided.
     */
    public void addToNeighbors(DirectedVertex v, Arc a) throws IllegalArgumentException {
        if (a.getTail() != this || a.getHead() != v || this.getGraphId() != v.getGraphId())
            throw new IllegalArgumentException();
        if (!neighbors.containsKey(v))
            neighbors.put(v, new ArrayList<Arc>());
        neighbors.get(v).add(a);
    }

    /**
     * Removes an arc joining this vertex with v.
     *
     * @param v - the other endpoint of the arc.
     * @param a - the arc to be removed.
     * @return true if operation successful, false if the arguments were invalid.
     */
    public boolean removeFromNeighbors(DirectedVertex v, Arc a) {
        if (!neighbors.containsKey(v) || !neighbors.get(v).contains(a))
            return false;
        neighbors.get(v).remove(a);
        if (neighbors.get(v).size() == 0)
            neighbors.remove(v);
        return true;
    }

    //=================================
    //
    // Getters and Setters
    //
    //=================================
    public int getInDegree() {
        return inDegree;
    }

    public void setInDegree(int inDegree) {
        this.inDegree = inDegree;
    }

    public int getOutDegree() {
        return outDegree;
    }

    public void setOutDegree(int outDegree) {
        this.outDegree = outDegree;
    }

    public int getDelta() {
        return inDegree - outDegree;
    }

    //====================================
    //
    // Graph Overrides
    //
    //====================================

    @Override
    public HashMap<DirectedVertex, ArrayList<Arc>> getNeighbors() {
        return neighbors;
    }

    @Override
    public void clearNeighbors() {
        neighbors = new HashMap<DirectedVertex, ArrayList<Arc>>();
    }

}
