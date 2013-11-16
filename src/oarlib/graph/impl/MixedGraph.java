package oarlib.graph.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.core.Graph;
import oarlib.core.Link;
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
public class MixedGraph extends MutableGraph<MixedVertex, Link<MixedVertex>>{

	
	//constructors
	public MixedGraph(){
		super();
	}
	
	@Override
	public void addVertex(MixedVertex v) {
		super.addVertex(v);
	}
	
	@Override
	public void addEdge(Link<MixedVertex> e) throws InvalidEndpointsException{
		//handle the two different cases
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
	public Collection<Link<MixedVertex>> findEdges(Pair<MixedVertex> endpoints) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public oarlib.core.Graph.Type getType() {
		return Graph.Type.MIXED;
	}

	@Override
	public Graph<MixedVertex, Link<MixedVertex>> getDeepCopy() {
		try {
			MixedGraph ans = new MixedGraph();
			for(MixedVertex v: this.getVertices())
			{
				ans.addVertex(new MixedVertex("deep copy original"), v.getId());
			}
			for(Link<MixedVertex> e : this.getEdges())
			{
				ans.addEdge(new Link<MixedVertex>("deep copy original", new Pair<MixedVertex>(ans.getMatchingVertexMap().get(e.getEndpoints().getFirst().getId()), ans.getMatchingVertexMap().get(e.getEndpoints().getSecond().getId())), e.getCost()));
			}
			return ans;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
