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
package oarlib.route.util;

import gnu.trove.TIntArrayList;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.AsymmetricLink;
import oarlib.route.impl.Tour;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by oliverlum on 11/20/14.
 */
public class RouteExpander<G extends Graph> {

    private static final Logger LOGGER = Logger.getLogger(RouteExpander.class);

    G mGraph;
    int[][] dist;
    int[][] path;
    int[][] edgePath;

    public RouteExpander(G g) {

        mGraph = g;

        int n = g.getVertices().size();
        dist = new int[n + 1][n + 1];
        path = new int[n + 1][n + 1];
        edgePath = new int[n + 1][n + 1];

        //TODO: If the graph changes after this point, we're screwed.  Either detect changes, or create finalized state
        CommonAlgorithms.fwLeastCostPaths(g, dist, path, edgePath);

    }

    public Tour unflattenRoute(TIntArrayList flattenedRoute, ArrayList<Boolean> direction) {

        //arg checking
        if (!(flattenedRoute.size() == direction.size())) {
            LOGGER.error("The flattened route and direction arrays are of different size.");
            throw new IllegalArgumentException();
        }

        Tour ans = new Tour();

        int prev = mGraph.getDepotId();
        int to, nextPrev, curr, next, end;
        Link<? extends Vertex> temp;
        boolean cont;
        for (int i = 0; i < flattenedRoute.size(); i++) {
            temp = mGraph.getEdge(flattenedRoute.get(i));
            if (!temp.isRequired() && (temp.isWindy() && !((AsymmetricLink) temp).isReverseRequired()))
                continue;
            if (direction.get(i)) {
                to = temp.getEndpoints().getFirst().getId();
                nextPrev = temp.getEndpoints().getSecond().getId();
            } else {
                to = temp.getEndpoints().getSecond().getId();
                nextPrev = temp.getEndpoints().getFirst().getId();
            }

            //add the path from prev to to, and then the edge
            curr = prev;
            end = to;

            if (curr != end) {
                do {
                    next = path[curr][end];
                    ans.appendEdge(mGraph.getEdge(edgePath[curr][end]), false);
                } while ((curr = next) != end);
            }

            ans.appendEdge(temp, true);
            prev = nextPrev;

        }

        //add the path back to the depot
        curr = prev;
        end = mGraph.getDepotId();

        if (curr != end) {
            do {
                next = path[curr][end];
                ans.appendEdge(mGraph.getEdge(edgePath[curr][end]), false);
            } while ((curr = next) != end);
        }

        return ans;
    }

}
