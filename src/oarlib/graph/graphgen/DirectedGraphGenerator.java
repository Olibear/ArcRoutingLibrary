package oarlib.graph.graphgen;

import oarlib.core.Arc;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class DirectedGraphGenerator extends GraphGenerator {

    public DirectedGraphGenerator() {
        super();
    }

    @Override
    public DirectedGraph generateGraph(int n, int maxCost, boolean connected,
                                       double density, boolean positiveCosts) {
        try {

            //ans graph
            DirectedGraph ans = new DirectedGraph();

            //set up the vertices
            for (int i = 0; i < n; i++) {
                ans.addVertex(new DirectedVertex("Original"));
            }
            HashMap<Integer, DirectedVertex> indexedVertices = ans.getInternalVertexMap();

            //figure out what is set
            maxCost = (maxCost < 0) ? Integer.MAX_VALUE : maxCost;
            density = (density > 0 && density < 1) ? density : Math.random();

            //add arcs randomly
            for (int j = 1; j <= n; j++) {
                for (int k = 1; k <= n; k++) {
                    if (j == k)
                        continue;
                    //add the arc with probability density
                    if (Math.random() < density) {
                        if (positiveCosts)
                            ans.addEdge(new Arc("Original", new Pair<DirectedVertex>(indexedVertices.get(k), indexedVertices.get(j)), 1 + (int) Math.round((maxCost - 1) * Math.random())));
                        else
                            ans.addEdge(new Arc("Original", new Pair<DirectedVertex>(indexedVertices.get(k), indexedVertices.get(j)), (int) Math.round(maxCost * Math.random())));
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
                    DirectedVertex lastCandidate = indexedVertices.get(1);
                    DirectedVertex currCandidate;
                    for (int i = 1; i < component.length; i++) {
                        if (alreadyIntegrated.contains(component[i]))
                            continue;
                        alreadyIntegrated.add(component[i]);
                        currCandidate = indexedVertices.get(i);
                        ans.addEdge(new Arc("To ensure connectivity.", new Pair<DirectedVertex>(lastCandidate, currCandidate), (int) Math.round(Math.random() * maxCost)));
                        ans.addEdge(new Arc("To ensure connectivity.", new Pair<DirectedVertex>(currCandidate, lastCandidate), (int) Math.round(Math.random() * maxCost)));
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
    public DirectedGraph generateEulerianGraph(int n, int maxCost,
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
                        g.addEdge(new Arc("to make Eulerian", new Pair<DirectedVertex>(vplus, vminus), (int) Math.round(maxCost * Math.random())));
                    }
                    //increment the vplus counter
                    iplus++;
                } else {
                    //add enough arcs to zero vminus
                    k = -vminus.getDelta();
                    for (j = 0; j < k; j++) {
                        g.addEdge(new Arc("to make Eulerian", new Pair<DirectedVertex>(vplus, vminus), (int) Math.round(maxCost * Math.random())));
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
