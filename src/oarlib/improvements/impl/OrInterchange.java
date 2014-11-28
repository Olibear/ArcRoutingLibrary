package oarlib.improvements.impl;

import gnu.trove.TIntArrayList;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.route.impl.Tour;
import oarlib.route.util.RouteFlattener;
import oarlib.route.util.WindyRouteExpander;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Improvement Procedure first given in Benavent et al. (2005) New Heuristic Algorithms
 * for the Windy Rural Postman Problem.  This IP operates over the compressed representation
 * of a feasible solution, and tries to swap the positions of two strings of required
 * edges, and re-assess cost.
 *
 * Created by oliverlum on 11/16/14.
 */
public class OrInterchange extends IntraRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {


    public OrInterchange(WindyGraph g, Collection<Route<WindyVertex, WindyEdge>> candidateRoute) {
        super(g, candidateRoute);
    }

    private static final int L = 4;
    private static final int M = 11;

    @Override
    protected Route improveRoute(Route r) {

        Route record = r;
        int recordCost = r.getCost();
        int candidateCost;

        WindyRouteExpander wre = new WindyRouteExpander(getGraph());
        boolean foundImprovement = true;

        Route newRecord = null;
        while (foundImprovement) {

            // defaults
            foundImprovement = false;
            TIntArrayList flattenedRoute = RouteFlattener.flattenRoute(record);
            ArrayList<Boolean> recordTraversalDirection = record.getCompactTraversalDirection();

            int n = flattenedRoute.size();

            Tour candidate;
            //swap them and re expand, and re-assess cost
            for (int i = 0; i < n; i++) { //starting point
                for (int j = 1; j <= L; j++) { //how many to move
                    if(i + j >= n )
                        break;
                    for(int k = -M; k <= M; k++) { //shift

                        //bounds
                        int lowerLim = i + k;
                        int upperLim = lowerLim + j;
                        if(lowerLim < 0)
                            continue;
                        if(k == 0)
                            continue;
                        if(upperLim > n)
                            break;

                        //copy
                        TIntArrayList candidateRoute = new TIntArrayList(flattenedRoute.toNativeArray());
                        ArrayList<Boolean> candidateTraversalDirection = new ArrayList<Boolean>(recordTraversalDirection);
                        int toMoveIndex;
                        boolean toMoveDirection;
                        if(k < 0) {
                            for(int l = 0; l > k; l--) {
                                toMoveIndex = candidateRoute.get(lowerLim);
                                toMoveDirection = candidateTraversalDirection.get(lowerLim);
                                candidateRoute.insert(i+j,toMoveIndex);
                                candidateTraversalDirection.add(i+j,toMoveDirection);
                                candidateRoute.remove(lowerLim);
                                candidateTraversalDirection.remove(lowerLim);
                            }
                        } else if(k > 0) {
                            for(int l = 0; l < k; l++) {
                                toMoveIndex = candidateRoute.get(i+j);
                                toMoveDirection = candidateTraversalDirection.get(i+j);
                                candidateRoute.remove(i+j);
                                candidateTraversalDirection.remove(i+j);
                                candidateRoute.insert(lowerLim,toMoveIndex);
                                candidateTraversalDirection.add(lowerLim, toMoveDirection);
                            }
                        }


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
            }
            record = newRecord;
        }

        return record;
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }
}
