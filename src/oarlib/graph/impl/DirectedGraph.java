package oarlib.graph.impl;

import java.util.ArrayList;
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

	@Override
	public void addVertex(DirectedVertex v) {
		super.addVertex(v);
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
	public List<Arc> findEdges(Pair<DirectedVertex> endpoints) {
		DirectedVertex first = endpoints.getFirst();
		HashMap<DirectedVertex, ArrayList<Arc>> firstNeighbors = first.getNeighbors();
		return firstNeighbors.get(endpoints.getSecond());
	}

	@Override
	public oarlib.core.Graph.Type getType() {
		return Graph.Type.DIRECTED;
	}

	@Override
	public DirectedGraph getDeepCopy() {
		try {
			DirectedGraph ans = new DirectedGraph();
			for(DirectedVertex v: this.getVertices())
			{
				ans.addVertex(new DirectedVertex("deep copy original"), v.getId());
			}
			for(Arc a : this.getEdges())
			{
				ans.addEdge(new Arc("deep copy original", new Pair<DirectedVertex>(ans.getMatchingVertexMap().get(a.getTail().getId()), ans.getMatchingVertexMap().get(a.getHead().getId())), a.getCost()), a.getId());
			}
			return ans;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
