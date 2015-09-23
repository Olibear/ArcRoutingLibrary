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
import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.ImprovementStrategy;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.route.impl.Tour;
import oarlib.route.util.RouteExpander;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Improvement Procedure first given in Benavent et al. (2005) New Heuristic Algorithms
 * for the Windy Rural Postman Problem.  This IP operates over the compressed representation
 * of a feasible solution, and tries to swap the positions of two strings of required
 * edges, and re-assess cost.
 * <p/>
 * Created by oliverlum on 11/16/14.
 */
public class OrInterchange extends IntraRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    private static final int L = 4;
    private static final int M = 11;

    public OrInterchange(Problem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }

    public OrInterchange(Problem<WindyVertex, WindyEdge, WindyGraph> problem, ImprovementStrategy.Type strat, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, strat, initialSol);
    }

    @Override
    public Route<WindyVertex, WindyEdge> improveRoute(Route<WindyVertex, WindyEdge> r) {

        Route<WindyVertex, WindyEdge> record = r;
        int recordCost = r.getCost();
        int candidateCost;

        RouteExpander wre = new RouteExpander(getGraph());
        boolean foundImprovement = true;

        Route<WindyVertex, WindyEdge> newRecord = null;
        while (foundImprovement) {

            // defaults
            foundImprovement = false;
            TIntArrayList flattenedRoute = record.getCompactRepresentation();
            ArrayList<Boolean> recordTraversalDirection = record.getCompactTraversalDirection();

            int n = flattenedRoute.size();

            Tour<WindyVertex, WindyEdge> candidate;
            //swap them and re expand, and re-assess cost
            for (int i = 0; i < n; i++) { //starting point
                for (int j = 1; j <= L; j++) { //how many to move
                    if (i + j >= n)
                        break;
                    for (int k = -M; k <= M; k++) { //shift

                        //bounds
                        int lowerLim = i + k;
                        int upperLim = lowerLim + j;
                        if (lowerLim < 0)
                            continue;
                        if (k == 0)
                            continue;
                        if (upperLim > n)
                            break;

                        //copy
                        TIntArrayList candidateRoute = new TIntArrayList(flattenedRoute.toNativeArray());
                        ArrayList<Boolean> candidateTraversalDirection = new ArrayList<Boolean>(recordTraversalDirection);
                        int toMoveIndex;
                        boolean toMoveDirection;
                        if (k < 0) {
                            for (int l = 0; l > k; l--) {
                                toMoveIndex = candidateRoute.get(lowerLim);
                                toMoveDirection = candidateTraversalDirection.get(lowerLim);
                                candidateRoute.insert(i + j, toMoveIndex);
                                candidateTraversalDirection.add(i + j, toMoveDirection);
                                candidateRoute.remove(lowerLim);
                                candidateTraversalDirection.remove(lowerLim);
                            }
                        } else if (k > 0) {
                            for (int l = 0; l < k; l++) {
                                toMoveIndex = candidateRoute.get(i + j);
                                toMoveDirection = candidateTraversalDirection.get(i + j);
                                candidateRoute.remove(i + j);
                                candidateTraversalDirection.remove(i + j);
                                candidateRoute.insert(lowerLim, toMoveIndex);
                                candidateTraversalDirection.add(lowerLim, toMoveDirection);
                            }
                        }

                        candidate = wre.unflattenRoute(candidateRoute, candidateTraversalDirection);

                        candidateCost = candidate.getCost();
                        if (candidateCost < recordCost) {
                            recordCost = candidateCost;
                            newRecord = candidate;
                            flattenedRoute = record.getCompactRepresentation();
                            foundImprovement = true;

                            if (mStrat == ImprovementStrategy.Type.FirstImprovement) {
                                return newRecord;
                            }
                        }
                    }
                }
            }
            if (foundImprovement)
                record = newRecord;
        }

        return record;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, null, null, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }
}
