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
	private Pair<V> mEndpoints;
	private double mCost;
	
	public Link(String label, Pair<V> endpoints, double cost)
	{
		//if mId gets set to a negative number, then we know it hasn't been set.
		setmId(-1);
		setLabel(label);
		setGuid(counter);
		setEndpoints(endpoints);
		setCost(cost);
		counter++;
	}
	public Link(String label, Pair<V> endpoints, double cost, int id) throws IllegalArgumentException
	{
		if( id < 0)
			throw new IllegalArgumentException("negative ids are reserved for detecting when ids have not been set.");
		setmId(id);
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

	public double getCost() {
		return mCost;
	}

	public void setCost(double mCost) {
		this.mCost = mCost;
	}
	public int getmId() {
		return mId;
	}
	public void setmId(int mId) {
		this.mId = mId;
	}

}
