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
package oarlib.graph.transform.rebalance.impl;

import oarlib.core.Factory;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
import oarlib.exceptions.FormatMismatchException;
import oarlib.graph.transform.rebalance.CostRebalancer;
import oarlib.graph.util.CommonAlgorithms;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 3/17/15.
 */
public class ClosestRequiredEdgeRebalancer<S extends Graph<?,?>> extends CostRebalancer<S>{

    double mWeight;
    Factory<S> mFactory;
    int[][] mDist;

    public ClosestRequiredEdgeRebalancer(S input, Factory<S> factory) throws FormatMismatchException {
        super(input);
        mFactory = factory;
        mWeight = 1;
    }

    public ClosestRequiredEdgeRebalancer(S input, Factory<S> factory, double weight) throws FormatMismatchException {
        super(input);
        mFactory = factory;
        mWeight = weight;
    }

    public ClosestRequiredEdgeRebalancer(S input, Factory<S> factory, double weight, CostRebalancer<S> nextRebalancer) {
        super(input, nextRebalancer);
        mFactory = factory;
        mWeight = weight;
    }

    public void setGraph(S input) {
        mGraph = input;
    }

    /**
     * Meant as a performance optimization; you should set this if you're going to do a lot of runs with the same
     * dist matrix.  Then you can compute it once, and set it each time.  Also, gives a little more flexibility if you
     * want to modify the dist matrix before using the rebalancer.
     *
     * @param dist - the distance matrix to be used in rebalancing.
     */
    public void setDistMatrix(int[][] dist) { mDist = dist; }

    @Override
    protected HashMap<Integer, Integer> startRebalance(HashMap<Integer, Integer> input) {

        HashMap<Integer, Integer> ans = new HashMap<Integer, Integer>();

        //find the edges that don't share an endpoint with another required link
        Vertex v1, v2;
        int tempId;
        boolean foundAdjacentReq;
        HashSet<Integer> reqVertexIds = new HashSet<Integer>();
        HashSet<Integer> reqEdgeIds = new HashSet<Integer>();
        for(Link<? extends Vertex> tempEdge : mGraph.getEdges()) {

            if(tempEdge.isRequired()) {
                foundAdjacentReq = false;
                v1 = tempEdge.getEndpoints().getFirst();
                v2 = tempEdge.getEndpoints().getSecond();
                tempId = tempEdge.getId();

                for (Link<? extends Vertex> candidate : v1.getIncidentLinks())
                    if (candidate.isRequired() && candidate.getId() != tempId) {
                        foundAdjacentReq = true;
                        break;
                    }

                if (foundAdjacentReq) {
                    ans.put(tempEdge.getId(), input.get(tempEdge.getId()));
                    continue;
                }

                for (Link<? extends Vertex> candidate : v2.getIncidentLinks())
                    if (candidate.isRequired() && candidate.getId() != tempId) {
                        foundAdjacentReq = true;
                        break;
                    }

                if (foundAdjacentReq) {
                    ans.put(tempEdge.getId(), input.get(tempEdge.getId()));
                    continue;
                }

                reqVertexIds.add(v1.getId());
                reqVertexIds.add(v2.getId());
                reqEdgeIds.add(tempEdge.getId());
            } else {
                ans.put(tempEdge.getId(), input.get(tempEdge.getId()));
            }

        }

        //calculate shortest paths
        int n = mGraph.getVertices().size();
        int[][] path = new int[n + 1][n + 1];

        if(mDist == null)
            CommonAlgorithms.fwLeastCostPaths(mGraph, mDist, path);

        int id1, id2;
        int min, minId;
        for(Integer i : reqEdgeIds) {
            Link<? extends Vertex> tempEdge = mGraph.getEdge(i);
            id1 = tempEdge.getEndpoints().getFirst().getId();
            id2 = tempEdge.getEndpoints().getSecond().getId();

            min = Integer.MAX_VALUE;
            for(Integer j : reqVertexIds) {
                if(j == id1 || j == id2)
                    continue;
                if(mDist[id1][j] < min) {
                    minId = id1;
                    min = mDist[id1][j];
                }
                if(mDist[id2][j] < min) {
                    minId = id2;
                    min = mDist[id2][j];
                }
            }
            ans.put(i, (int)(input.get(i) + mWeight * min));
        }
        return ans;
    }
}
