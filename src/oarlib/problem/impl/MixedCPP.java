package oarlib.problem.impl;

import java.util.Collection;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Vertex;

public class MixedCPP extends Problem{
	

	public MixedCPP(Graph<Vertex, Link<Vertex>> g, ObjectiveFunction o) {
		super(g, o);
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
	public Graph<?, ?> getGraph() {
		// TODO Auto-generated method stub
		return null;
	}

}
