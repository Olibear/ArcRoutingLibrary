/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016 Oliver Lum
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
 *
 */
package oarlib.improvements.impl;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.ZigZagGraph;
import oarlib.graph.util.Pair;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.ZigZagLink;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.rpp.WindyRPPZZTW;
import oarlib.route.impl.ZigZagTour;
import oarlib.route.util.RouteExporter;
import oarlib.route.util.ZigZagExpander;
import oarlib.solver.impl.WRPPZZTW_PFIH;
import oarlib.vertex.impl.ZigZagVertex;
import org.apache.commons.lang3.text.translate.NumericEntityUnescaper;

import java.util.ArrayList;

/**
 * Created by oliverlum on 9/26/15.
 */
public class ChangeZigZagDecision extends IntraRouteImprovementProcedure<ZigZagVertex, ZigZagLink, ZigZagGraph> {

    public ChangeZigZagDecision(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> problem) {
        super(problem);
    }

    /**
     * Improvement procedure which looks for cases where you zig-zag, but traverse the link in
     * the opposite direction anyways later on; thereby saving the marginal meander cost
     *
     * @param r
     * @return
     */
    @Override
    public ZigZagTour improveRoute(Route<ZigZagVertex, ZigZagLink> r) {

        if (!(r instanceof ZigZagTour))
            return null;

        ZigZagExpander ex = new ZigZagExpander(getGraph(), ((ZigZagTour) r).getPenalty());
        ZigZagTour rCopy = ex.unflattenRoute(r.getCompactRepresentation(), r.getCompactTraversalDirection(), ((ZigZagTour) r).getCompactZZList());

        //cross-check against who we're actually servicing
        ArrayList<Boolean> compactTD = rCopy.getCompactTraversalDirection();
        ArrayList<Boolean> compactZZ = rCopy.getCompactZZList();
        TIntArrayList compactRoute = rCopy.getCompactRepresentation();


        ZigZagLink temp;
        ZigZagTour candidate;
        int bestCost = Integer.MAX_VALUE;
        String fileName = "/Users/oliverlum/Downloads/20node/" + mProblem.getName() + "_ans_101.txt";

        int l = Integer.parseInt(mProblem.getName().substring(0,1));
        int j = Integer.parseInt(mProblem.getName().substring(2,3));
        int k = Integer.parseInt(mProblem.getName().substring(4,5));

        for (int i = 0; i < compactRoute.size(); i++) {

            temp = mProblem.getGraph().getEdge(compactRoute.get(i));
            //only care about zigzag optional links
            if (!(temp.getStatus() == ZigZagLink.ZigZagStatus.OPTIONAL))
                continue;

            //try switching it and resolving
            rCopy.changeZigZagStatus(i);
            RouteExporter.exportRoute(rCopy, RouteExporter.RouteFormat.ZHANG, fileName);
            candidate = WRPPZZTW_PFIH.runIP(mProblem.getGraph(),l,j,k,101,1000);
            if(candidate.getCost() <= bestCost) {
                bestCost = candidate.getCost();
                rCopy = candidate;
            } else {
                rCopy.changeZigZagStatus(i);
            }
        }

        return rCopy;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, ProblemAttributes.Properties.TIME_WINDOWS);
    }
}
