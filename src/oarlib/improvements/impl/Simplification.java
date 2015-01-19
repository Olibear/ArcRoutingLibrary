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
import gnu.trove.TIntHashSet;
import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.InterRouteImprovementProcedure;
import oarlib.improvements.util.Utils;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.route.util.RouteExpander;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oliverlum on 12/4/14.
 */
public class Simplification extends InterRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    public Simplification(Problem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }

    public Simplification(Problem<WindyVertex, WindyEdge, WindyGraph> problem, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, null, initialSol);
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, null, ProblemAttributes.NumVehicles.MULTI_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
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
        int counter = 0;

        while (newId != longestRouteId) {

            if (counter > 50)
                break;

            //compile a list of all the required edges traversed by the other routes
            coveredReqEdges.clear();
            for (Route<WindyVertex, WindyEdge> r : ans) {
                if (r.getGlobalId() == longestRouteId)
                    continue;
                TIntArrayList flatR = r.getCompactRepresentation();
                coveredReqEdges.addAll(flatR.toNativeArray());
            }

            //remove them from the longest route
            TIntArrayList flatLongest = new TIntArrayList(longestRoute.getCompactRepresentation().toNativeArray());
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

            counter++;
        }

        return ans;
    }
}
