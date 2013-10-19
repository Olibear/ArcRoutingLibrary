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
	private int mId;
	private Pair<V> mEndpoints;
	private double mCost;
	
	public Link(String label, Pair<V> endpoints, double cost)
	{
		setLabel(label);
		setId(counter);
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

	public int getId() {
		return mId;
	}

	public void setId(int mId) {
		this.mId = mId;
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

}
