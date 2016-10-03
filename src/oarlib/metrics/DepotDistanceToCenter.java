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
package oarlib.metrics;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.util.Utils;

import java.util.Collection;

/**
 * Created by oliverlum on 1/18/15.
 */
public class DepotDistanceToCenter extends Metric {

    private Graph<? extends Vertex, ?> mGraph;

    public <V extends Vertex, E extends Link<V>> DepotDistanceToCenter(Graph<V, E> g) {
        mGraph = g;
    }

    @Override
    public <V extends Vertex, E extends Link<V>> double evaluate(Collection<? extends Route> routes) {

        double meanX = 0;
        double meanY = 0;
        int n = mGraph.getVertices().size();

        for (Vertex v : mGraph.getVertices()) {
            meanX += v.getX();
            meanY += v.getY();
        }

        meanX = meanX / n;
        meanY = meanY / n;

        Vertex depot = mGraph.getVertex(mGraph.getDepotId());
        double depotDist = Utils.dist(depot.getX(), depot.getY(), meanX, meanY);

        int bestId = -1;
        double largestDist = Double.MIN_VALUE;
        double candidateDist;

        for (Vertex v : mGraph.getVertices()) {
            candidateDist = Utils.dist(v.getX(), v.getY(), meanX, meanY);
            if (candidateDist > largestDist) {
                largestDist = candidateDist;
                bestId = v.getId();
            }
        }

        return depotDist / largestDist;
    }

    @Override
    public Type getType() {
        return Type.DEPDIST;
    }

    @Override
    public String toString() {
        return "Normalized Depot Distance from Center";
    }
}
