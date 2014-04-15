package oarlib.route.impl;

import java.util.ArrayList;
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

	private ArrayList<Link<? extends Vertex>> mRoute;
	
	public Tour() {
		super();
		mRoute = new ArrayList<Link<? extends Vertex>>();
	}

	@Override
	public List<Link<? extends Vertex>> getRoute() {
		return mRoute;
	}

	@Override
	public void appendEdge(Link<? extends Vertex> l) {
		mRoute.add(l);
		mCost += l.getCost();
	}

	@Override
	public void prependVertex(Link<? extends Vertex> l) {
		mRoute.add(0, l);
		mCost+=l.getCost();
		
	}

	@Override
	public void insertVertex(Link<? extends Vertex> l, int position)
			throws IndexOutOfBoundsException {
		mRoute.add(0, l);
		mCost+=l.getCost();
	}

	@Override
	public boolean checkRoutes(Graph<? extends Vertex, Link<? extends Vertex>> g) {
		if(mRoute.get(0).getEndpoints().getFirst().getId() == mRoute.get(mRoute.size()-1).getEndpoints().getSecond().getId())
			return true;
		return false;
		
	}

}
