package oarlib.graph.graphgen;

import oarlib.core.Edge;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.UndirectedVertex;

import java.util.HashMap;
import java.util.HashSet;

public class UndirectedGraphGenerator extends GraphGenerator {

    public UndirectedGraphGenerator() {
        super();
    }

    @Override
    public UndirectedGraph generateGraph(int n, int maxCost, boolean connected,
                                         double density, boolean positiveCosts) throws IllegalArgumentException {


        //edge cases
        if (n < 0)
            throw new IllegalArgumentException();
        if (n == 0)
            return new UndirectedGraph();

        try {
            //ans graph
            UndirectedGraph ans = new UndirectedGraph();

            //set up the vertices
            for (int i = 0; i < n; i++) {
                ans.addVertex(new UndirectedVertex("Original"));
            }

            if (n == 1)
                return ans;

            HashMap<Integer, UndirectedVertex> indexedVertices = ans.getInternalVertexMap();

            //figure out what is set
            maxCost = (maxCost < 0) ? Integer.MAX_VALUE : maxCost;
            density = (density > 0 && density < 1) ? density : Math.random();

            //randomly add edges
            for (int j = 2; j <= n; j++) {
                for (int k = 1; k < j; k++) {
                    //add the arc with probability density
                    if (Math.random() < density) {
                        if (positiveCosts)
                            ans.addEdge(new Edge("Original", new Pair<UndirectedVertex>(indexedVertices.get(k), indexedVertices.get(j)), 1 + (int) Math.round((maxCost - 1) * Math.random())));
                        else
                            ans.addEdge(new Edge("Original", new Pair<UndirectedVertex>(indexedVertices.get(k), indexedVertices.get(j)), (int) Math.round(maxCost * Math.random())));
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
                    UndirectedVertex lastCandidate = indexedVertices.get(1);
                    alreadyIntegrated.add(component[1]);
                    UndirectedVertex currCandidate;
                    for (int i = 2; i < component.length; i++) {
                        if (alreadyIntegrated.contains(component[i]))
                            continue;
                        alreadyIntegrated.add(component[i]);
                        currCandidate = indexedVertices.get(i);
                        ans.addEdge(new Edge("To ensure connectivity.", new Pair<UndirectedVertex>(lastCandidate, currCandidate), (int) Math.round(Math.random() * maxCost)));
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
    public UndirectedGraph generateEulerianGraph(int n, int maxCost,
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
                        g.addEdge(new Edge("to make Eulerian", new Pair<UndirectedVertex>(temp, v), (int) Math.round(maxCost * Math.random())));
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
