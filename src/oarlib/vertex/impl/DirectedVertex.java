package oarlib.vertex.impl;

import java.util.ArrayList;
import java.util.HashMap;

import oarlib.core.Arc;
import oarlib.core.Vertex;
/**
 * Vertex representation for Directed Graphs.  
 * @author Oliver
 *
 */
public class DirectedVertex extends Vertex{

	private int inDegree;
	private int outDegree;
	private HashMap<DirectedVertex,ArrayList<Arc>> neighbors;
	
	public DirectedVertex(String label) {
		super(label);
		setInDegree(0);
		setOutDegree(0);
		neighbors = new HashMap<DirectedVertex,ArrayList<Arc>>();
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
	
	public int getDelta()
	{
		return inDegree - outDegree;
	}
	
	@Override
	public HashMap<DirectedVertex, ArrayList<Arc>> getNeighbors()
	{
		return neighbors;
	}
	public void addToNeighbors(DirectedVertex v, Arc a)
	{
		if(!neighbors.containsKey(v))
			neighbors.put(v, new ArrayList<Arc>());
		neighbors.get(v).add(a);
		return;
	}
	public void removeFromNeighbors(DirectedVertex v, Arc a) throws IllegalArgumentException
	{
		neighbors.get(v).remove(a);
		if(neighbors.get(v).size()==0)
			neighbors.remove(v);
		return;
	}

}
