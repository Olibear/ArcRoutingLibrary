package arl.vertex.impl;

import arl.core.Vertex;
/**
 * Vertex representation for Directed Graphs.  
 * @author Oliver
 *
 */
public class DirectedVertex extends Vertex{

	private int inDegree;
	private int outDegree;
	
	public DirectedVertex(String label) {
		super(label);
		setInDegree(0);
		setOutDegree(0);
	}

	public int getInDegree() {
		return inDegree;
	}

	public void setInDegree(int inDegree) {
		this.inDegree = inDegree;
	}

	public int getOutDegree() {
		return outDegree;
	}

	public void setOutDegree(int outDegree) {
		this.outDegree = outDegree;
	}


}
