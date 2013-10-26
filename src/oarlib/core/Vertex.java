package oarlib.core;

import oarlib.exceptions.NoDemandSetException;

/**
 * Vertex abstraction. Most general contract that Vertex must fulfill.
 * @author oliverlum
 *
 */
public abstract class Vertex {
	private static int counter = 1; //for assigning vertex ids
	private String mLabel;
	private int mId;
	private int guid;
	private int myDemand;
	private boolean demandSet;
	
	public Vertex(String label)
	{
		setId(-1);
		setLabel(label);
		setGuid(counter);
		counter++;
		demandSet = false;
	}
	
	public Vertex(String label, int id)
	{
		setId(id);
		setLabel(label);
		setGuid(counter);
		counter++;
		demandSet = false;
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
	
	public int getDemand() throws NoDemandSetException{
		if (!demandSet)
			throw new NoDemandSetException();
		return myDemand;
	}
	
	public void setDemand(int newDemand) {
		demandSet = true;
		myDemand = newDemand;
	}
	
	public void unsetDemand() {
		demandSet = false;
	}

	public int getGuid() {
		return guid;
	}

	public void setGuid(int guid) {
		this.guid = guid;
	}
}