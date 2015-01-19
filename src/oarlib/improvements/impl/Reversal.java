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
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.route.util.RouteExpander;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oliverlum on 11/16/14.
 */
public class Reversal extends IntraRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    private static final Logger LOGGER = Logger.getLogger(Reversal.class);

    public Reversal(Problem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }

    public Reversal(Problem<WindyVertex, WindyEdge, WindyGraph> problem, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, null, initialSol);
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, null, null, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public Route<WindyVertex, WindyEdge> improveRoute(Route<WindyVertex, WindyEdge> r) {

        TIntArrayList flattenedRoute = r.getCompactRepresentation();
        DirectedGraph optimalDirection = constructDirDAG(r, flattenedRoute);

        int m = flattenedRoute.size();
        ArrayList<Boolean> newDirection = determineDirection(optimalDirection, m);

        Route ret = reconstructRoute(newDirection, flattenedRoute);

        LOGGER.debug("Original route cost: " + r.getCost());
        LOGGER.debug("New route cost: " + ret.getCost());

        return ret;
    }

    private DirectedGraph constructDirDAG(Route<WindyVertex, WindyEdge> r, TIntArrayList flattenedRoute) {

        WindyGraph mGraph = getGraph();
        int n = mGraph.getVertices().size();

        //calculate shortest paths
        int[][] dist = new int[n + 1][n + 1];
        int[][] path = new int[n + 1][n + 1];

        CommonAlgorithms.fwLeastCostPaths(mGraph, dist, path);

        //self-distances need to be zero
        for (int i = 1; i <= n; i++) {
            if (dist[i][i] > 0)
                dist[i][i] = 0;
        }

        //check for argument legality
        int m = flattenedRoute.size();

        /*
         * In the following graph, the indexing is as follows:
         * 1, 2: These are dummy nodes which we connect to represent the cost of going from the depot to i1 and j2 respectively
         * 3 - (2m + 3): These are i1, j1, i2, j2, ..., im, jm, im+1
         * 2m + 4 - 4m + 4: These are j1', i1', j2', i2', ..., jm', im', jm+1'
         */
        DirectedGraph optimalDirection = new DirectedGraph(4 * m + 4);
        WindyEdge temp, temp2;
        int tempIndex = 3;
        int offset = 2 * m + 1;
        int currFirst, currSecond, nextFirst, nextSecond;

        try {

            temp = mGraph.getEdge(flattenedRoute.get(0));
            optimalDirection.addEdge(1, 3, dist[mGraph.getDepotId()][temp.getEndpoints().getFirst().getId()]);
            optimalDirection.addEdge(2, 2 * m + 4, dist[mGraph.getDepotId()][temp.getEndpoints().getSecond().getId()]);

            for (int i = 0; i < m; i++) {

                temp = mGraph.getEdge(flattenedRoute.get(i));
                currFirst = temp.getEndpoints().getFirst().getId();
                currSecond = temp.getEndpoints().getSecond().getId();

                //add the traversal arcs
                optimalDirection.addEdge(tempIndex, tempIndex + 1, temp.getCost());
                optimalDirection.addEdge(tempIndex + offset, tempIndex + offset + 1, temp.getReverseCost());

                if (i + 1 == m) {
                    nextFirst = mGraph.getDepotId();
                    nextSecond = mGraph.getDepotId();
                } else {
                    temp2 = mGraph.getEdge(flattenedRoute.get(i + 1));
                    nextFirst = temp2.getEndpoints().getFirst().getId();
                    nextSecond = temp2.getEndpoints().getSecond().getId();
                }

                //add the shortest path arcs
                optimalDirection.addEdge(tempIndex + 1, tempIndex + 2, dist[currSecond][nextFirst]);
                optimalDirection.addEdge(tempIndex + 1, tempIndex + offset + 2, dist[currSecond][nextSecond]);
                optimalDirection.addEdge(tempIndex + offset + 1, tempIndex + 2, dist[currFirst][nextFirst]);
                optimalDirection.addEdge(tempIndex + offset + 1, tempIndex + offset + 2, dist[currFirst][nextSecond]);

                tempIndex += 2;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; //fail fast
        }

        return optimalDirection;
    }

    private ArrayList<Boolean> determineDirection(DirectedGraph g, int m) {

        ArrayList<Boolean> ans = new ArrayList<Boolean>();

        int n = g.getVertices().size();
        int[] dist = new int[n + 1];
        int[] path = new int[n + 1];
        int[] dist2 = new int[n + 1];
        int[] path2 = new int[n + 1];
        int[] truePath;

        CommonAlgorithms.dijkstrasAlgorithm(g, 1, dist, path);
        CommonAlgorithms.dijkstrasAlgorithm(g, 2, dist2, path2);

        int start, end, next;
        boolean matters = false;

        //figure out which path
        if (dist[2 * m + 3] < dist2[4 * m + 4]) {
            start = 1;
            end = 2 * m + 3;
            truePath = path;
        } else {
            start = 2;
            end = 4 * m + 4;
            truePath = path2;
        }

        //figure out the path
        int threshold = 2 * m + 3;
        do {
            next = truePath[end];
            if (matters) {
                if (next < threshold)
                    ans.add(0, true);
                else
                    ans.add(0, false);
            }
            matters = !matters;
        } while ((end = next) != start);


        return ans;
    }

    private Route reconstructRoute(ArrayList<Boolean> newDirection, TIntArrayList origRoute) {

        RouteExpander wre = new RouteExpander(getGraph());
        return wre.unflattenRoute(origRoute, newDirection);

    }
}