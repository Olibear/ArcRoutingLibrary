package oarlib.route.impl;

import java.util.List;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

/**
 * A tour must begin and end at the same node.  For convenience, this is always maintained, so there is no need to add the starting vertex at the end again.
 * @author Oliver
 *
 */
public class Tour extends Route {

	@Override
	public List<Vertex> getRoute() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void appendVertex(Vertex v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void prependVertex(Vertex v) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void insertVertex(Vertex v, int position)
			throws IndexOutOfBoundsException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean checkRoutes(Graph<Vertex, Link<Vertex>> g) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void tryAddVertex(int position) {
		// TODO Auto-generated method stub
		
	}


}
