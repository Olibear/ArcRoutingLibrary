package oarlib.vertex.impl;

import java.util.ArrayList;
import java.util.HashMap;
import oarlib.core.Vertex;
import oarlib.core.WindyEdge;
/**
 * Vertex representation for Windy Graphs.
 * @author Oliver
 *
 */
public class WindyVertex extends Vertex{

	private int degree;
	private HashMap<WindyVertex, ArrayList<WindyEdge>> neighbors;
	
	public WindyVertex(String label) {
		super(label);
		neighbors = new HashMap<WindyVertex, ArrayList<WindyEdge>>();
		setDegree(0);
	}

	public int getDegree() {
		return degree;
	}

	public void setDegree(int degree) {
		this.degree = degree;
	}
	
	@Override
	public HashMap<WindyVertex, ArrayList<WindyEdge>> getNeighbors()
	{
		return neighbors;
	}
	public void addToNeighbors(WindyVertex v, WindyEdge e)
	{
		if(!neighbors.containsKey(v))
			neighbors.put(v, new ArrayList<WindyEdge>());
		neighbors.get(v).add(e);
		return;
	}

	public void removeFromNeighbors(WindyVertex v, WindyEdge e) throws IllegalArgumentException
	{
		neighbors.get(v).remove(e);
		if(neighbors.get(v).size()==0)
			neighbors.remove(v);
		return;
	}

	@Override
	public void clearNeighbors() {
		neighbors = new HashMap<WindyVertex, ArrayList<WindyEdge>>();
	}


}
