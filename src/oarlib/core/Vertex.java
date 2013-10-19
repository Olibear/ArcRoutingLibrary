package oarlib.core;

/**
 * Vertex abstraction. Most general contract that Vertex must fulfill.
 * @author oliverlum
 *
 */
public abstract class Vertex {
	private static int counter = 1; //for assigning vertex ids
	private String mLabel;
	private int mId;
	
	public Vertex(String label)
	{
		setLabel(label);
		setId(counter);
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
}