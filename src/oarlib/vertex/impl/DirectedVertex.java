package oarlib.vertex.impl;

import java.util.HashMap;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Vertex;
/**
 * Vertex representation for Directed Graphs.  
 * @author Oliver
 *
 */
public class DirectedVertex extends Vertex{

	private int inDegree;
	private int outDegree;
	private HashMap<DirectedVertex,Arc> neighbors;
	
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
	
	public HashMap<DirectedVertex, Arc> getNeighbors()
	{
		return neighbors;
	}
	
	public void addToNeighbors(DirectedVertex v, Arc a)
	{
		neighbors.put(v, a);
		return;
	}

}
