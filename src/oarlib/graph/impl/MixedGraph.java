package oarlib.graph.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import oarlib.core.Graph;
import oarlib.core.MixedEdge;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.MixedVertex;


/**
 * Representation of  Mixed Graph; that is, it can use both edges and arcs, in tandem with mixed vertices
 * @author Oliver
 *
 */
public class MixedGraph extends MutableGraph<MixedVertex, MixedEdge>{


	//constructors
	public MixedGraph(){
		super();
	}
	
	public MixedGraph(int n) {
		super(n);
	}
	
	//===============================================
	//
	// Adders and Factory methods with isDirected
	//
	//===============================================
	
	public void addEdge(int i, int j, String desc, int cost, boolean isDirected) throws InvalidEndpointsException
	{
		if(i > this.getVertices().size() || j > this.getVertices().size())
			throw new InvalidEndpointsException();
		this.addEdge(this.constructEdge(i, j, desc, cost, isDirected));
	}
	
	public MixedEdge constructEdge(int i, int j, String desc, int cost, boolean isDirected)
			throws InvalidEndpointsException {
		if(i > this.getVertices().size() || j > this.getVertices().size())
			throw new InvalidEndpointsException();
		return new MixedEdge(desc, new Pair<MixedVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost, isDirected);

	}
	
	//===============================================
	//
	// Graph Overrides
	//
	//===============================================

	@Override
	public void addVertex(MixedVertex v) {
		super.addVertex(v);
	}

	@Override
	public void addEdge(MixedEdge e) throws InvalidEndpointsException{
		//handle the two different cases
		if(!this.getVertices().contains(e.getEndpoints().getFirst()) || !this.getVertices().contains(e.getEndpoints().getSecond()))
			throw new InvalidEndpointsException();
		if(e.isDirected())
		{
			e.getEndpoints().getFirst().addToNeighbors(e.getEndpoints().getSecond(), e);
			MixedVertex toUpdate = e.getEndpoints().getFirst();
			toUpdate.setOutDegree(toUpdate.getOutDegree() + 1);
			toUpdate.setDegree(toUpdate.getDegree()+1);
			toUpdate = e.getEndpoints().getSecond();
			toUpdate.setInDegree(toUpdate.getInDegree()+1);
			toUpdate.setDegree(toUpdate.getDegree()+1);
			super.addEdge(e);	
		}
		else 
		{
			Pair<MixedVertex> endpoints = e.getEndpoints();
			endpoints.getFirst().addToNeighbors(endpoints.getSecond(), e);
			endpoints.getSecond().addToNeighbors(endpoints.getFirst(), e);
			MixedVertex toUpdate = endpoints.getFirst();
			toUpdate.setDegree(toUpdate.getDegree()+1);
			toUpdate = e.getEndpoints().getSecond();
			toUpdate.setDegree(toUpdate.getDegree()+1);
			super.addEdge(e);
		}
	}

	@Override
	public void clearEdges()
	{
		super.clearEdges();
		for(MixedVertex v: this.getVertices())
		{
			v.setDegree(0);
			v.setInDegree(0);
			v.setOutDegree(0);
			v.clearNeighbors();
		}
	}

	@Override
	public void removeEdge(MixedEdge e) throws IllegalArgumentException
	{
		if(!this.getEdges().contains(e))
			throw new IllegalArgumentException();
		try {
			if(e.isDirected())
			{
				e.getEndpoints().getFirst().removeFromNeighbors(e.getEndpoints().getSecond(), e);
				MixedVertex toUpdate = e.getEndpoints().getFirst();
				toUpdate.setOutDegree(toUpdate.getOutDegree() - 1);
				toUpdate = e.getEndpoints().getSecond();
				toUpdate.setInDegree(toUpdate.getInDegree() - 1);
				super.removeEdge(e);
			}
			else
			{
				Pair<MixedVertex> endpoints = e.getEndpoints();
				endpoints.getFirst().removeFromNeighbors(endpoints.getSecond(), e);
				endpoints.getSecond().removeFromNeighbors(endpoints.getFirst(), e);
				MixedVertex toUpdate = endpoints.getFirst();
				toUpdate.setDegree(toUpdate.getDegree() - 1);
				toUpdate = e.getEndpoints().getSecond();
				toUpdate.setDegree(toUpdate.getDegree() - 1);
				super.removeEdge(e);
			}
		} catch(Exception ex)
		{
			ex.printStackTrace();
			return;
		}
	}

	@Override
	public List<MixedEdge> findEdges(Pair<MixedVertex> endpoints) {
		List<MixedEdge> ret = new ArrayList<MixedEdge>();
		HashSet<MixedEdge> temp = new HashSet<MixedEdge>(); //to make sure we don't add two copies of an edge
		MixedVertex first = endpoints.getFirst();
		HashMap<MixedVertex, ArrayList<MixedEdge>> firstNeighbors = first.getNeighbors();
		if(firstNeighbors.get(endpoints.getSecond()) != null)
			temp.addAll(firstNeighbors.get(endpoints.getSecond()));
		MixedVertex second = endpoints.getSecond();
		HashMap<MixedVertex, ArrayList<MixedEdge>> secondNeighbors = second.getNeighbors();
		if(secondNeighbors.get(first) != null)
		{
			for(MixedEdge me: secondNeighbors.get(first))
				if(!me.isDirected())
					temp.add(me);
		}
		ret.addAll(temp);
		return ret;
	}

	@Override
	public oarlib.core.Graph.Type getType() {
		return Graph.Type.MIXED;
	}

	@Override
	public MixedGraph getDeepCopy() {
		try {
			MixedGraph ans = new MixedGraph();
			HashMap<Integer, MixedEdge> indexedEdges = this.getInternalEdgeMap();
			HashMap<Integer, MixedVertex> indexedVertices = this.getInternalVertexMap();
			MixedVertex temp, temp2;
			int n = this.getVertices().size();
			int m = this.getEdges().size();
			for(int i = 1; i <= n; i++)
			{
				temp = new MixedVertex("deep copy original"); //the new guy
				temp2 = indexedVertices.get(i); //the old guy
				if(temp2.isDemandSet())
					temp.setDemand(temp2.getDemand());
				ans.addVertex(temp, i);
			}
			MixedEdge e, e2;
			ArrayList<Integer> forSorting = new ArrayList<Integer>(indexedEdges.keySet());
			Collections.sort(forSorting);
			m = forSorting.size();
			for(int i = 0; i < m; i++)
			{
				e = indexedEdges.get(forSorting.get(i));
				e2 = new MixedEdge("deep copy original", new Pair<MixedVertex>(ans.getInternalVertexMap().get(e.getEndpoints().getFirst().getId()), ans.getInternalVertexMap().get(e.getEndpoints().getSecond().getId())), e.getCost(), e.isDirected());
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
	public MixedEdge constructEdge(int i, int j, String desc, int cost)
			throws InvalidEndpointsException {
		if(i > this.getVertices().size() || j > this.getVertices().size())
			throw new InvalidEndpointsException();
		return new MixedEdge(desc, new Pair<MixedVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost);

	}

	@Override
	public MixedVertex constructVertex(String desc) {
		return new MixedVertex(desc);
	}
}
