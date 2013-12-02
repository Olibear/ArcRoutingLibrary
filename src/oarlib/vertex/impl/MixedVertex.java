package oarlib.vertex.impl;

import java.util.ArrayList;
import java.util.HashMap;

import oarlib.core.Arc;
import oarlib.core.Link;
import oarlib.core.MixedEdge;
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
	private HashMap<MixedVertex, ArrayList<MixedEdge>> neighbors;
	
	public MixedVertex(String label) {
		super(label);
		setInDegree(0);
		setOutDegree(0);
		setDegree(0);
		neighbors = new HashMap<MixedVertex, ArrayList<MixedEdge>>();
		
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
	
	public int getDelta()
	{
		return inDegree - outDegree;
	}
	
	public HashMap<MixedVertex, ArrayList<MixedEdge>> getNeighbors()
	{
		return neighbors;
	}
	public void addToNeighbors(MixedVertex v, MixedEdge e)
	{
		if(!neighbors.containsKey(v))
			neighbors.put(v, new ArrayList<MixedEdge>());
		neighbors.get(v).add(e);
	}
	public void removeFromNeighbors(MixedVertex v, MixedEdge e) throws IllegalArgumentException
	{
		neighbors.get(v).remove(e);
		if(neighbors.get(v).size() == 0)
			neighbors.remove(v);
		return;
	}

	@Override
	public void clearNeighbors() {
		neighbors = new HashMap<MixedVertex, ArrayList<MixedEdge>>();
	}

}
