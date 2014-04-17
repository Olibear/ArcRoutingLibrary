package oarlib.problem.impl;

import java.util.Collection;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.MixedGraph;

public class MixedCPP extends Problem{
	
	private MixedGraph mGraph;	
	
	public MixedCPP(MixedGraph g) {
		mGraph = g;
	}

	@Override
	public boolean isFeasible(Collection<Route> routes) {
		return false;
	}

	@Override
	public Type getType() {
		return Problem.Type.MIXED_CHINESE_POSTMAN;
	}

	@Override
	public MixedGraph getGraph() {
		return mGraph;
	}

}
