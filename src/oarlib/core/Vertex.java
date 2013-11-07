package oarlib.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oarlib.exceptions.NoDemandSetException;
import oarlib.vertex.impl.UndirectedVertex;

/**
 * Vertex abstraction. Most general contract that Vertex must fulfill.
 * @author oliverlum
 *
 */
public abstract class Vertex {
	private static int counter = 1; //for assigning vertex ids
	private String mLabel;
	private int mId; //id in the graph, (1,2,3...)
	private int guid; //global id, for identifying a specific node that may have copies in multiple graphs
	private int matchId; //id for finding matching guys in different graphs, so that they can have different internal labels (so numbering is still from 1 - n) but we can still locate companions
	private int myDemand;
	private boolean demandSet;
	
	public Vertex(String label)
	{
		setId(-1);
		setMatchId(-1);
		setLabel(label);
		setGuid(counter);
		counter++;
		demandSet = false;
	}
	
	public abstract Map<? extends Vertex, ? extends List<? extends Link<? extends Vertex>>> getNeighbors();

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
		//we only care about nonzero demands anyways
		if(newDemand==0)
			return;
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

	public int getMatchId() {
		return matchId;
	}

	public void setMatchId(int matchId) {
		this.matchId = matchId;
	}
}