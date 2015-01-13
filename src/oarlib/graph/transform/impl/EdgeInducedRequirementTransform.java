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
package oarlib.graph.transform.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Factory;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.GraphTransformer;
import oarlib.link.impl.WindyEdge;

import java.util.HashSet;

/**
 * Created by oliverlum on 9/19/14.
 */
public class EdgeInducedRequirementTransform<S extends Graph<?, ?>> implements GraphTransformer<S, S> {

    S mGraph;
    HashSet<Integer> mEdges;
    Factory<S> mFactory;

    /**
     * Transformer, primarily for rural problems.  This takes a set of ids, and returns the same graph, but where the only required edges are the
     * ones specified.  This ensures maximum flexibility, and free connectivity
     */
    public EdgeInducedRequirementTransform(S graph, Factory<S> sFactory, HashSet<Integer> ids) {
        mGraph = graph;
        mEdges = ids;
        mFactory = sFactory;
    }

    @Override
    public void setGraph(S input) {
        mGraph = input;
    }

    @Override
    public S transformGraph() {

        try {
            S blankGraph = mFactory.instantiate();
            blankGraph.setDepotId(mGraph.getDepotId());
            int n = mGraph.getVertices().size();
            int m = mGraph.getEdges().size();
            TIntObjectHashMap<? extends Vertex> blankVertices = blankGraph.getInternalVertexMap();
            TIntObjectHashMap<? extends Vertex> mGraphVertices = mGraph.getInternalVertexMap();
            TIntObjectHashMap<? extends Link<? extends Vertex>> mGraphEdges = mGraph.getInternalEdgeMap();

            boolean isWindy = mGraph.getClass() == WindyGraph.class;

            for (int i = 1; i <= n; i++) {
                blankGraph.addVertex();
                blankVertices.get(i).setMatchId(i);
                blankVertices.get(i).setCoordinates(mGraphVertices.get(i).getX(), mGraphVertices.get(i).getY());
            }

            Link<? extends Vertex> l;
            for (int i = 1; i <= m; i++) {
                l = mGraphEdges.get(i);

                if (isWindy) {
                    if (mEdges.contains(l.getId())) {
                        ((WindyGraph) blankGraph).addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), l.getCost(), ((WindyEdge) l).getReverseCost(), l.isRequired());
                    } else {
                        ((WindyGraph) blankGraph).addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), l.getCost(), ((WindyEdge) l).getReverseCost(), false);
                    }
                } else {
                    if (mEdges.contains(l.getId())) {
                        blankGraph.addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), l.getCost(), l.isRequired());
                    } else {
                        blankGraph.addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), l.getCost(), false);
                    }
                }
            }

            //to make sure the depot gets included
            int depotId = blankGraph.getDepotId();
            blankGraph.addEdge(depotId, depotId, 2, true);

            return blankGraph;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
