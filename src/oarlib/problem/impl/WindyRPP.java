package oarlib.problem.impl;

import java.util.Collection;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;

public class WindyRPP extends Problem{
	
	WindyGraph mGraph;

	public WindyRPP(WindyGraph g) {
		mGraph = g;
	}

	@Override
	public boolean isFeasible(Collection<Route> routes) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Type getType() {
		return Problem.Type.WINDY_RURAL_POSTMAN;
	}

	@Override
	public WindyGraph getGraph() {
		return mGraph;
	}

}
