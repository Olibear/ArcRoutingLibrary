package arl.vertex.impl;

import arl.core.Vertex;
/**
 * Vertex representation for Undirected Graphs.
 * @author Oliver
 *
 */
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
