package oarlib.graph.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import oarlib.core.Arc;
import oarlib.core.Graph;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.exceptions.NoCapacitySetException;
import oarlib.exceptions.NoDemandSetException;
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
		ret.addAll(firstNeighbors.get(endpoints.getSecond()));
		DirectedVertex second = endpoints.getSecond();
		HashMap<DirectedVertex, ArrayList<Arc>> secondNeighbors = second.getNeighbors();
		ret.addAll(secondNeighbors.get(endpoints.getFirst()));
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
			for(int i=1;i<this.getVertices().size()+1;i++)
			{
				temp = new DirectedVertex("deep copy original"); //the new guy
				temp2 = indexedVertices.get(i); //the old guy
				if(temp2.isDemandSet())
					temp.setDemand(temp2.getDemand());
				ans.addVertex(temp, i);
				
			}
			for(int i=1; i<this.getEdges().size()+1; i++)
			{
				a = indexedArcs.get(i);
				a2 = new Arc("deep copy original", new Pair<DirectedVertex>(ans.getInternalVertexMap().get(a.getTail().getId()), ans.getInternalVertexMap().get(a.getHead().getId())), a.getCost());
				if(a.isCapacitySet())
					a2.setCapacity(a.getCapacity());
				ans.addEdge(a2, i);
			}
			return ans;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void addEdge(int i, int j, String desc, int cost)
			throws InvalidEndpointsException {
		if(i > this.getVertices().size() || j > this.getVertices().size())
			throw new InvalidEndpointsException();
		this.addEdge(new Arc(desc, new Pair<DirectedVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)),cost));
		
	}

}
