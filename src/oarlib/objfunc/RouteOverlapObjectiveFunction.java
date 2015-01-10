package oarlib.objfunc;

import gnu.trove.TIntHashSet;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Oliver on 12/26/2014.
 */
public class RouteOverlapObjectiveFunction extends ObjectiveFunction {

    private Graph<? extends Vertex, ? extends Link<? extends Vertex>> mGraph;
    private int mN;

    public <V extends Vertex, E extends Link<V>> RouteOverlapObjectiveFunction(Graph<V, E> g) {

        mGraph = g;

        TIntHashSet ids = new TIntHashSet();
        for (Link l : mGraph.getEdges()) {
            if (l.isRequired()) {
                ids.add(l.getFirstEndpointId());
                ids.add(l.getSecondEndpointId());
            }
        }
        mN = ids.size();
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<Route<V, E>> routes) {
        int no = calcNO(routes);
        int numRoutes = routes.size();

        return (no - mN) / (Math.pow(Math.sqrt(numRoutes) + Math.sqrt(mN) - 1, 2) - mN);
    }

    private <V extends Vertex, E extends Link<V>> int calcNO(Collection<Route<V, E>> sol) {

        int NO = 0;
        HashSet<Integer> traversedIds = new HashSet<Integer>();

        for (Route<V, E> r : sol) {
            traversedIds.clear();
            List<E> temp = r.getRoute();
            E tempLink;
            ArrayList<Boolean> tempService = r.getServicingList();
            for (int i = 0; i < temp.size(); i++) {
                if (tempService.get(i)) {
                    tempLink = temp.get(i);
                    if (!traversedIds.contains(tempLink.getFirstEndpointId())) {
                        NO++;
                        traversedIds.add(tempLink.getFirstEndpointId());
                    }
                    if (!traversedIds.contains(tempLink.getSecondEndpointId())) {
                        NO++;
                        traversedIds.add(tempLink.getSecondEndpointId());
                    }
                }
            }
        }

        return NO;
    }
}
