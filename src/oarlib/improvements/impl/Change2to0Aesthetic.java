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

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.ImprovementStrategy;
import oarlib.improvements.InterRouteImprovementProcedure;
import oarlib.improvements.util.CompactMove;
import oarlib.improvements.util.Mover;
import oarlib.improvements.util.Utils;
import oarlib.link.impl.WindyEdge;
import oarlib.metrics.MaxMetric;
import oarlib.metrics.RouteOverlapMetric;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oliverlum on 11/20/14.
 */
public class Change2to0Aesthetic extends InterRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    public Change2to0Aesthetic(Problem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }

    public Change2to0Aesthetic(Problem<WindyVertex, WindyEdge, WindyGraph> problem, ImprovementStrategy.Type strat, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, strat, initialSol);
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, null, ProblemAttributes.NumVehicles.MULTI_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public Collection<Route<WindyVertex, WindyEdge>> improveSolution() {

        //find the longest route
        Route longestRoute = Utils.findLongestRoute(getInitialSol());

        //try to offload each of the links to a cheaper route.
        return offloadTwoEdges(longestRoute);
    }

    private Collection<Route<WindyVertex, WindyEdge>> offloadTwoEdges(Route<WindyVertex, WindyEdge> longestRoute) {

        //aesthetic init
        MaxMetric mm = new MaxMetric();
        RouteOverlapMetric roi = new RouteOverlapMetric(getGraph());
        double aestheticFactor = mm.evaluate(getInitialSol()) / roi.evaluate(getInitialSol());


        Collection<Route<WindyVertex, WindyEdge>> initialSol = getInitialSol();
        int skipId = longestRoute.getGlobalId();
        Mover<WindyVertex, WindyEdge, WindyGraph> mover = new Mover<WindyVertex, WindyEdge, WindyGraph>(getGraph());

        double bestSavings = 0;
        boolean foundImprovement = false;
        ArrayList<CompactMove<WindyVertex, WindyEdge>> bestMoveList = new ArrayList<CompactMove<WindyVertex, WindyEdge>>();
        Collection<Route<WindyVertex, WindyEdge>> ans = new ArrayList<Route<WindyVertex, WindyEdge>>();
        Collection<Route<WindyVertex, WindyEdge>> bestAns = null;

        for (Route<WindyVertex, WindyEdge> r : initialSol) {
            //don't try and move to yourself.
            if (r.getGlobalId() == skipId)
                continue;

            //business logic
            int lim = longestRoute.getCompactRepresentation().size() - 1;
            int lim2 = r.getCompactRepresentation().size() - 1;
            CompactMove<WindyVertex, WindyEdge> temp, temp2;
            ArrayList<CompactMove<WindyVertex, WindyEdge>> moveList = new ArrayList<CompactMove<WindyVertex, WindyEdge>>();
            double savings;

            Route tempLongest, tempR;

            for (int i = 0; i < lim; i++) {
                for (int j = 0; j < lim2; j++) {

                    tempLongest = longestRoute.getDeepCopy();
                    tempR = r.getDeepCopy();

                    temp = new CompactMove<WindyVertex, WindyEdge>(tempLongest, tempR, i, j);
                    temp2 = new CompactMove<WindyVertex, WindyEdge>(tempLongest, tempR, i, j + 1);
                    moveList.clear();
                    moveList.add(temp);
                    moveList.add(temp2);

                    TIntObjectHashMap<Route<WindyVertex, WindyEdge>> routesToChange = mover.makeComplexMove(moveList);
                    for (Route r2 : initialSol) {
                        if (routesToChange.containsKey(r2.getGlobalId())) {
                            ans.add(routesToChange.get(r2.getGlobalId()));
                        } else
                            ans.add(r2);
                    }

                    savings = (mm.evaluate(initialSol) - mm.evaluate(ans)) + (aestheticFactor * (roi.evaluate(initialSol) - roi.evaluate(ans)));

                    if (savings < bestSavings) {
                        bestSavings = savings;
                        bestMoveList = moveList;
                        bestAns = ans;
                        foundImprovement = true;
                        if (mStrat == ImprovementStrategy.Type.FirstImprovement) {
                            return ans;
                        }
                    }

                    ans.clear();
                }
            }
        }

        if (foundImprovement) {
            return bestAns;
        }
        return initialSol;
    }
}
