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
package oarlib.graph.transform.partition.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.transform.partition.PartitionTransformer;
import oarlib.graph.transform.rebalance.CostRebalancer;
import oarlib.link.impl.MixedEdge;
import oarlib.vertex.impl.MixedVertex;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oliverlum on 9/13/14.
 */
public class PreciseMixedKWayPartitionTransform implements PartitionTransformer<MixedGraph> {

    private MixedGraph mGraph;
    private HashMap<Integer, Integer> mCostMap;
    private boolean mWeighNonReq;
    private boolean usingCostRebalancer;

    public PreciseMixedKWayPartitionTransform(MixedGraph input) {
        this(input, false, null);
    }

    public PreciseMixedKWayPartitionTransform(MixedGraph input, boolean weighNonReq) {
        this(input, weighNonReq, null);
    }

    public PreciseMixedKWayPartitionTransform(MixedGraph input, boolean weighNonReq, CostRebalancer<MixedGraph> costRebalancer) {
        mGraph = input;
        mWeighNonReq = weighNonReq;
        if (costRebalancer != null) {
            mCostMap = costRebalancer.rebalance();
            usingCostRebalancer = true;
        } else {
            usingCostRebalancer = false;
        }

    }

    @Override
    public void setGraph(MixedGraph input) {
        mGraph = input;
    }

    @Override
    public MixedGraph transformGraph() {
        try {
            int m = mGraph.getEdges().size();
            //ans
            MixedGraph ans = new MixedGraph(m);

            //setup
            MixedEdge temp;
            MixedVertex tail, head;
            TIntObjectHashMap<MixedVertex> ansVertices = ans.getInternalVertexMap();
            TIntObjectHashMap<MixedEdge> mEdges = mGraph.getInternalEdgeMap();

            int tempCost;

            for (int i : mEdges.keys()) {
                temp = mEdges.get(i);
                tempCost = temp.getCost();
                if (usingCostRebalancer)
                    tempCost = mCostMap.get(i);
                head = temp.getEndpoints().getSecond();
                tail = temp.getEndpoints().getFirst();

                //assign the cost:
                if (temp.isRequired() || mWeighNonReq)
                    ansVertices.get(i).setCost(tempCost);
                else
                    ansVertices.get(i).setCost(0);

                //figure out the conns
                for (ArrayList<MixedEdge> toAdd : head.getNeighbors().values()) {
                    for (MixedEdge e : toAdd) {
                        //to avoid redundancy and self conns
                        if (e.getId() < i) {
                            ans.addEdge(i, e.getId(), 1);
                        }
                    }
                }

                for (ArrayList<MixedEdge> toAdd : tail.getNeighbors().values()) {
                    for (MixedEdge e : toAdd) {
                        //to avoid redundancy and self conns
                        if (e.getId() < i) {
                            ans.addEdge(i, e.getId(), 1);
                        }
                    }
                }
            }

            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
