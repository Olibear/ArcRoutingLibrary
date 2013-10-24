package oarlib.vertex.impl;

import java.util.HashMap;

import oarlib.core.Edge;
import oarlib.core.Link;
import oarlib.core.Vertex;
/**
 * Vertex representation for Undirected Graphs.
 * @author Oliver
 *
 */
public class UndirectedVertex extends Vertex{

	private int degree;
	private HashMap<UndirectedVertex, Edge> neighbors;
	
	public UndirectedVertex(String label) {
		super(label);
		neighbors = new HashMap<UndirectedVertex, Edge>();
		setDegree(0);
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}
	
	public HashMap<UndirectedVertex, Edge> getNeighbors()
	{
		return neighbors;
	}
	public void addToNeighbors(UndirectedVertex v, Edge e)
	{
		neighbors.put(v, e);
		return;
	}


}
