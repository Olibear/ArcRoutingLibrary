package oarlib.problem.impl;

import java.util.Collection;

import oarlib.core.Arc;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.impl.DirectedGraph;
import oarlib.vertex.impl.DirectedVertex;
/**
 * The Directed Chinese Postman Problem.
 * @author oliverlum
 *
 */
public class DirectedCPP extends Problem{
	
	private DirectedGraph<Arc> mGraph;

	public DirectedCPP(DirectedGraph<Arc> g) {
		mGraph = g;
	}

	@Override
	public boolean isFeasible(Collection<Route> routes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type getType() {
		return Problem.Type.DIRECTED_CHINESE_POSTMAN;
	}

	@Override
	public DirectedGraph<Arc> getGraph() {
		return mGraph;
	}

}
