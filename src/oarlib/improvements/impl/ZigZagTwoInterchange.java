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
import oarlib.graph.impl.ZigZagGraph;
import oarlib.improvements.ImprovementStrategy;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.ZigZagLink;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.route.impl.ZigZagTour;
import oarlib.route.util.ZigZagExpander;
import oarlib.vertex.impl.ZigZagVertex;
import org.apache.log4j.Logger;

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
public class ZigZagTwoInterchange extends IntraRouteImprovementProcedure<ZigZagVertex, ZigZagLink, ZigZagGraph> {

    private static final Logger LOGGER = Logger.getLogger(ZigZagTwoInterchange.class);

    public ZigZagTwoInterchange(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> problem) {
        super(problem);
    }

    public ZigZagTwoInterchange(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> problem, ImprovementStrategy.Type strat, Collection<Route<ZigZagVertex, ZigZagLink>> initialSol) {
        super(problem, strat, initialSol);
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, null, null, ProblemAttributes.NumDepots.SINGLE_DEPOT, ProblemAttributes.Properties.TIME_WINDOWS);
    }

    @Override
    public ZigZagTour improveRoute(Route<ZigZagVertex, ZigZagLink> r) {

        //check type
        if (!(r instanceof ZigZagTour)) {
            LOGGER.warn("This improvement procedure only works with ZigZagTours.");
            return null;
        }

        ZigZagTour record = (ZigZagTour) r;
        int recordCost = r.getCost();
        int temp, temp2, candidateCost;
        Boolean tempForward, tempForward2, tempZig, tempZig2;

        ZigZagExpander wre = new ZigZagExpander(getGraph(), record.getPenalty());
        boolean foundImprovement = true;

        ZigZagTour newRecord = null;
        while (foundImprovement) {

            // defaults
            foundImprovement = false;
            TIntArrayList flattenedRoute = new TIntArrayList(record.getCompactRepresentation().toNativeArray());
            ArrayList<Boolean> traversalDirection = new ArrayList<Boolean>(record.getCompactTraversalDirection());
            ArrayList<Boolean> zigzagList = new ArrayList<Boolean>(record.getCompactZZList());

            int n = flattenedRoute.size();

            ZigZagTour candidate;
            //swap them and re expand, and re-assess cost
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < i; j++) {
                    temp = flattenedRoute.get(i);
                    temp2 = flattenedRoute.get(j);
                    tempForward = traversalDirection.get(i);
                    tempForward2 = traversalDirection.get(j);
                    tempZig = zigzagList.get(i);
                    tempZig2 = zigzagList.get(j);

                    //swap
                    flattenedRoute.set(i, temp2);
                    flattenedRoute.set(j, temp);
                    traversalDirection.set(i, tempForward2);
                    traversalDirection.set(j, tempForward);
                    zigzagList.set(i, tempZig2);
                    zigzagList.set(j, tempZig);

                    try {
                        candidate = wre.unflattenRoute(flattenedRoute, traversalDirection, zigzagList);
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
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
                    zigzagList.set(i, tempZig);
                    zigzagList.set(j, tempZig2);

                }
            }

            if (foundImprovement) {
                LOGGER.debug("Solution improved.");
                record = newRecord;
            }
        }

        return record;
    }


}
