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
public class Tour<V extends Vertex, E extends Link<V>> extends Route<V,E> {

    private ArrayList<E> mRoute;

    public Tour() {
        super();
        mRoute = new ArrayList<E>();
    }

    @Override
    public List<E> getRoute() {
        return mRoute;
    }

    @Override
    public void appendEdge(E l) {
        mRoute.add(l);
        mCost += l.getCost();
        if (l.isRequired())
            mReqCost += l.getCost();
    }

    @Override
    public boolean checkRoutes(Graph<V, E> g) {
        return mRoute.get(0).getEndpoints().getFirst().getId() == mRoute.get(mRoute.size() - 1).getEndpoints().getSecond().getId();

    }

}
