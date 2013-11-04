package oarlib.problem.impl;

import java.util.Collection;
import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.UndirectedGraph;

public class WindyCPP extends Problem{
	
	UndirectedGraph mGraph;

	public WindyCPP(UndirectedGraph g) {
		mGraph = g;
	}

	@Override
	public boolean isFeasible(Collection<Route> routes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type getType() {
		return Problem.Type.WINDY_CHINESE_POSTMAN;
	}

	@Override
	public Graph<?, ?> getGraph() {
		return mGraph;
	}

}
