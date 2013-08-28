package arl.route.impl;

import java.util.List;

import arl.core.Graph;
import arl.core.Link;
import arl.core.Route;
import arl.core.Vertex;
/**
 * A path is a simple route that keeps track of its cost during construction.
 * @author Oliver
 *
 */
public class Path extends Route {

	private double mCost;
	
	public Path()
	{
		mCost = 0;
	}

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
