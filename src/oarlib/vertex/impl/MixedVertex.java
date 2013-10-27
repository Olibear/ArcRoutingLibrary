package oarlib.vertex.impl;

import java.util.ArrayList;
import java.util.HashMap;

import oarlib.core.Link;
import oarlib.core.Vertex;

/**
 * Vertex representation for use with Mixed Graphs.  This vertex stores undirected degree separately from in-degree and out-degree.
 * @author Oliver
 *
 */
public class MixedVertex extends Vertex {

	private int inDegree;
	private int outDegree;
	private int degree;
	private HashMap<MixedVertex, ArrayList<Link<MixedVertex>>> neighbors;
	
	public MixedVertex(String label) {
		super(label);
		setInDegree(0);
		setOutDegree(0);
		setDegree(0);
		neighbors = new HashMap<MixedVertex, ArrayList<Link<MixedVertex>>>();
		
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
	
	public HashMap<MixedVertex, ArrayList<Link<MixedVertex>>> getNeighbors()
	{
		return neighbors;
	}
	public void addToNeighbors(MixedVertex v, Link<MixedVertex> e)
	{
		neighbors.get(v).add(e);
	}

}
