package oarlib.problem.impl;

import java.util.Collection;

import oarlib.core.Arc;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.impl.DirectedGraph;

/**
 * The Directed Rural Postman Problem.
 * @author oliverlum
 *
 */
public class DirectedRPP extends Problem {
	
	private DirectedGraph mGraph;

	public DirectedRPP(DirectedGraph g) {
		mGraph = g;
	}

	@Override
	public boolean isFeasible(Collection<Route> routes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type getType() {
		return Problem.Type.DIRECTED_RURAL_POSTMAN;
	}

	@Override
	public DirectedGraph getGraph() {
		return mGraph;
	}

}
