package oarlib.graph.graphgen.erdosrenyi;

import gnu.trove.TIntObjectHashMap;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.link.impl.WindyEdge;
import oarlib.vertex.impl.WindyVertex;

import java.util.HashSet;

public class WindyErdosRenyiGraphGenerator extends ErdosRenyiGraphGenerator<WindyGraph> {

    public WindyErdosRenyiGraphGenerator() {
        super();
    }

    @Override
    public WindyGraph generate(int n, int maxCost, boolean connected,
                                    double density, double reqDensity, boolean positiveCosts) throws IllegalArgumentException {

        //edge cases
        if (n == 0)
            return new WindyGraph();

        try {
            //ans graph
            WindyGraph ans = new WindyGraph(n);

            if (n == 1)
                return ans;

            //figure out what is set
            maxCost = (maxCost < 0) ? Integer.MAX_VALUE : maxCost;
            density = (density > 0 && density < 1) ? density : Math.random();


            //randomly add edges
            boolean isReq;
            int cost, reverseCost;
            int coeff;
            for (int j = 2; j <= n; j++) {
                for (int k = 1; k < j; k++) {
                    //add the arc with probability density
                    if (Math.random() < density) {
                        if(Math.random() < reqDensity)
                            isReq = true;
                        else
                            isReq = false;

                        if (positiveCosts) {
                            cost = 1 + (int) Math.round((maxCost - 1) * Math.random());
                            reverseCost = 1 + (int) Math.round((maxCost - 1) * Math.random());
                        } else {
                            if(Math.random() < .5)
                                coeff = 1;
                            else
                                coeff = -1;

                            cost = (int) Math.round(maxCost * Math.random()) * coeff;
                            reverseCost = (int) Math.round(maxCost * Math.random()) * coeff;
                        }
                        ans.addEdge(k, j, cost, reverseCost, isReq);
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
                for (WindyEdge e : ans.getEdges()) {
                    nodei[e.getId()] = e.getEndpoints().getFirst().getId();
                    nodej[e.getId()] = e.getEndpoints().getSecond().getId();
                }
                CommonAlgorithms.connectedComponents(n, m, nodei, nodej, component);
                //if we need to connect guys
                if (component[0] != 1) {
                    //keep track of who we've already connected up.  If we haven't connected vertex i yet, then add connections to/from lastcandidate
                    //(the last guy we connected) to currcandidate (whichever vertex belongs to a CC we haven't connected yet.
                    HashSet<Integer> alreadyIntegrated = new HashSet<Integer>();
                    WindyVertex currCandidate;
                    for (int i = 1; i < component.length; i++) {
                        if (alreadyIntegrated.contains(component[i]))
                            continue;
                        alreadyIntegrated.add(component[i]);
                        cost = (int) Math.round(Math.random() * maxCost);
                        reverseCost = (int) Math.round(Math.random() * maxCost);
                        ans.addEdge(1, i, cost, reverseCost);
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
    public WindyGraph generateEulerian(int n, int maxCost,
                                            boolean connected, double density) {
        try {
            WindyGraph g = this.generateGraph(n, maxCost, connected, density, false);
            //make Eulerian
            WindyVertex temp = null;
            boolean lookingForPartner = false;
            int cost, reverseCost;
            for (WindyVertex v : g.getVertices()) {
                //if odd degree
                if (v.getDegree() % 2 == 1) {
                    //either set temp, or connect it with temp
                    if (lookingForPartner) {
                        cost = (int) Math.round(maxCost * Math.random());
                        reverseCost = (int) Math.round(maxCost * Math.random());
                        g.addEdge(temp.getId(), v.getId(), cost, reverseCost);
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
