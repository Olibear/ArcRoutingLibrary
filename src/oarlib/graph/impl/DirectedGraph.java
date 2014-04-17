package oarlib.graph.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import oarlib.core.Arc;
import oarlib.core.Graph;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;
/**
 * Reperesentation of a Directed Graph; that is, it can only contain arcs, and directed vertices
 * @author Oliver
 *
 * @param <A> Arc that this graph will use
 */
public class DirectedGraph extends MutableGraph<DirectedVertex,Arc> {

	//constructors
	public DirectedGraph(){
		super();
	}

	//====================================================
	//
	// Graph Overrides
	//
	//====================================================

	@Override
	public void addVertex(DirectedVertex v) {
		super.addVertex(v);
	}

	@Override
	public void clearEdges()
	{
		super.clearEdges();
		for(DirectedVertex v: this.getVertices())
		{
			v.setInDegree(0);
			v.setOutDegree(0);
			v.clearNeighbors();
		}
	}

	@Override
	public void addEdge(Arc e) throws InvalidEndpointsException{ 
		e.getTail().addToNeighbors(e.getHead(), e);
		DirectedVertex toUpdate = e.getTail();
		toUpdate.setOutDegree(toUpdate.getOutDegree() + 1);
		toUpdate = e.getHead();
		toUpdate.setInDegree(toUpdate.getInDegree()+1);
		super.addEdge(e);
	}

	@Override
	public void removeEdge(Arc e) throws IllegalArgumentException
	{
		if(!this.getEdges().contains(e))
			throw new IllegalArgumentException();
		e.getTail().removeFromNeighbors(e.getHead(), e);
		DirectedVertex toUpdate = e.getTail();
		toUpdate.setOutDegree(toUpdate.getOutDegree() - 1);
		toUpdate = e.getHead();
		toUpdate.setInDegree(toUpdate.getInDegree() - 1);
		super.removeEdge(e);
	}
	@Override
	public List<Arc> findEdges(Pair<DirectedVertex> endpoints) {
		List<Arc> ret = new ArrayList<Arc>();
		DirectedVertex first = endpoints.getFirst();
		HashMap<DirectedVertex, ArrayList<Arc>> firstNeighbors = first.getNeighbors();
		if(!firstNeighbors.containsKey(endpoints.getSecond()))
			return new ArrayList<Arc>();
		ret.addAll(firstNeighbors.get(endpoints.getSecond()));


		/*
		 * THIS IS THE ONLY CASE WHERE WE IGNORE THIS, IN MIXED, WE ALSO DO BIDIRECTIONAL
		 */
		//DirectedVertex second = endpoints.getSecond();
		//HashMap<DirectedVertex, ArrayList<Arc>> secondNeighbors = second.getNeighbors();
		//ret.addAll(secondNeighbors.get(endpoints.getFirst()));
		return ret;
	}

	@Override
	public oarlib.core.Graph.Type getType() {
		return Graph.Type.DIRECTED;
	}

	@Override
	public DirectedGraph getDeepCopy() {
		try {
			DirectedGraph ans = new DirectedGraph();
			DirectedVertex temp, temp2;
			Arc a, a2;
			HashMap<Integer, DirectedVertex> indexedVertices = this.getInternalVertexMap();
			HashMap<Integer, Arc> indexedArcs = this.getInternalEdgeMap();
			int n = this.getVertices().size();
			int m = this.getEdges().size();
			for(int i = 1;i <=n; i++)
			{
				temp = new DirectedVertex("deep copy original"); //the new guy
				temp2 = indexedVertices.get(i); //the old guy
				if(temp2.isDemandSet())
					temp.setDemand(temp2.getDemand());
				ans.addVertex(temp);
			}
			ArrayList<Integer> forSorting = new ArrayList<Integer>(indexedArcs.keySet());
			Collections.sort(forSorting);
			m = forSorting.size();

			for(int i = 0; i < m; i++)
			{
				a = indexedArcs.get(forSorting.get(i));
				a2 = new Arc("deep copy original", new Pair<DirectedVertex>(ans.getInternalVertexMap().get(a.getTail().getId()), ans.getInternalVertexMap().get(a.getHead().getId())), a.getCost());
				if(a.isCapacitySet())
					a2.setCapacity(a.getCapacity());
				a2.setRequired(a.isRequired());
				a2.setMatchId(a.getId());
				ans.addEdge(a2);
			}
			return ans;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public Arc constructEdge(int i, int j, String desc, int cost)
			throws InvalidEndpointsException {
		if(i > this.getVertices().size() || j > this.getVertices().size())
			throw new InvalidEndpointsException();
		return new Arc(desc, new Pair<DirectedVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)),cost);

	}

	@Override
	public DirectedVertex constructVertex(String desc) {
		return new DirectedVertex(desc);
	}

}
