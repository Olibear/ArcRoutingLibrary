package oarlib.improvements.impl;

import gnu.trove.TIntArrayList;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.WindyEdge;
import oarlib.route.impl.Tour;
import oarlib.route.util.RouteFlattener;
import oarlib.route.util.WindyRouteExpander;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Improvement Procedure first given in Benavent et al. (2005) New Heuristic Algorithms
 * for the Windy Rural Postman Problem.  This IP operates over the compressed representation
 * of a feasible solution, and tries to swap the positions of two required edges, and re-assess
 * cost.
 *
 * Created by oliverlum on 11/16/14.
 */
public class TwoInterchange extends IntraRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    public TwoInterchange(WindyGraph g, Collection<Route<WindyVertex, WindyEdge>> candidateRoute) {
        super(g, candidateRoute);
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    @Override
    protected Route improveRoute(Route r) {

        Route record = r;
        int recordCost = r.getCost();
        int temp, temp2, candidateCost;
        Boolean tempForward, tempForward2;

        WindyRouteExpander wre = new WindyRouteExpander(getGraph());
        boolean foundImprovement = true;

        Route newRecord = null;
        while(foundImprovement) {

            // defaults
            foundImprovement = false;
            TIntArrayList flattenedRoute = RouteFlattener.flattenRoute(record, true);

            int n = flattenedRoute.size();

            Tour candidate;
            //swap them and re expand, and re-assess cost
            for (int i = 0; i < n; i++) {
                temp = flattenedRoute.get(i);
                for (int j = 0; j < i; j++) {
                    temp2 = flattenedRoute.get(j);
                    //copy TODO: instead of recopying, just undo the swap for perf
                    TIntArrayList candidateRoute = new TIntArrayList(flattenedRoute.toNativeArray());
                    ArrayList<Boolean> candidateTraversalDirection = new ArrayList<Boolean>(record.getCompactTraversalDirection());
                    tempForward = candidateTraversalDirection.get(i);
                    tempForward2 = candidateTraversalDirection.get(j);

                    //swap
                    candidateRoute.set(i, temp2);
                    candidateRoute.set(j, temp);
                    candidateTraversalDirection.set(i, tempForward2);
                    candidateTraversalDirection.set(j, tempForward);

                    candidate = wre.unflattenRoute(candidateRoute, candidateTraversalDirection);
                    candidateCost = candidate.getCost();
                    if (candidateCost < recordCost) {
                        recordCost = candidateCost;
                        newRecord = candidate;
                        flattenedRoute = RouteFlattener.flattenRoute(record);
                        foundImprovement = true;
                    }

                }
            }

            if(foundImprovement)
                record = newRecord;
        }

        return record;
    }


}
