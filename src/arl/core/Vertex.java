package arl.core;

import arl.graph.util.Pair;

/**
 * Vertex abstraction. Most general contract that Vertex must fulfill.
 * @author oliverlum
 *
 */
public abstract class Vertex {
	private String mLabel;
	private int mId;
	
	public Vertex(String label, int id)
	{
		setLabel(label);
		setId(id);
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
}
