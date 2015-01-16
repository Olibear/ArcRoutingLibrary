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
package oarlib.core;

import oarlib.exceptions.NoCapacitySetException;
import oarlib.graph.util.Pair;
import org.apache.log4j.Logger;

/**
 * Link abstraction. Provides most general contract for all Link objects.
 *
 * @author oliverlum
 */
public abstract class Link<V extends Vertex> {

    private static Logger LOGGER = Logger.getLogger(Link.class);
    private String mLabel;
    private int mId; //while this will help us identify the 'same' link in different graphs (graph copies for instance)
    private int mGraphId;
    private int matchId;
    private Pair<V> mEndpoints;
    private int mCost;
    private int mCapacity;
    private boolean isDirected;
    private boolean isRequired;
    private boolean capacitySet;

    public Link(String label, Pair<V> endpoints, int cost) {
        this(label, endpoints, cost, true);
    }

    public Link(String label, Pair<V> endpoints, int cost, boolean required) {
        setId(-1);
        setGraphId(-1);
        setMatchId(-1);
        setLabel(label);
        setEndpoints(endpoints);
        setCost(cost);
        setRequired(required);
        capacitySet = false;
    }

    /**
     * Gets a copy of the edge.
     *
     * @return - a copy of the edge
     */
    public abstract Link<V> getCopy();

    public String getLabel() {
        return mLabel;
    }

    //==================================
    // Getters and Setters
    //==================================

    public void setLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    public Pair<V> getEndpoints() {
        return mEndpoints;
    }

    protected void setEndpoints(Pair<V> mEndpoints) {
        this.mEndpoints = mEndpoints;
    }

    public int getCost() {
        return mCost;
    }

    public void setCost(int mCost) {
        this.mCost = mCost;
    }

    public int getId() {
        return mId;
    }

    protected void setId(int mId) {
        this.mId = mId;
    }

    public int getFirstEndpointId() {
        return mEndpoints.getFirst().getId();
    }

    public int getSecondEndpointId() {
        return mEndpoints.getSecond().getId();
    }

    public boolean isDirected() {
        return isDirected;
    }

    protected void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public abstract boolean isWindy();

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public int getCapacity() throws NoCapacitySetException {
        if (!capacitySet) {
            LOGGER.error("It does not appear as though capacity has been set for this link.");
            throw new NoCapacitySetException();
        }
        return mCapacity;
    }

    public void setCapacity(int newCapacity) throws IllegalArgumentException {
        //negative capcity is not meaningful
        if (newCapacity < 0) {
            LOGGER.error("The capacity cannot be set to less than 0.");
            throw new IllegalArgumentException();
        }
        capacitySet = true;
        mCapacity = newCapacity;
    }

    public boolean isCapacitySet() {
        return capacitySet;
    }

    public void unsetCapacity() {
        capacitySet = false;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public void setRequired(boolean isRequired) {
        this.isRequired = isRequired;
    }

    public int getGraphId() {
        return mGraphId;
    }

    protected void setGraphId(int mGraphId) {
        this.mGraphId = mGraphId;
    }

    public String toString() {
        return this.getEndpoints().getFirst().getId() + "-" + this.getEndpoints().getSecond().getId();
    }

    public abstract Type getLinkType();

    public enum Type {
        UNDIRECTED,
        DIRECTED,
        MIXED,
        WINDY
    }

}
