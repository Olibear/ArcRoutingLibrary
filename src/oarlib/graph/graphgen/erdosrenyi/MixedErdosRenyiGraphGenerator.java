package oarlib.graph.graphgen.erdosrenyi;

import gnu.trove.TIntObjectHashMap;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.link.impl.MixedEdge;
import oarlib.vertex.impl.MixedVertex;

import java.util.ArrayList;
import java.util.HashSet;

public class MixedErdosRenyiGraphGenerator extends ErdosRenyiGraphGenerator<MixedGraph> {

    public MixedErdosRenyiGraphGenerator() {
        super();
    }

    @Override
    public MixedGraph generate(int n, int maxCost, boolean connected,
                                    double density, double reqDensity, boolean positiveCosts) {

        //edge cases
        if (n == 0)
            return new MixedGraph();

        try {
            //ans graph
            MixedGraph ans = new MixedGraph(n);

            if (n == 1)
                return ans;

            //figure out what is set
            maxCost = (maxCost < 0) ? Integer.MAX_VALUE : maxCost;
            density = (density > 0 && density < 1) ? density : Math.random();

            double rand;
            boolean isDirected;
            boolean isReq;
            int coeff;
            int m = 0; // since we set up the directed representation of this to determine connectedness, then m will not just be the number of edges
            //randomly add edges
            for (int j = 2; j <= n; j++) {
                for (int k = 1; k < j; k++) {
                    rand = Math.random();
                    isDirected = (rand <= .5);
                    m = isDirected ? m + 1 : m + 2;
                    //add the arc with probability density
                    if (rand < density) {
                        if(Math.random() < reqDensity)
                            isReq = true;
                        else
                            isReq = false;

                        if (positiveCosts)
                            ans.addEdge(k,j, 1 + (int) Math.round((maxCost - 1) * Math.random()), isReq);
                        else {
                            if(Math.random() < .5)
                                coeff = 1;
                            else
                                coeff = -1;
                            ans.addEdge(k, j, (int) Math.round(maxCost * rand) * coeff, isDirected, isReq);
                        }
                    }
                }
            }

            //enforce connectedness
            if (connected) {
                //get the Strongly Connected Components of the graph, and add an arc for each direction between them.
                int[] component = new int[n + 1];

                int[] nodei = new int[m + 1];
                int[] nodej = new int[m + 1];
                int counter = 1;
                for (MixedEdge e : ans.getEdges()) {
                    nodei[counter] = e.getEndpoints().getFirst().getId();
                    nodej[counter++] = e.getEndpoints().getSecond().getId();
                    //have to be a little careful with mixed; add another arc in the opposite dir for testing connected
                    if (!e.isDirected()) {
                        nodei[counter] = e.getEndpoints().getSecond().getId();
                        nodej[counter] = e.getEndpoints().getFirst().getId();
                    }
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
    public MixedGraph generateEulerian(int n, int maxCost,
                                            boolean connected, double density) {
        try {
            MixedGraph g = this.generateGraph(n, maxCost, connected, density, false);

            //make balance (indegree = outdegree
            ArrayList<MixedVertex> Dplus = new ArrayList<MixedVertex>();
            ArrayList<MixedVertex> Dminus = new ArrayList<MixedVertex>();
            for (MixedVertex v : g.getVertices()) {
                if (v.getDelta() > 0) {
                    Dplus.add(v);
                } else if (v.getDelta() < 0) {
                    Dminus.add(v);
                }
            }
            int iplus = 0;
            int iminus = 0;
            int j, k;
            MixedVertex vplus;
            MixedVertex vminus;
            while (iplus < Dplus.size() && iminus < Dminus.size()) {
                vplus = Dplus.get(iplus);
                vminus = Dminus.get(iminus);
                if (-vminus.getDelta() > vplus.getDelta()) {
                    //add enough arcs to zero vplus
                    k = vplus.getDelta();
                    for (j = 0; j < k; j++) {
                        g.addEdge(iplus, iminus, (int) Math.round(maxCost * Math.random()), true);
                    }
                    //increment the vplus counter
                    iplus++;
                } else {
                    //add enough arcs to zero vminus
                    k = -vminus.getDelta();
                    for (j = 0; j < k; j++) {
                        g.addEdge(iplus, iminus, (int) Math.round(maxCost * Math.random()), true);
                    }
                    //increment the vminus counter
                    iminus++;
                    //if they were equal in delta, we need to update this too
                    if (vplus.getDelta() == 0)
                        iplus++;
                }

                //evendegree (make eulerian in the undirected sense)
                MixedVertex temp = null;
                boolean lookingForPartner = false;
                for (MixedVertex v : g.getVertices()) {
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

            }
            return g;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
