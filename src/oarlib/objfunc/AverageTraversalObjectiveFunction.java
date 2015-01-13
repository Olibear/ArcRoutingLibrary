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
package oarlib.objfunc;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.util.CommonAlgorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Oliver on 12/26/2014.
 */
public class AverageTraversalObjectiveFunction extends ObjectiveFunction {

    private Graph mGraph;

    public AverageTraversalObjectiveFunction(Graph g) {
        mGraph = g;
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<Route<V, E>> routes) {
        int numRoutes = routes.size();
        int numTasks = 0;
        HashSet<Integer> alreadyTraversed = new HashSet<Integer>();
        int sumDist = 0;

        //compute shortest paths
        int n = mGraph.getVertices().size();
        int[][] dist = new int[n + 1][n + 1];
        int[][] path = new int[n + 1][n + 1];

        CommonAlgorithms.fwLeastCostPaths(mGraph, dist, path);

        //calculuate the pairwise sum
        int tempBest;
        List<E> temp;
        ArrayList<Boolean> tempService;
        for (Route r : routes) {
            temp = r.getRoute();
            tempService = r.getServicingList();
            for (int i = 0; i < temp.size(); i++) {
                if (!tempService.get(i))
                    continue;
                numTasks++;
                E l = temp.get(i);
                int a = l.getEndpoints().getFirst().getId();
                int b = l.getEndpoints().getSecond().getId();
                for (int j = 0; j < temp.size(); j++) {
                    if (!tempService.get(j))
                        continue;
                    E m = temp.get(j);
                    if (l.getId() == m.getId())
                        continue;
                    //check the four possible combinations a-c, a-d, b-c, b-d
                    int c = m.getEndpoints().getFirst().getId();
                    int d = m.getEndpoints().getSecond().getId();

                    tempBest = dist[a][d];
                    if (tempBest > dist[a][c])
                        tempBest = dist[a][c];
                    if (tempBest > dist[b][c])
                        tempBest = dist[b][c];
                    if (tempBest > dist[b][d])
                        tempBest = dist[b][d];

                    sumDist += tempBest;
                }
            }
        }


        double denom = numTasks * (numTasks - numRoutes) / (double) (2 * numRoutes);

        return (double) sumDist / denom;
    }
}
