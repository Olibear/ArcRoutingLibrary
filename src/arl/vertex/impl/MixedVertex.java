package arl.vertex.impl;

import arl.core.Vertex;

/**
 * Vertex representatio for use with Mixed Graphs.  This vertex stores undirected degree separately from in-degree and out-degree.
 * @author Oliver
 *
 */
public class MixedVertex extends Vertex {

	private int inDegree;
	private int outDegree;
	private int degree;
	
	public MixedVertex(String label) {
		super(label);
		setInDegree(0);
		setOutDegree(0);
		setDegree(0);
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

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}


}
