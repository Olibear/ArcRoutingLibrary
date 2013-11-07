package oarlib.core;

import oarlib.graph.util.Pair;

/**
 * Link abstraction.  Provides most general contract for all Link objects.
 * @author oliverlum
 *
 */
public abstract class Link<V extends Vertex> {
	
	private static int counter = 1; //for assigning edge ids
	private String mLabel;
	private int guid; //the idea is that this will be unique for all links, even between graphs
	private int mId; //while this will help us identify the 'same' link in different graphs (graph copies for instance)
	private int matchId;
	private Pair<V> mEndpoints;
	private int mCost;
	private boolean isDirected;
	
	public Link(String label, Pair<V> endpoints, int cost)
	{
		setId(-1);
		setMatchId(-1);
		setLabel(label);
		setGuid(counter);
		setEndpoints(endpoints);
		setCost(cost);
		counter++;
	}

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
	public void setId(int mId) {
		this.mId = mId;
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

}
