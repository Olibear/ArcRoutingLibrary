package oarlib.graph.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.MixedEdge;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.util.Pair;
import oarlib.graph.util.UnmatchedPair;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.MixedVertex;
import oarlib.vertex.impl.UndirectedVertex;

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
			toUpdate = e.getEndpoints().getSecond();
			toUpdate.setInDegree(toUpdate.getInDegree()+1);
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
		// TODO Auto-generated method stub
		return null;
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
			for(int i = 1; i< this.getVertices().size()+1; i++)
			{
				ans.addVertex(new MixedVertex("deep copy original"), i);
			}
			MixedEdge e;
			for(int i=1;i<this.getEdges().size()+1;i++)
			{
				e = indexedEdges.get(i);
				ans.addEdge(new MixedEdge("deep copy original", new Pair<MixedVertex>(ans.getInternalVertexMap().get(e.getEndpoints().getFirst().getId()), ans.getInternalVertexMap().get(e.getEndpoints().getSecond().getId())), e.getCost(), e.isDirected()));
			}
			return ans;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
