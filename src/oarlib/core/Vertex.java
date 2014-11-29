package oarlib.core;

import oarlib.exceptions.NoDemandSetException;
import oarlib.graph.util.Pair;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;


/**
 * Vertex abstraction. Most general contract that Vertex must fulfill.
 *
 * @author oliverlum
 */
public abstract class Vertex {

    private static final Logger LOGGER = Logger.getLogger(Vertex.class);

    private static int counter = 1; //for assigning global vertex ids
    private String mLabel;
    private int mGraphId;
    private int mId; //id in the graph, (1,2,3...)
    private int guid; //global id, for identifying a specific node that may have copies in multiple graphs
    private int matchId; //id for finding matching guys in different graphs, so that they can have different internal labels (so numbering is still from 1 - n) but we can still locate companions
    private int myDemand;
    private boolean demandSet;
    private int myCost;
    private int mySize;
    private double myX;
    private double myY;
    private boolean hasCoordinates;
    private boolean isFinalized; // should be true if in a graph, false oth.

    public Vertex(String label) {
        setId(-1);
        setGraphId(-1);
        setMatchId(-1);
        setCost(0);
        setSize(0);
        setLabel(label);
        setGuid(counter);
        counter++;
        demandSet = false;
        hasCoordinates = false;
        isFinalized = false;
    }

    public abstract Map<? extends Vertex, ? extends List<? extends Link<? extends Vertex>>> getNeighbors();


    public abstract void clearNeighbors();

    //==================================
    // Getters and Setters
    //==================================

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public int getDemand() throws NoDemandSetException {
        if (!demandSet) {
            //LOGGER.debug("It does not appear that demand is set.");
            throw new NoDemandSetException();
        }
        return myDemand;
    }

    public void setDemand(int newDemand) {
        //we only care about nonzero demands anyways
        demandSet = true;
        myDemand = newDemand;
    }

    public void unsetDemand() {
        demandSet = false;
    }

    public boolean isDemandSet() {
        return demandSet;
    }

    public int getGuid() {
        return guid;
    }

    public void setGuid(int guid) {
        this.guid = guid;
    }

    public int getMatchId() {
        return matchId;
    }

    public void setMatchId(int matchId) {
        this.matchId = matchId;
    }

    public int getGraphId() {
        return mGraphId;
    }

    public void setGraphId(int graphId) {
        this.mGraphId = graphId;
    }

    public boolean isFinalized() {
        return isFinalized;
    }

    public void setFinalized(boolean isFinalized) {
        this.isFinalized = isFinalized;
    }

    public int getCost() {
        return myCost;
    }

    public void setCost(int myCost) {
        this.myCost = myCost;
    }

    public int getSize() {
        return mySize;
    }

    public void setSize(int mySize) {
        this.mySize = mySize;
    }

    public double getX() {
        return myX;
    }

    public void setCoordinates(double x, double y) {
        myX = x;
        myY = y;
        hasCoordinates = true;
    }

    public double getY() {
        return myY;
    }

    public Pair<Double> getCoordinates() {
        return new Pair<Double>(myX, myY);
    }

    public boolean hasCoordinates() {
        return hasCoordinates;
    }
}