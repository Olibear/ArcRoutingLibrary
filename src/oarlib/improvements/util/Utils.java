package oarlib.improvements.util;

import gnu.trove.TIntArrayList;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.route.util.RouteExpander;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Created by oliverlum on 11/29/14.
 */
public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class);
    /**
     * fetches the id of the longest route in the initial solution passed in.
     *
     * @return the id of the longest route in the initial solution provided by this object.
     */
    public static <V extends Vertex, E extends Link<V>> Route<V,E> findLongestRoute(Collection<Route<V,E>> routes){
        int max = Integer.MIN_VALUE;
        Route<V,E> ret = null;
        for(Route<V,E> r: routes) {
            if(r.getCost() > max) {
                max = r.getCost();
                ret = r;
            }
        }
        if(ret == null) {
            LOGGER.debug("We were unable to find a longest route.  This is most likely because the collection apssed in was empty. ");
        }
        return ret;
    }

    /**
     * Determines which of the two candidate solutions is better, (currently only min-max objective, but will support more in the future)
     * @param sol1 - first candidate solution
     * @param sol2 - second candidate solution
     * @return - the superior candidate solution
     */
    public static <V extends Vertex, E extends Link<V>> Collection<Route<V, E>> compareSolutions(Collection<Route<V,E>> sol1, Collection<Route<V,E>> sol2) {

        int cost1 = findLongestRoute(sol1).getCost();
        int cost2 = findLongestRoute(sol2).getCost();

        if(cost1 < cost2)
            return sol1;
        else
            return sol2;
    }

    /**
     * Collapses a collection of routes into a single global tour by appending and deduping all the compact representations
     * of the routes.  The ordering of the visitation is given by the ordering of the iterator over the collection.
     *
     * @param routes - the routes to be collapsed
     * @return - the global tour
     */
    public static <V extends Vertex, E extends Link<V>, G extends Graph<V,E>> Route<V,E> aggregateIntoGlobalTour(Collection<Route<V,E>> routes, G graph) {
        RouteExpander<G> re = new RouteExpander<G>(graph);
        TIntArrayList flatGlobal = new TIntArrayList();
        ArrayList<Boolean> globalDir = new ArrayList<Boolean>();

        HashSet<Integer> alreadyAdded = new HashSet<Integer>();

        TIntArrayList tempFlat;
        int tempId;
        for(Route<V,E> r: routes) {
            tempFlat = r.getCompactRepresentation();
            int limi = tempFlat.size();
            for(int i = 0; i < limi; i ++) {
                tempId = tempFlat.get(i);
                if(!alreadyAdded.contains(tempId)) {
                    alreadyAdded.add(tempId);
                    flatGlobal.add(tempId);
                    globalDir.add(r.getCompactTraversalDirection().get(i));
                }
            }
        }

        return re.unflattenRoute(flatGlobal, globalDir);
    }

}
