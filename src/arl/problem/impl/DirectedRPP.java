package arl.problem.impl;

import java.util.Collection;
import arl.core.Vertex;
import arl.core.Link;
import arl.core.Graph;
import arl.core.Problem;
import arl.core.Route;

/**
 * The Directed Rural Postman Problem.
 * @author oliverlum
 *
 */
public class DirectedRPP extends Problem {

	public DirectedRPP(Graph<Vertex, Link<Vertex>> g, ObjectiveFunction o) {
		super(g, o);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isFeasible(Collection<Route> routes) {
		// TODO Auto-generated method stub
		return false;
	}

}
