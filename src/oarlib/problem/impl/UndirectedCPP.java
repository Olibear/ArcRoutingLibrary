package oarlib.problem.impl;

import java.util.Collection;

import oarlib.core.Edge;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.UndirectedGraph;

public class UndirectedCPP extends Problem{
	
	private UndirectedGraph mGraph;

	public UndirectedCPP(UndirectedGraph g) {
		mGraph = g;
	}

	@Override
	public boolean isFeasible(Collection<Route> routes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type getType() {
		return Problem.Type.UNDIRECTED_CHINESE_POSTMAN;
	}

	@Override
	public UndirectedGraph getGraph() {
		return mGraph;
	}

}
