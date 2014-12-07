package oarlib.improvements.impl;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.InterRouteImprovementProcedure;
import oarlib.improvements.util.Utils;
import oarlib.link.impl.WindyEdge;
import oarlib.route.util.RouteExpander;
import oarlib.route.util.RouteFlattener;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oliverlum on 12/4/14.
 */
public class Simplification extends InterRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    public Simplification(WindyGraph windyGraph, Collection<Route<WindyVertex, WindyEdge>> candidateSol) {
        super(windyGraph, candidateSol);
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    @Override
    public Collection<Route<WindyVertex, WindyEdge>> improveSolution() {

        ArrayList<Route<WindyVertex, WindyEdge>> postIntraRouteDedup = new ArrayList<Route<WindyVertex, WindyEdge>>();
        for (Route<WindyVertex, WindyEdge> r : getInitialSol()) {
            postIntraRouteDedup.add(dedupTraversalsIntraRoute(r));
        }
        return dedupTraversalsInterRoute(postIntraRouteDedup);
    }

    public Route<WindyVertex, WindyEdge> dedupTraversalsIntraRoute(Route<WindyVertex, WindyEdge> r) {
        //go through the path and look for repeatedly traversed links

        //TODO
        return r;
    }

    public Collection<Route<WindyVertex, WindyEdge>> dedupTraversalsInterRoute(Collection<Route<WindyVertex, WindyEdge>> routes) {

        int newId, longestRouteId;
        newId = -1;

        Collection<Route<WindyVertex, WindyEdge>> ans = routes;
        //find the longest route
        Route<WindyVertex, WindyEdge> longestRoute = Utils.findLongestRoute(routes);
        longestRouteId = longestRoute.getGlobalId();
        RouteExpander<WindyGraph> re = new RouteExpander<WindyGraph>(getGraph());

        TIntHashSet coveredReqEdges = new TIntHashSet();
        while (newId != longestRouteId) {

            //compile a list of all the required edges traversed by the other routes
            coveredReqEdges.clear();
            for (Route<WindyVertex, WindyEdge> r : ans) {
                if(r.getGlobalId() == longestRouteId)
                    continue;
                TIntArrayList flatR = RouteFlattener.flattenRoute(r);
                coveredReqEdges.addAll(flatR.toNativeArray());
            }

            //remove them from the longest route
            TIntArrayList flatLongest = RouteFlattener.flattenRoute(longestRoute, true);
            ArrayList<Boolean> longestTD = new ArrayList<Boolean>(longestRoute.getCompactTraversalDirection());
            int size = flatLongest.size();
            for (int i = 0; i < size; i++) {
                if (coveredReqEdges.contains(flatLongest.get(i))) {
                    flatLongest.remove(i);
                    longestTD.remove(i);
                    size--;
                    i--;
                }
            }

            Route<WindyVertex, WindyEdge> newRoute = re.unflattenRoute(flatLongest, longestTD);
            newId = newRoute.getGlobalId();

            ans.remove(longestRoute);
            ans.add(newRoute);

            //find the new longest
            longestRoute = Utils.findLongestRoute(ans);
            longestRouteId = longestRoute.getGlobalId();
        }

        return ans;
    }
}
