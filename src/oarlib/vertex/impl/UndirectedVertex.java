package oarlib.vertex.impl;

import java.util.ArrayList;
import java.util.HashMap;

import oarlib.core.Edge;
import oarlib.core.Vertex;
/**
 * Vertex representation for Undirected Graphs.
 * @author Oliver
 *
 */
public class UndirectedVertex extends Vertex{

	private int degree;
	private HashMap<UndirectedVertex, ArrayList<Edge>> neighbors;
	
	public UndirectedVertex(String label) {
		super(label);
		neighbors = new HashMap<UndirectedVertex, ArrayList<Edge>>();
		setDegree(0);
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}
	
	public HashMap<UndirectedVertex, ArrayList<Edge>> getNeighbors()
	{
		return neighbors;
	}
	public void addToNeighbors(UndirectedVertex v, Edge e)
	{
		neighbors.get(v).add(e);
		return;
	}


}
