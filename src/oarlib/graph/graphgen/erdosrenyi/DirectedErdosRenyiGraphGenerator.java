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

import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.Arc;
import oarlib.vertex.impl.DirectedVertex;

import java.util.ArrayList;
import java.util.HashSet;

public class DirectedErdosRenyiGraphGenerator extends ErdosRenyiGraphGenerator<DirectedGraph> {

    public DirectedErdosRenyiGraphGenerator() {
        super();
    }

    @Override
    public DirectedGraph generate(int n, int maxCost, boolean connected,
                                  double density, double reqDensity, boolean positiveCosts) {
        try {
            //edge cases
            if (n == 0)
                return new DirectedGraph();

            //ans graph
            DirectedGraph ans = new DirectedGraph(n);

            //add arcs randomly
            boolean req;
            int coeff;
            for (int j = 1; j <= n; j++) {
                for (int k = 1; k <= n; k++) {
                    if (j == k)
                        continue;
                    //add the arc with probability density
                    if (Math.random() < density) {
                        if (Math.random() <= reqDensity)
                            req = true;
                        else
                            req = false;

                        if (positiveCosts)
                            ans.addEdge(k, j, 1 + (int) Math.round((maxCost - 1) * Math.random()), req);
                        else {
                            if (Math.random() < .5)
                                coeff = 1;
                            else
                                coeff = -1;
                            ans.addEdge(k, j, (int) Math.round(maxCost * Math.random()) * coeff, req);
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
                for (Arc a : ans.getEdges()) {
                    nodei[a.getId()] = a.getEndpoints().getFirst().getId();
                    nodej[a.getId()] = a.getEndpoints().getSecond().getId();
                }
                CommonAlgorithms.stronglyConnectedComponents(n, m, nodei, nodej, component);
                //if we need to connect guys
                if (component[0] != 1) {
                    //keep track of who we've already connected up.  If we haven't connected vertex i yet, then add connections to/from lastcandidate
                    //(the last guy we connected) to currcandidate (whichever vertex belongs to a CC we haven't connected yet.
                    HashSet<Integer> alreadyIntegrated = new HashSet<Integer>();
                    for (int i = 1; i < component.length; i++) {
                        if (alreadyIntegrated.contains(component[i]))
                            continue;
                        alreadyIntegrated.add(component[i]);
                        ans.addEdge(1, i, (int) Math.round(Math.random() * maxCost));
                        ans.addEdge(i, 1, (int) Math.round(Math.random() * maxCost));
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
    public DirectedGraph generateEulerian(int n, int maxCost,
                                          boolean connected, double density) {
        try {
            DirectedGraph g = this.generateGraph(n, maxCost, connected, density, false);

            //make Eulerian
            ArrayList<DirectedVertex> Dplus = new ArrayList<DirectedVertex>();
            ArrayList<DirectedVertex> Dminus = new ArrayList<DirectedVertex>();
            for (DirectedVertex v : g.getVertices()) {
                if (v.getDelta() > 0) {
                    Dplus.add(v);
                } else if (v.getDelta() < 0) {
                    Dminus.add(v);
                }
            }
            int iplus = 0;
            int iminus = 0;
            int j, k;
            DirectedVertex vplus;
            DirectedVertex vminus;
            while (iplus < Dplus.size() && iminus < Dminus.size()) {
                vplus = Dplus.get(iplus);
                vminus = Dminus.get(iminus);
                if (-vminus.getDelta() > vplus.getDelta()) {
                    //add enough arcs to zero vplus
                    k = vplus.getDelta();
                    for (j = 0; j < k; j++) {
                        g.addEdge(iplus, iminus, (int) Math.round(maxCost * Math.random()));
                    }
                    //increment the vplus counter
                    iplus++;
                } else {
                    //add enough arcs to zero vminus
                    k = -vminus.getDelta();
                    for (j = 0; j < k; j++) {
                        g.addEdge(iplus, iminus, (int) Math.round(maxCost * Math.random()));
                    }
                    //increment the vminus counter
                    iminus++;
                    //if they were equal in delta, we need to update this too
                    if (vplus.getDelta() == 0)
                        iplus++;
                }

            }

            return g;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
