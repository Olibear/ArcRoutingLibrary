package oarlib.route.impl;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.ArrayList;
import java.util.List;

/**
 * A tour is a route that must begin and end at the same node.
 *
 * @author Oliver
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
    public boolean checkRoutes(Graph<? extends Vertex, ? extends Link<? extends Vertex>> g) {
        return mRoute.get(0).getEndpoints().getFirst().getId() == mRoute.get(mRoute.size() - 1).getEndpoints().getSecond().getId();

    }

}
