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

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
import oarlib.exceptions.FormatMismatchException;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.rebalance.CostRebalancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by oliverlum on 10/27/14.
 * Takes each edge that we know we're going to have to duplicate and doubles its cost.  This is to stop long chains from
 * throwing off the partition
 */
public class DuplicateEdgeCostRebalancer<S extends Graph<?, ?>> extends CostRebalancer<S> {

    /**
     * Super constructor for any Rebalance transformers.
     *
     * @param input - the input graph.
     * @throws oarlib.exceptions.FormatMismatchException - if the ArrayList is of the wrong size.
     */
    public DuplicateEdgeCostRebalancer(S input) throws FormatMismatchException {
        super(input);
    }

    public DuplicateEdgeCostRebalancer(S input, CostRebalancer<S> nextRebalancer) {
        super(input, nextRebalancer);
    }

    public void setGraph(S input) {
        mGraph = input;
    }

    @Override
    protected HashMap<Integer, Integer> startRebalance(HashMap<Integer, Integer> input) {
        Graph<? extends Vertex, ? extends Link<? extends Vertex>> copy = mGraph.getDeepCopy();

        HashMap<Integer, Integer> ans = input;
        boolean isWindy = false;
        if (mGraph.getClass() == WindyGraph.class)
            isWindy = true;

        Stack<Integer> toCheck = new Stack<Integer>();

        for (Vertex v : copy.getVertices())
            toCheck.push(v.getId());

        //find a vertex of degree 1
        TIntObjectHashMap<? extends Vertex> indexedVertices = copy.getInternalVertexMap();
        Map<? extends Vertex, ? extends List<? extends Link<? extends Vertex>>> tempNeighbors;
        Vertex v;
        int lid;
        while (!toCheck.isEmpty()) {
            v = indexedVertices.get(toCheck.pop());
            tempNeighbors = v.getNeighbors();
            if (tempNeighbors.size() != 1)
                continue;
            for (Vertex v2 : tempNeighbors.keySet()) {
                if (tempNeighbors.get(v2).size() != 1)
                    continue;

                lid = tempNeighbors.get(v2).get(0).getId();
                copy.removeEdge(lid);
                ans.put(lid, 2 * ans.get(lid));
                if (!toCheck.contains(v2.getId()))
                    toCheck.push(v2.getId());

            }
        }

        return ans;
    }
}
