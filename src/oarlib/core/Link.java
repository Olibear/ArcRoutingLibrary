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

    //TODO: Rethink this architectre; consider creating an addToGraph method so that you can't do weird stuff with coupled vars

    private static Logger LOGGER = Logger.getLogger(Link.class);
    protected boolean isFinalized; // should be true if in a graph, false oth.
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

    public enum Type{
        UNDIRECTED,
        DIRECTED,
        MIXED,
        WINDY
    }

    public Link(String label, Pair<V> endpoints, int cost) {
        this(label, endpoints, cost, true);
    }

    public Link(String label, Pair<V> endpoints, int cost, boolean required) {
        setFinalized(false);
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

    //==================================
    // Getters and Setters
    //==================================

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    public Pair<V> getEndpoints() {
        return mEndpoints;
    }

    public boolean setEndpoints(Pair<V> mEndpoints) {
        if(!isFinalized) {
            this.mEndpoints = mEndpoints;
            return true;
        }
        return false;
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

    public void setGraphId(int mGraphId) {
        this.mGraphId = mGraphId;
    }

    public boolean isFinalized() {
        return isFinalized;
    }

    public void setFinalized(boolean isFinalized) {
        this.isFinalized = isFinalized;
    }

    public String toString() {
        return this.getEndpoints().getFirst().getId() + "-" + this.getEndpoints().getSecond().getId();
    }

    public abstract Type getLinkType();

}
