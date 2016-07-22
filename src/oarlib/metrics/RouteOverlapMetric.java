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
package oarlib.metrics;

import gnu.trove.TIntHashSet;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Oliver on 12/26/2014.
 */
public class RouteOverlapMetric extends Metric {

    private Graph<? extends Vertex, ? extends Link<? extends Vertex>> mGraph;
    private int mN;

    public <V extends Vertex, E extends Link<V>> RouteOverlapMetric(Graph<V, E> g) {

        mGraph = g;

        TIntHashSet ids = new TIntHashSet();
        for (Link l : mGraph.getEdges()) {
            if (l.isRequired()) {
                ids.add(l.getFirstEndpointId());
                ids.add(l.getSecondEndpointId());
            }
        }
        mN = ids.size();
        mN = 0;
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<? extends Route> routes) {
        int no = calcNO(routes);
        int numRoutes = routes.size();

        return (no - mN);// / (Math.pow(Math.sqrt(numRoutes) + Math.sqrt(mN) - 1, 2) - mN);
    }

    @Override
    public Type getType() {
        return Type.ROI;
    }

    @Override
    public String toString() {
        return "Route Overlap Index (ROI)";
    }

    private <V extends Vertex, E extends Link<V>> int calcNO(Collection<? extends Route> sol) {

        int NO = 0;
        HashSet<Integer> traversedIds = new HashSet<Integer>();
        HashSet<Integer> totalIds = new HashSet<Integer>();

        for (Route<V, E> r : sol) {
            traversedIds.clear();
            List<E> temp = r.getPath();
            E tempLink;
            ArrayList<Boolean> tempService = r.getServicingList();
            for (int i = 0; i < temp.size(); i++) {
                //if (tempService.get(i)) {
                    tempLink = temp.get(i);
                    if (!traversedIds.contains(tempLink.getFirstEndpointId())) {
                        NO++;
                        traversedIds.add(tempLink.getFirstEndpointId());
                    }
                    if (!traversedIds.contains(tempLink.getSecondEndpointId())) {
                        NO++;
                        traversedIds.add(tempLink.getSecondEndpointId());
                    }
                //}
                totalIds.add(tempLink.getFirstEndpointId());
                totalIds.add(tempLink.getSecondEndpointId());
            }
        }

        return NO - totalIds.size();
    }
}
