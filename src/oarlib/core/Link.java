package oarlib.core;

import oarlib.exceptions.NoCapacitySetException;
import oarlib.graph.util.Pair;

/**
 * Link abstraction. Provides most general contract for all Link objects.
 *
 * @author oliverlum
 */
public abstract class Link<V extends Vertex> {

    private static int counter = 1; //for assigning edge ids
    private String mLabel;
    private int guid; //the idea is that this will be unique for all links, even between graphs
    private int mId; //while this will help us identify the 'same' link in different graphs (graph copies for instance)
    private int mGraphId;
    private int matchId;
    private Pair<V> mEndpoints;
    private int mCost;
    private int mCapacity;
    private boolean isDirected;
    private boolean isRequired;
    private boolean capacitySet;
    protected boolean isFinalized; // should be true if in a graph, false oth.

    public Link(String label, Pair<V> endpoints, int cost) {
        this(label, endpoints, cost, true);
    }

    public Link(String label, Pair<V> endpoints, int cost, boolean required) {
        setFinalized(false);
        setId(-1);
        setGraphId(-1);
        setMatchId(-1);
        setLabel(label);
        setGuid(counter);
        setEndpoints(endpoints);
        setCost(cost);
        setRequired(required);
        capacitySet = false;
        counter++;
    }

    /**
     * Gets a copy of the edge.
     *
     * @return - a copy of the edge
     */
    public abstract Link<V> getCopy();

    //==================================
    // Getters and Setters
    //==================================

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    public int getGuid() {
        return guid;
    }

    public void setGuid(int mId) {
        this.guid = mId;
    }

    public Pair<V> getEndpoints() {
        return mEndpoints;
    }

    public void setEndpoints(Pair<V> mEndpoints) {
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

    public boolean setId(int mId) {
        if (!isFinalized) {
            this.mId = mId;
            return true;
        }
        return false;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public void setCapacity(int newCapacity) throws IllegalArgumentException {
        //negative capcity is not meaningful
        if (newCapacity < 0)
            throw new IllegalArgumentException();
        capacitySet = true;
        mCapacity = newCapacity;
    }

    public int getCapacity() throws NoCapacitySetException {
        if (!capacitySet)
            throw new NoCapacitySetException();
        return mCapacity;
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

    public void setGraphId(int mGraphId) {
        this.mGraphId = mGraphId;
    }

    public boolean isFinalized() {
        return isFinalized;
    }

    public void setFinalized(boolean isFinalized) {
        this.isFinalized = isFinalized;
    }

}
