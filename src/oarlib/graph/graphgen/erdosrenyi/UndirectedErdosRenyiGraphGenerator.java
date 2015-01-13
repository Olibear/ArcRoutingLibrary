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
package oarlib.graph.graphgen.erdosrenyi;

import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.Edge;
import oarlib.vertex.impl.UndirectedVertex;

import java.util.HashSet;

public class UndirectedErdosRenyiGraphGenerator extends ErdosRenyiGraphGenerator<UndirectedGraph> {

    public UndirectedErdosRenyiGraphGenerator() {
        super();
    }

    @Override
    public UndirectedGraph generate(int n, int maxCost, boolean connected,
                                    double density, double reqDensity, boolean positiveCosts) throws IllegalArgumentException {

        //edge cases
        if (n == 0)
            return new UndirectedGraph();

        try {
            //ans graph
            UndirectedGraph ans = new UndirectedGraph(n);

            if (n == 1)
                return ans;

            //figure out what is set
            maxCost = (maxCost < 0) ? Integer.MAX_VALUE : maxCost;
            density = (density > 0 && density < 1) ? density : Math.random();

            //randomly add edges
            boolean isReq;
            int coeff;
            for (int j = 2; j <= n; j++) {
                for (int k = 1; k < j; k++) {
                    //add the arc with probability density
                    if (Math.random() < density) {
                        if (Math.random() < reqDensity)
                            isReq = true;
                        else
                            isReq = false;

                        if (positiveCosts)
                            ans.addEdge(k, j, 1 + (int) Math.round((maxCost - 1) * Math.random()), isReq);
                        else {
                            if (Math.random() < .5)
                                coeff = 1;
                            else
                                coeff = -1;
                            ans.addEdge(k, j, (int) Math.round(maxCost * Math.random()) * coeff, isReq);
                        }
                    }
                }
            }

            //enforce connectedness
            if (connected) {
                //get the Strongly Connected Components of the graph, and add an arc for each direction between them.
                int[] component = new int[n + 1];
                int m = ans.getEdges().size();
                int[] nodei = new int[m + 1];
                int[] nodej = new int[m + 1];
                for (Edge e : ans.getEdges()) {
                    nodei[e.getId()] = e.getEndpoints().getFirst().getId();
                    nodej[e.getId()] = e.getEndpoints().getSecond().getId();
                }
                CommonAlgorithms.connectedComponents(n, m, nodei, nodej, component);
                //if we need to connect guys
                if (component[0] != 1) {
                    //keep track of who we've already connected up.  If we haven't connected vertex i yet, then add connections to/from lastcandidate
                    //(the last guy we connected) to currcandidate (whichever vertex belongs to a CC we haven't connected yet.
                    HashSet<Integer> alreadyIntegrated = new HashSet<Integer>();
                    alreadyIntegrated.add(component[1]);
                    UndirectedVertex currCandidate;
                    for (int i = 2; i < component.length; i++) {
                        if (alreadyIntegrated.contains(component[i]))
                            continue;
                        alreadyIntegrated.add(component[i]);
                        ans.addEdge(1, i, (int) Math.round(Math.random() * maxCost));
                    }
                }
            }

            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public UndirectedGraph generateEulerian(int n, int maxCost,
                                            boolean connected, double density) {
        try {
            UndirectedGraph g = this.generateGraph(n, maxCost, connected, density, false);
            //make Eulerian
            UndirectedVertex temp = null;
            boolean lookingForPartner = false;
            for (UndirectedVertex v : g.getVertices()) {
                //if odd degree
                if (v.getDegree() % 2 == 1) {
                    //either set temp, or connect it with temp
                    if (lookingForPartner) {
                        g.addEdge(temp.getId(), v.getId(), (int) Math.round(maxCost * Math.random()));
                        lookingForPartner = false;
                    } else {
                        temp = v;
                        lookingForPartner = true;
                    }
                }

            }
            return g;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
