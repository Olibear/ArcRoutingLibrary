package arl.core.problem.impl;

import java.util.Collection;

import arl.core.Graph;
import arl.core.Problem;
import arl.core.Route;
/**
 * The Directed Chinese Postman Problem.
 * @author oliverlum
 *
 */
public class DirectedCPP extends Problem{

	public DirectedCPP(Graph g, ObjectiveFunction o) {
		super(g, o);
		// TODO Auto-generated constructor stub
	}

	@Override
	public double evaluateObjective(Collection<Route> routes) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isFeasible(Collection<Route> routes) {
		// TODO Auto-generated method stub
		return false;
	}

}
