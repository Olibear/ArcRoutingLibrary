
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
package oarlib.graph.util;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.link.impl.WindyEdge;
import oarlib.route.impl.Tour;
import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.List;

public class Utils {

    private static final Logger LOGGER = Logger.getLogger(Utils.class);

    /**
     * Takes a route over an augmentation of a graph g, and returns the route using ids from g.  Used primarily to allow
     * solvers to calculate thier routes however they wish (typically not by modifying the original graph), and then
     * get a solution that is in the original graph.
     *
     * @param origAns - the ans which will be converted
     * @param g       - the original graph; the returned route will contain link objects from this graph
     * @return - a route in g corresponding to the route in origAns
     */
    public static <V extends Vertex, E extends Link<V>, G extends Graph<V, E>> Route<V, E> reclaimTour(Route<? extends Vertex, ? extends Link<? extends Vertex>> origAns, G g) {

        Tour ans = new Tour();

        List<? extends Link> path = origAns.getPath();
        List<? extends Link<? extends Vertex>> candidates;
        int n = path.size();
        int firstId, secondId, traversalCost, secondCost;
        boolean foundIt;

        for (int i = 0; i < n; i++) {

            Link<? extends Vertex> l = path.get(i);
            firstId = l.getEndpoints().getFirst().getId();
            secondId = l.getEndpoints().getSecond().getId();
            traversalCost = l.getCost();
            foundIt = false;

            candidates = g.getVertex(firstId).getNeighbors().get(g.getVertex(secondId));

            if (firstId == secondId && firstId == g.getDepotId())
                continue;

            for (Link<? extends Vertex> l2 : candidates) {
                if (l.isRequired() && !l2.isRequired())
                    continue;
                if (l2.getEndpoints().getFirst().getId() == firstId && traversalCost == l2.getCost()) {
                    ans.appendEdge(l2, l.isRequired());
                    foundIt = true;
                    break;
                } else if (!l2.isDirected()) {
                    secondCost = l2.getCost();
                    if (l2.isWindy())
                        secondCost = ((WindyEdge) l2).getReverseCost();
                    if (l2.getEndpoints().getFirst().getId() == secondId && traversalCost == secondCost) {
                        ans.appendEdge(l2, l.isRequired());
                        foundIt = true;
                        break;
                    }
                }
            }
            if (!foundIt) {
                LOGGER.error("It seems as though this solution is invalid(?)");
                return null;
            }
        }

        return ans;

    }

    /**
     * Using the vertices getX and getY methods, it calculates the vertex closest to the center of the graph.
     *
     * @param g - the graph over which to perform the calculation
     * @return - the (internal) id of the vertex closest to the center
     */
    public static <V extends Vertex, E extends Link<V>> int findCenterVertex(Graph<V, E> g) {

        double meanX = 0;
        double meanY = 0;
        int n = g.getVertices().size();

        for (Vertex v : g.getVertices()) {
            meanX += v.getX();
            meanY += v.getY();
        }

        meanX = meanX / n;
        meanY = meanY / n;

        int bestId = -1;
        double bestDist = Double.MAX_VALUE;
        double candidateDist;

        for (Vertex v : g.getVertices()) {
            candidateDist = dist(v.getX(), v.getY(), meanX, meanY);
            if (candidateDist < bestDist) {
                bestDist = candidateDist;
                bestId = v.getId();
            }
        }

        return bestId;
    }

    /**
     * Returns the shortest path between two edges
     * @param e1 - the first edge (we go from this edge)
     * @param e2 - the second edge (we go to this edge)
     * @param dist - the allpairs shortest path matrix
     * @param constraint - an integer that constrains the search:
     * 0 - no constraints
     * 1 - force the path to begin from e1's second endpoint
     * 2 - force the path to end at e2's first endpoint
     * 3 - force the path to begin from e1's first endpoint
     * 4 - force the path to end at e2's second endpoint
     * @return - a pair, the first entry is the length of the path.  The second
     * entry is coded as follows:
     * 1 - path is from e1's 2nd endpoint to e2's first endpoint
     * 2 - path is from e1's 2nd endpoint to e2's second endpoint
     * 3 - path is from e1's 1st endpoint to e2's first endpoint
     * 4 - path is from e1's 1st endpoint to e2's second endpoint
     */
    public static Pair<Integer> shortestEdgeDistance(Link e1, Link e2, int[][] dist, int constraint) {

        if (constraint > 4 || constraint < 0) {
            constraint = 0;
            LOGGER.warn("The constraint argument has a value of: " + constraint + " which isn't a recognized value.  Assuming no constraints.");
        }

        int shortest = Integer.MAX_VALUE;
        int ret = -1;

        int e1First = e1.getFirstEndpointId();
        int e1Second = e1.getSecondEndpointId();
        int e2First = e2.getFirstEndpointId();
        int e2Second = e2.getSecondEndpointId();

        // 1-1
        if (constraint != 1 && constraint != 4) {
            if (dist[e1First][e2First] < shortest) {
                shortest = dist[e1First][e2First];
                ret = 3;
            }
        }
        // 1-2
        if (constraint != 1 && constraint != 2) {
            if (dist[e1First][e2Second] < shortest) {
                shortest = dist[e1First][e2Second];
                ret = 4;
            }
        }
        // 2-1
        if (constraint != 3 && constraint != 4) {
            if (dist[e1Second][e2First] < shortest) {
                shortest = dist[e1Second][e2First];
                ret = 1;
            }
        }
        // 2-2
        if (constraint != 3 && constraint != 2) {
            if (dist[e1Second][e2Second] < shortest) {
                shortest = dist[e1Second][e2Second];
                ret = 2;
            }
        }

        return new Pair<Integer>(shortest, ret);

    }

    /**
     * Returns the Euclidean distance between two points (possibly a dup)
     *
     * @param x1 - x coordinate of the first point
     * @param y1 - y coordinate of the first point
     * @param x2 - x coordinate of the second point
     * @param y2 - y coordinate of the second point
     * @return - the distance between the two points with coordinates (x1, y1) and (x2, y2) respectively.
     */
    public static double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    public static class DijkstrasComparator implements Comparator<Pair<Integer>> {
        @Override
        public int compare(Pair<Integer> arg0, Pair<Integer> arg1) {
            if (arg0.getSecond() > arg1.getSecond())
                return 1;
            else if (arg0.getSecond() < arg1.getSecond())
                return -1;
            return 0;
        }
    }

    public static class InverseDijkstrasComparator implements Comparator<Pair<Integer>> {
        @Override
        public int compare(Pair<Integer> arg0, Pair<Integer> arg1) {
            if (arg0.getSecond() < arg1.getSecond())
                return 1;
            else if (arg0.getSecond() > arg1.getSecond())
                return -1;
            return 0;
        }
    }

    public static class PFIHComparator implements Comparator<UnmatchedPair<Integer, Double>> {
        @Override
        public int compare(UnmatchedPair<Integer, Double> arg0, UnmatchedPair<Integer, Double> arg1) {
            if (arg0.getSecond() > arg1.getSecond())
                return 1;
            else if (arg0.getSecond() < arg1.getSecond())
                return -1;
            return 0;
        }
    }

    public static class DijkstrasComparatorForRecords implements Comparator<UnmatchedPair<Integer, IndexedRecord<Integer>>> {
        @Override
        public int compare(UnmatchedPair<Integer, IndexedRecord<Integer>> arg0, UnmatchedPair<Integer, IndexedRecord<Integer>> arg1) {
            return arg0.getSecond().getRecord().compareTo(arg1.getSecond().getRecord());
        }
    }

    public static class InverseDijkstrasComparatorForRecords implements Comparator<UnmatchedPair<Integer, IndexedRecord<Integer>>> {
        @Override
        public int compare(UnmatchedPair<Integer, IndexedRecord<Integer>> arg0, UnmatchedPair<Integer, IndexedRecord<Integer>> arg1) {
            return arg1.getSecond().getRecord().compareTo(arg0.getSecond().getRecord());
        }
    }


}
