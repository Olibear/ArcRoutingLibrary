package arl.vertex.impl;

import arl.core.Vertex;

public class UndirectedVertex extends Vertex{

	private int degree;
	
	public UndirectedVertex(String label) {
		super(label);
		setDegree(0);
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}


}
