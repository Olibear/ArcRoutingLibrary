package oarlib.graph.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import oarlib.core.Graph;
import oarlib.core.WindyEdge;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.WindyVertex;

public class WindyGraph extends MutableGraph<WindyVertex, WindyEdge>{


	//constructors
	public WindyGraph(){
		super();
	}

	//====================================================
	//
	// Adders and Factory methods that take reverse cost
	//
	//====================================================

	public void addEdge(int i, int j, String desc, int cost, int reverseCost)
			throws InvalidEndpointsException {
		this.addEdge(this.constructEdge(i, j, desc, cost, reverseCost));
	}

	public void addEdge(int i, int j, String desc, int cost, int reverseCost, int matchId)
			throws InvalidEndpointsException {
		this.addEdge(this.constructEdge(i, j, desc, cost, reverseCost), matchId);
	}

	public void addEdge(int i, int j, String desc, int cost, int reverseCost, boolean isRequired)
			throws InvalidEndpointsException {
		this.addEdge(this.constructEdge(i, j, desc, cost, reverseCost, isRequired));
	}

	public void addEdge(int i, int j, String desc, int cost, int reverseCost, int matchId, boolean isRequired)
			throws InvalidEndpointsException {
		this.addEdge(this.constructEdge(i, j, desc, cost, reverseCost, isRequired), matchId);
	}

	public WindyEdge constructEdge(int i, int j, String desc, int cost, int reverseCost)
			throws InvalidEndpointsException {
		if(i > this.getVertices().size() || j > this.getVertices().size())
			throw new InvalidEndpointsException();
		return new WindyEdge(desc, new Pair<WindyVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost, reverseCost);

	}

	public WindyEdge constructEdge(int i, int j, String desc, int cost, int reverseCost, boolean isRequired)
			throws InvalidEndpointsException {
		if(i > this.getVertices().size() || j > this.getVertices().size())
			throw new InvalidEndpointsException();
		return new WindyEdge(desc, new Pair<WindyVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost, reverseCost, isRequired);

	}

	//====================================================
	//
	// Graph Override
	//
	//====================================================

	@Override
	public void addVertex(WindyVertex v) {
		super.addVertex(v);
	}

	@Override
	public List<WindyEdge> findEdges(Pair<WindyVertex> endpoints) {
		WindyVertex first = endpoints.getFirst();
		HashMap<WindyVertex, ArrayList<WindyEdge>> firstNeighbors = first.getNeighbors();
		return firstNeighbors.get(endpoints.getSecond());
	}

	@Override
	public oarlib.core.Graph.Type getType() {
		return Graph.Type.WINDY;
	}

	@Override
	public void addEdge(WindyEdge e) throws InvalidEndpointsException{
		Pair<WindyVertex> endpoints = e.getEndpoints();
		endpoints.getFirst().addToNeighbors(endpoints.getSecond(), e);
		endpoints.getSecond().addToNeighbors(endpoints.getFirst(), e);
		WindyVertex toUpdate = endpoints.getFirst();
		toUpdate.setDegree(toUpdate.getDegree()+1);
		toUpdate = e.getEndpoints().getSecond();
		toUpdate.setDegree(toUpdate.getDegree()+1);
		super.addEdge(e);
	}

	@Override
	public void clearEdges()
	{
		super.clearEdges();
		for(WindyVertex v: this.getVertices())
		{
			v.setDegree(0);
			v.clearNeighbors();
		}
	}

	@Override
	public void removeEdge(WindyEdge e) throws IllegalArgumentException
	{
		if(!this.getEdges().contains(e))
			throw new IllegalArgumentException();
		Pair<WindyVertex> endpoints = e.getEndpoints();
		endpoints.getFirst().removeFromNeighbors(endpoints.getSecond(), e);
		endpoints.getSecond().removeFromNeighbors(endpoints.getFirst(), e);
		WindyVertex toUpdate = endpoints.getFirst();
		toUpdate.setDegree(toUpdate.getDegree() - 1);
		toUpdate = e.getEndpoints().getSecond();
		toUpdate.setDegree(toUpdate.getDegree() - 1);
		super.removeEdge(e);
	}

	@Override
	public WindyGraph getDeepCopy() {
		try {
			WindyGraph ans = new WindyGraph();
			HashMap<Integer, WindyEdge> indexedEdges = this.getInternalEdgeMap();
			WindyVertex temp;
			int n = this.getVertices().size();
			for(int i = 1; i <= n; i++)
			{
				temp = new WindyVertex("deep copy original"); //the new guy
				ans.addVertex(temp, i);
			}
			WindyEdge e, e2;
			ArrayList<Integer> forSorting = new ArrayList<Integer>(indexedEdges.keySet());
			Collections.sort(forSorting);
			int m = forSorting.size();
			for(int i = 0; i < m; i++)
			{
				e = indexedEdges.get(forSorting.get(i));
				e2 = new WindyEdge("deep copy original", new Pair<WindyVertex>(ans.getInternalVertexMap().get(e.getEndpoints().getFirst().getId()), ans.getInternalVertexMap().get(e.getEndpoints().getSecond().getId())), e.getCost(), e.getReverseCost());
				e2.setMatchId(e.getId());
				e2.setRequired(e.isRequired());
				ans.addEdge(e2, e.getId());
			}

			return ans;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	@Override
	public WindyEdge constructEdge(int i, int j, String desc, int cost)
			throws InvalidEndpointsException {
		if(i > this.getVertices().size() || j > this.getVertices().size())
			throw new InvalidEndpointsException();
		return new WindyEdge(desc, new Pair<WindyVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost, cost);

	}

	@Override
	public WindyVertex constructVertex(String desc) {
		return new WindyVertex(desc);
	}


}
