package oarlib.route.impl;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.link.impl.WindyEdge;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A tour is a route that must begin and end at the same node.
 *
 * @author Oliver
 */
public class Tour<V extends Vertex, E extends Link<V>> extends Route<V,E> {

    public Tour() {
        super();
    }

    @Override
    public boolean checkRoutes(Graph<V, E> g) {
        return mRoute.get(0).getEndpoints().getFirst().getId() == mRoute.get(mRoute.size() - 1).getEndpoints().getSecond().getId();

    }

}
