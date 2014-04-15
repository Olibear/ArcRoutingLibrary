package oarlib.route.impl;

import java.util.List;

import oarlib.core.Edge;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
/**
 * A path is a simple route that keeps track of its cost during construction.
 * @author Oliver
 *
 */
public class Path extends Route {

	public Path()
	{
		super();
	}

	@Override
	public List<Edge> getRoute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void appendEdge(Link<? extends Vertex> l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkRoutes(Graph<? extends Vertex, Link<? extends Vertex>> g) {
		// TODO Auto-generated method stub
		return false;
	}


}
