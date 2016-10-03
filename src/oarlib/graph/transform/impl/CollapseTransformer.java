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
package oarlib.graph.transform.impl;

import oarlib.core.Factory;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.GraphTransformer;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.AsymmetricLink;

import java.util.HashSet;

/**
 * Takes set of edge ids and collapses them
 * Created by oliverlum on 3/17/16.
 */
public class CollapseTransformer<S extends Graph<?, ?>> implements GraphTransformer<S, S> {

    S mGraph;
    Factory<S> mFactory;
    HashSet<Integer> toCollapse;

    /**
     * Constructor
     *
     * @param graph    - input graph
     * @param sFactory - to create the output graph
     * @param collapse - set of sets; each set should contain a list of internal edge ids that
     *                 will be combined into a single
     */
    public CollapseTransformer(S graph, Factory<S> sFactory, HashSet<Integer> collapse) {
        mGraph = graph;
        mFactory = sFactory;
        toCollapse = collapse;
    }

    @Override
    public void setGraph(S input) {
        mGraph = input;
    }

    @Override
    public S transformGraph() {

        //init
        S blankGraph = mFactory.instantiate();
        int n = mGraph.getVertices().size();
        int m = toCollapse.size();


        //figure out which vertices will be combined into one
        try {

            int[] nodei = new int[m + 1];
            int[] nodej = new int[m + 1];
            int[] component = new int[m + 1];
            int index = 1;
            for (Integer i : toCollapse) {
                Link temp = mGraph.getEdge(i);
                nodei[index] = temp.getFirstEndpointId();
                nodej[index] = temp.getSecondEndpointId();
                index++;
            }

            CommonAlgorithms.connectedComponents(n, m, nodei, nodej, component);

            for (int i = 0; i < component[0]; i++) {
                blankGraph.addVertex();
            }

            //now add the rest of the edges to the ans
            for (Link l : mGraph.getEdges()) {
                if (toCollapse.contains(l.getId()))
                    continue;
                if (component[l.getFirstEndpointId()] == component[l.getSecondEndpointId()])
                    continue;

                //may still need to check if we have an existing edge
                if (l.isWindy())
                    ((WindyGraph) blankGraph).addEdge(component[l.getFirstEndpointId()], component[l.getSecondEndpointId()], l.getCost(), ((AsymmetricLink) l).getReverseCost());
                else
                    blankGraph.addEdge(component[l.getFirstEndpointId()], component[l.getSecondEndpointId()], l.getCost());
            }

            //assign vertex costs
            for (Integer i : toCollapse) {
                Link l = mGraph.getEdge(i);
                //figure out where it went
                int vId = component[l.getFirstEndpointId()];
                if (l.isWindy()) {
                    int oldCost = blankGraph.getVertex(vId).getCost();
                    blankGraph.getVertex(vId).setCost((int) (oldCost + (l.getCost() + ((AsymmetricLink) l).getReverseCost()) * .5));
                } else {
                    blankGraph.getVertex(vId).setCost(blankGraph.getVertex(vId).getCost() + l.getCost());
                }
            }

            return blankGraph;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
