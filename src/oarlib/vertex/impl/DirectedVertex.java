package oarlib.vertex.impl;

import oarlib.core.Arc;
import oarlib.core.Vertex;

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
