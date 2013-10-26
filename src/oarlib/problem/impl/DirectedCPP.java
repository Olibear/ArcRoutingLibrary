package oarlib.problem.impl;

import java.util.Collection;

import oarlib.core.Arc;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.DirectedGraph;
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
