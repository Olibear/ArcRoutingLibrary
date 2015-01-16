/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015 Oliver Lum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package oarlib.improvements.impl;

import gnu.trove.TIntArrayList;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.ImprovementStrategy;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.WindyEdge;
import oarlib.route.impl.Tour;
import oarlib.route.util.RouteExpander;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Improvement Procedure first given in Benavent et al. (2005) New Heuristic Algorithms
 * for the Windy Rural Postman Problem.  This IP operates over the compressed representation
 * of a feasible solution, and tries to swap the positions of two required edges, and re-assess
 * cost.
 * <p/>
 * Created by oliverlum on 11/16/14.
 */
public class TwoInterchange extends IntraRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    public TwoInterchange(Problem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }

    public TwoInterchange(Problem<WindyVertex, WindyEdge, WindyGraph> problem, ImprovementStrategy.Type strat, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, strat, initialSol);
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    @Override
    public Route<WindyVertex, WindyEdge> improveRoute(Route<WindyVertex, WindyEdge> r) {

        Route record = r;
        int recordCost = r.getCost();
        int temp, temp2, candidateCost;
        Boolean tempForward, tempForward2;

        RouteExpander wre = new RouteExpander(getGraph());
        boolean foundImprovement = true;

        Route newRecord = null;
        while (foundImprovement) {

            // defaults
            foundImprovement = false;
            TIntArrayList flattenedRoute = new TIntArrayList(record.getCompactRepresentation().toNativeArray());
            ArrayList<Boolean> traversalDirection = new ArrayList<Boolean>(record.getCompactTraversalDirection());

            int n = flattenedRoute.size();

            Tour candidate;
            //swap them and re expand, and re-assess cost
            for (int i = 0; i < n; i++) {
                temp = flattenedRoute.get(i);
                for (int j = 0; j < i; j++) {
                    temp2 = flattenedRoute.get(j);
                    tempForward = traversalDirection.get(i);
                    tempForward2 = traversalDirection.get(j);

                    //swap
                    flattenedRoute.set(i, temp2);
                    flattenedRoute.set(j, temp);
                    traversalDirection.set(i, tempForward2);
                    traversalDirection.set(j, tempForward);

                    candidate = wre.unflattenRoute(flattenedRoute, traversalDirection);
                    candidateCost = candidate.getCost();
                    if (candidateCost < recordCost) {
                        recordCost = candidateCost;
                        newRecord = candidate;
                        foundImprovement = true;
                        if (mStrat == ImprovementStrategy.Type.FirstImprovement) {
                            return newRecord;
                        }
                    }

                    //undo the swap
                    flattenedRoute.set(i, temp);
                    flattenedRoute.set(j, temp2);
                    traversalDirection.set(i, tempForward);
                    traversalDirection.set(j, tempForward2);

                }
            }

            if (foundImprovement) {
                record = newRecord;
            }
        }

        return record;
    }


}
