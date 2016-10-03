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
import oarlib.link.impl.WindyEdge;
import oarlib.metrics.MaxMetric;
import oarlib.metrics.RouteOverlapMetric;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by oliverlum on 11/20/14.
 */
public class Change1to0Aesthetic extends InterRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    public Change1to0Aesthetic(Problem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }

    public Change1to0Aesthetic(Problem<WindyVertex, WindyEdge, WindyGraph> problem, ImprovementStrategy.Type strat, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, strat, initialSol);
    }

    private static boolean isBetter(Collection<Route<WindyVertex, WindyEdge>> col1, Collection<Route<WindyVertex, WindyEdge>> col2) {
        ArrayList<Integer> costs1 = new ArrayList<Integer>();
        ArrayList<Integer> costs2 = new ArrayList<Integer>();

        for (Route r : col1)
            costs1.add(r.getCost());
        for (Route r : col2)
            costs2.add(r.getCost());

        Collections.sort(costs1);
        Collections.sort(costs2);

        int n = costs1.size();
        for (int i = 1; i <= n; i++) {
            if (costs1.get(n - i).equals(costs2.get(n - i)))
                continue;
            return costs1.get(n - i) < costs2.get(n - i);
        }
        return false;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, null, ProblemAttributes.NumVehicles.MULTI_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public Collection<Route<WindyVertex, WindyEdge>> improveSolution() {

        Collection<Route<WindyVertex, WindyEdge>> workingSol = getInitialSol();
        Collection<Route<WindyVertex, WindyEdge>> bestSol = getInitialSol();

        boolean improved = true;

        while (improved) {
            bestSol = workingSol;
            improved = false;
            for (Route<WindyVertex, WindyEdge> r : bestSol) {
                workingSol = offloadOneEdge(r, bestSol);
                if (isBetter(workingSol, bestSol)) {
                    improved = true;
                    break;
                }
            }
        }

        return bestSol;
    }

    private Collection<Route<WindyVertex, WindyEdge>> offloadOneEdge(Route<WindyVertex, WindyEdge> longestRoute, Collection<Route<WindyVertex, WindyEdge>> init) {

        //aesthetic init
        //MaxMetric mm = new MaxMetric();
        //RouteOverlapMetric roi = new RouteOverlapMetric(getGraph());
        //double aestheticFactor = mm.evaluate(getInitialSol()) / roi.evaluate(getInitialSol());

        Collection<Route<WindyVertex, WindyEdge>> initialSol = init;
        int skipId = longestRoute.getGlobalId();
        Mover<WindyVertex, WindyEdge, WindyGraph> mover = new Mover<WindyVertex, WindyEdge, WindyGraph>(getGraph());

        double maxSavings = 0;
        boolean foundImprovement = false;
        ArrayList<CompactMove<WindyVertex, WindyEdge>> bestMoveList = new ArrayList<CompactMove<WindyVertex, WindyEdge>>();
        Collection<Route<WindyVertex, WindyEdge>> ans = new ArrayList<Route<WindyVertex, WindyEdge>>();
        Collection<Route<WindyVertex, WindyEdge>> bestAns = null;

        Route tempLongest, tempR;

        for (Route<WindyVertex, WindyEdge> r : initialSol) {
            //don't try and move to yourself.
            if (r.getGlobalId() == skipId)
                continue;
            //don't try to move to a more expensive route.
            if (r.getCost() > longestRoute.getCost())
                continue;

            //business logic
            int lim = longestRoute.getCompactRepresentation().size();
            int lim2 = r.getCompactRepresentation().size();
            CompactMove<WindyVertex, WindyEdge> temp;
            ArrayList<CompactMove<WindyVertex, WindyEdge>> moveList = new ArrayList<CompactMove<WindyVertex, WindyEdge>>();
            double savings;
            int moddedMax;
            int newCost;
            boolean pd;

            for (int i = 0; i < lim; i++) {
                for (int j = 0; j < lim2; j++) {
                    for (int k = 0; k < 2; k++) {

                        pd = k % 2 == 0;

                        tempLongest = longestRoute.getDeepCopy();
                        tempR = r.getDeepCopy();
                        temp = new CompactMove<WindyVertex, WindyEdge>(tempLongest, tempR, i, j);
                        temp.setPrudentDirection(pd);
                        moveList.clear();
                        moveList.add(temp);

                        TIntObjectHashMap<Route<WindyVertex, WindyEdge>> routesToChange = mover.makeComplexMove(moveList);
                        moddedMax = Integer.MIN_VALUE;
                        for (Route r2 : initialSol) {
                            if (routesToChange.containsKey(r2.getGlobalId())) {
                                ans.add(routesToChange.get(r2.getGlobalId()));
                                newCost = routesToChange.get(r2.getGlobalId()).getCost();
                                if (newCost > moddedMax)
                                    moddedMax = newCost;
                            } else {
                                ans.add(r2);
                            }

                        }

                        //savings = (mm.evaluate(initialSol) - mm.evaluate(ans)) + (aestheticFactor * (roi.evaluate(initialSol) - roi.evaluate(ans)));
                        savings = moddedMax - longestRoute.getCost();
                        if (savings < maxSavings) {
                            maxSavings = savings;
                            foundImprovement = true;
                            bestAns = ans;
                            bestMoveList = moveList;
                            if (mStrat == ImprovementStrategy.Type.FirstImprovement) {
                                for (Route r3 : ans)
                                    if (r3.getCompactRepresentation().size() == 0)
                                        System.out.println("DEBUG");
                                return ans;
                            }
                        }

                        ans.clear();
                    }

                }
            }
        }


        if (foundImprovement) {
            return bestAns;
        }
        return initialSol;
    }
}
