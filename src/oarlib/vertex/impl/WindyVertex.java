package oarlib.vertex.impl;

import oarlib.core.Vertex;
import oarlib.link.impl.WindyEdge;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Vertex representation for Windy Graphs.
 *
 * @author Oliver
 */
public class WindyVertex extends Vertex {

    private int degree;
    private HashMap<WindyVertex, ArrayList<WindyEdge>> neighbors;

    public WindyVertex(String label) {
        super(label);
        neighbors = new HashMap<WindyVertex, ArrayList<WindyEdge>>();
        setDegree(0);
    }

    /**
     * Adds an arc joining this vertex with v.
     *
     * @param v - the other endpoint of the arc.
     * @param a - the arc to be added.
     * @throws IllegalArgumentException - if the vertex isn't the other endpoint of the arc provided.
     */
    public void addToNeighbors(WindyVertex v, WindyEdge e) throws IllegalArgumentException {
        if ((e.getEndpoints().getFirst() != this && e.getEndpoints().getSecond() != this) || (e.getEndpoints().getFirst() != v && e.getEndpoints().getSecond() != v) || this.getGraphId() != v.getGraphId())
            throw new IllegalArgumentException();
        if (!neighbors.containsKey(v))
            neighbors.put(v, new ArrayList<WindyEdge>());
        neighbors.get(v).add(e);
    }

    /**
     * Removes an arc joining this vertex with v.
     *
     * @param v - the other endpoint of the arc.
     * @param a - the arc to be removed.
     * @return true if operation successful, false if the arguments were invalid.
     */
    public boolean removeFromNeighbors(WindyVertex v, WindyEdge a) {
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

    public int getDegree() {
        return degree;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    //=================================
    //
    // Graph Override
    //
    //=================================

    @Override
    public HashMap<WindyVertex, ArrayList<WindyEdge>> getNeighbors() {
        return neighbors;
    }

    @Override
    public void clearNeighbors() {
        neighbors = new HashMap<WindyVertex, ArrayList<WindyEdge>>();
    }


}
