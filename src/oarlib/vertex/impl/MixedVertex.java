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

import oarlib.core.Link;
import oarlib.core.Vertex;
import oarlib.link.impl.Arc;
import oarlib.link.impl.MixedEdge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Vertex representation for use with Mixed Graphs.  This vertex stores undirected degree separately from in-degree and out-degree.
 *
 * @author Oliver
 */
public class MixedVertex extends Vertex {

    private int inDegree;
    private int outDegree;
    private int degree;
    private HashMap<MixedVertex, ArrayList<MixedEdge>> neighbors;

    public MixedVertex(String label) {
        super(label);
        setInDegree(0);
        setOutDegree(0);
        setDegree(0);
        neighbors = new HashMap<MixedVertex, ArrayList<MixedEdge>>();
    }

    /**
     * Adds an edge joining this vertex with v.
     *
     * @param v - the other endpoint of the arc.
     * @param e - the arc to be added.
     * @throws IllegalArgumentException - if the vertex isn't the other endpoint of the arc provided.
     */
    public void addToNeighbors(MixedVertex v, MixedEdge e) throws IllegalArgumentException {
        try {
            if (!e.isDirected()) {
                if ((e.getEndpoints().getFirst() != this && e.getEndpoints().getSecond() != this) || (e.getEndpoints().getFirst() != v && e.getEndpoints().getSecond() != v) || this.getGraphId() != v.getGraphId())
                    throw new IllegalArgumentException();
            } else if (e.getTail() != this || e.getHead() != v || this.getGraphId() != v.getGraphId())
                throw new IllegalArgumentException();


            if (!neighbors.containsKey(v))
                neighbors.put(v, new ArrayList<MixedEdge>());
            neighbors.get(v).add(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removes an edge joining this vertex with v.
     *
     * @param v - the other endpoint of the arc.
     * @param a - the arc to be removed.
     * @return true if operation successful, false if the arguments were invalid.
     */
    public boolean removeFromNeighbors(MixedVertex v, MixedEdge a) {
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

    @Override
    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    public int getDelta() {
        return inDegree - outDegree;
    }

    //=================================
    //
    // Graph Overrides
    //
    //=================================

    @Override
    public HashMap<MixedVertex, ArrayList<MixedEdge>> getNeighbors() {
        return neighbors;
    }

    @Override
    public void clearNeighbors() {
        neighbors = new HashMap<MixedVertex, ArrayList<MixedEdge>>();
    }

}
