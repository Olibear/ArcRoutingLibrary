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
package oarlib.graph.transform.rebalance.impl;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
import oarlib.exceptions.FormatMismatchException;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.rebalance.CostRebalancer;
import oarlib.graph.util.CommonAlgorithms;

import java.util.HashMap;

/**
 * Created by oliverlum on 9/14/14.
 */
public class IndividualDistanceToDepotRebalancer<S extends Graph<?, ?>> extends CostRebalancer<S> {


    double mWeight;

    /**
     * Super constructor for any Rebalance transformers.
     *
     * @param input - the input graph.
     */
    public IndividualDistanceToDepotRebalancer(S input) throws FormatMismatchException {
        super(input, null);
        mWeight = 1;
    }

    public IndividualDistanceToDepotRebalancer(S input, double weight) throws FormatMismatchException {
        super(input, null);
        mWeight = weight;
    }

    public IndividualDistanceToDepotRebalancer(S input, double weight, CostRebalancer<S> nextRebalancer) {
        super(input, nextRebalancer);
        mWeight = weight;
    }

    public void setGraph(S input) {
        mGraph = input;
    }

    /**
     * Rebalances the graph, (which means that this transformer assumes a PartitionTransformer has already been run on
     * this graph).  Each vertex weight gets augmented the cost of its dijkstra distance.
     *
     * @return - a new vertex-weighted graph ready for METIS partitioning.
     */
    @Override
    protected HashMap<Integer, Integer> startRebalance(HashMap<Integer, Integer> input) {

        HashMap<Integer, Integer> ans = new HashMap<Integer, Integer>();
        boolean isWindy = false;
        if (mGraph.getClass() == WindyGraph.class)
            isWindy = true;

        //calculate shortest paths
        int n = mGraph.getVertices().size();
        int[] dist = new int[n + 1];
        int[] path = new int[n + 1];
        CommonAlgorithms.dijkstrasAlgorithm(mGraph, mGraph.getDepotId(), dist, path);

        //update the vertex weights for each
        int higherCost;
        for (Link<? extends Vertex> tempEdge : mGraph.getEdges()) {
            higherCost = Math.max(dist[tempEdge.getEndpoints().getFirst().getId()], dist[tempEdge.getEndpoints().getSecond().getId()]);
            ans.put(tempEdge.getId(), input.get(tempEdge.getId()) + (int) (mWeight * higherCost));
        }

        return ans;
    }
}
