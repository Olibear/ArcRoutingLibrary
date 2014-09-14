package oarlib.graph.transform.impl;

import oarlib.core.*;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.GraphTransformer;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Transform to fetch the subgraph induced by the edges with the ids specified.
 * Created by Oliver Lum on 7/26/2014.
 */
public class EdgeInducedSubgraphTransform<S extends Graph<?, ?>> implements GraphTransformer<S, S> {

    S mGraph;
    Factory<S> graphGen;
    HashSet<Integer> mEdges;
    boolean inclDepot;

    /**
     * This transformer takes the input graph and the specified edge ids, and returns the subgraph induced by said edges.
     *
     * @param input        - the whole graph from which the subgraph is generated
     * @param sFactory     - a factory that constructs an empty graph of type S to put our ans in
     * @param edges        - the edge ids which induce the desired subgraph
     * @param includeDepot - true if you'd like to include and connect the depot of the graph to this partition (using
     *                     shortest paths), false oth.
     */
    public EdgeInducedSubgraphTransform(S input, Factory<S> sFactory, HashSet<Integer> edges, boolean includeDepot) {
        setGraph(input);
        setEdges(edges);
        graphGen = sFactory;
        inclDepot = includeDepot;
    }

    /**
     * This transformer takes the input graph and the specified edge ids, and returns the subgraph induced by said edges.
     *
     * @param input    - the whole graph from which the subgraph is generated
     * @param sFactory - a factory that constructs an empty graph of type S to put our ans in
     * @param edges    - the edge ids which induce the desired subgraph
     */
    public EdgeInducedSubgraphTransform(S input, Factory<S> sFactory, HashSet<Integer> edges) {
        setGraph(input);
        setEdges(edges);
        graphGen = sFactory;
        inclDepot = false;
    }

    @Override
    public void setGraph(S input) {
        mGraph = input;
    }

    @Override
    public S transformGraph() {
        try {

            /*
             * Takes the graph and attempts to construct the subgraph induced by the edges with the ids specified.
             *
             * blankGraph - the template from which we build the subgraph, (it's what gets returned).
             *
             * indexedEdges - the edge map for the original graph
             * indexedVertices - the vertex map for the original graph
             *
             * blankVertices - the vertex map for the answer graph
             *
             * addedVertices - the ids of the vertices that have thus far in the construction been added
             *
             * firstId, secondId - as we iterate through the subgraph-inducing edge set, these contain the ids of the endpoints
             * in the original graph
             */

            S blankGraph = graphGen.instantiate();

            //go through the edges, and create the induced graph
            HashMap<Integer, ? extends Link<? extends Vertex>> indexedEdges = mGraph.getInternalEdgeMap();
            HashMap<Integer, ? extends Vertex> indexedVertices = mGraph.getInternalVertexMap();
            HashMap<Integer, ? extends Vertex> blankVertices = blankGraph.getInternalVertexMap();
            HashSet<Integer> addedVertices = new HashSet<Integer>();
            int firstId, secondId;
            int newVertexCounter = 1;
            int depotId = mGraph.getDepotId();
            Vertex first, second;
            Link<? extends Vertex> temp;

            //windy case vars
            boolean isWindy = blankGraph.getClass() == WindyGraph.class;

            for (Integer i : mEdges) {

                temp = indexedEdges.get(i);

                //start with only the required guys
                if(!temp.isRequired())
                    continue;
                first = temp.getEndpoints().getFirst();
                firstId = first.getId();

                if (!addedVertices.contains(firstId)) {
                    addedVertices.add(firstId);
                    blankGraph.addVertex();

                    //set the match id for later
                    blankVertices.get(newVertexCounter).setMatchId(firstId);
                    if (firstId == depotId) {
                        blankGraph.setDepotId(newVertexCounter);
                    }
                    first.setMatchId(newVertexCounter++);

                }

                second = temp.getEndpoints().getSecond();
                secondId = second.getId();
                if (!addedVertices.contains(secondId)) {

                    addedVertices.add(secondId);
                    blankGraph.addVertex();

                    //set the match id for later
                    blankVertices.get(newVertexCounter).setMatchId(secondId);
                    if (secondId == depotId) {
                        blankGraph.setDepotId(newVertexCounter);
                    }
                    second.setMatchId(newVertexCounter++);

                }

                //now add the edges in there

                //fix for windy case
                if(isWindy)
                    ((WindyGraph) blankGraph).addEdge(first.getMatchId(), second.getMatchId(), temp.getCost(), ((WindyEdge)temp).getReverseCost(), temp.isRequired());
                else
                    blankGraph.addEdge(first.getMatchId(), second.getMatchId(), temp.getCost(), temp.isRequired());
            }

            //if the depot isn't in the list, then add it, and connect up with the shortest path costs
            if (inclDepot && !addedVertices.contains(depotId)) {
                //add it
                blankGraph.addVertex();
                Vertex depot = indexedVertices.get(depotId);
                blankVertices.get(newVertexCounter).setMatchId(depotId);
                depot.setMatchId(newVertexCounter);
                blankGraph.setDepotId(newVertexCounter++);
                addedVertices.add(depotId);

                //connect depot to partition
                int n = mGraph.getVertices().size();
                int[] dijkstraDist = new int[n + 1];
                int[] dijkstraPath = new int[n + 1];
                int[] dijkstraEdges = new int[n + 1];
                CommonAlgorithms.dijkstrasAlgorithm(mGraph, depotId, dijkstraDist, dijkstraPath, dijkstraEdges);

                int bestConnectId = -1; //the id of the vertex with the shortest distance to the depot node
                boolean minSet = false;
                double bestCost = 0;
                for (Integer i : addedVertices) {
                    if (bestCost < dijkstraDist[i] || !minSet) {
                        bestCost = dijkstraDist[i];
                        bestConnectId = i;
                        minSet = true;
                    }
                }
                int bestConnectMatchId = indexedVertices.get(bestConnectId).getMatchId();

                int start = depotId;
                int end = bestConnectId;
                int next;
                Link<? extends Vertex> l;
                do {
                    next = dijkstraPath[end];
                    //make sure that we've added the proper vertices
                    l = indexedEdges.get(dijkstraEdges[end]);

                    first = l.getEndpoints().getFirst();
                    firstId = first.getId();

                    if (!addedVertices.contains(firstId)) {
                        addedVertices.add(firstId);
                        blankGraph.addVertex();

                        //set the match id for later
                        blankVertices.get(newVertexCounter).setMatchId(firstId);
                        if (firstId == depotId) {
                            blankGraph.setDepotId(newVertexCounter);
                        }
                        first.setMatchId(newVertexCounter++);

                    }

                    second = l.getEndpoints().getSecond();
                    secondId = second.getId();
                    if (!addedVertices.contains(secondId)) {

                        addedVertices.add(secondId);
                        blankGraph.addVertex();

                        //set the match id for later
                        blankVertices.get(newVertexCounter).setMatchId(secondId);
                        if (secondId == depotId) {
                            blankGraph.setDepotId(newVertexCounter);
                        }
                        second.setMatchId(newVertexCounter++);

                    }

                    //fix windy

                    //fix for windy case
                    if(isWindy) {
                        ((WindyGraph) blankGraph).addEdge(first.getMatchId(), second.getMatchId(), l.getCost(), ((WindyEdge)l).getReverseCost(), true);
                    }
                    else
                        blankGraph.addEdge(first.getMatchId(), second.getMatchId(), l.getCost(), true);

                } while((end = next) != start);

                //connect partition to depot
                CommonAlgorithms.dijkstrasAlgorithm(mGraph, bestConnectId, dijkstraDist, dijkstraPath, dijkstraEdges);

                start = bestConnectId;
                end = depotId;
                do {
                    next = dijkstraPath[end];
                    //make sure that we've added the proper vertices
                    l = indexedEdges.get(dijkstraEdges[end]);

                    first = l.getEndpoints().getFirst();
                    firstId = first.getId();

                    if (!addedVertices.contains(firstId)) {
                        addedVertices.add(firstId);
                        blankGraph.addVertex();

                        //set the match id for later
                        blankVertices.get(newVertexCounter).setMatchId(firstId);
                        if (firstId == depotId) {
                            blankGraph.setDepotId(newVertexCounter);
                        }
                        first.setMatchId(newVertexCounter++);

                    }

                    second = l.getEndpoints().getSecond();
                    secondId = second.getId();
                    if (!addedVertices.contains(secondId)) {

                        addedVertices.add(secondId);
                        blankGraph.addVertex();

                        //set the match id for later
                        blankVertices.get(newVertexCounter).setMatchId(secondId);
                        if (secondId == depotId) {
                            blankGraph.setDepotId(newVertexCounter);
                        }
                        second.setMatchId(newVertexCounter++);

                    }

                    //fix for windy case
                    if(isWindy) {
                        ((WindyGraph) blankGraph).addEdge(first.getMatchId(), second.getMatchId(), l.getCost(), ((WindyEdge)l).getReverseCost(), true);
                    }
                    else
                        blankGraph.addEdge(first.getMatchId(), second.getMatchId(), l.getCost(), true);

                } while((end = next) != start);
            }

            //now add back the non-required guys
            for(Integer i : mEdges)
            {
                temp = indexedEdges.get(i);

                //start with only the required guys
                if(temp.isRequired())
                    continue;
                first = temp.getEndpoints().getFirst();
                firstId = first.getId();

                if (!addedVertices.contains(firstId)) {
                    addedVertices.add(firstId);
                    blankGraph.addVertex();

                    //set the match id for later
                    blankVertices.get(newVertexCounter).setMatchId(firstId);
                    first.setMatchId(newVertexCounter++);

                }

                second = temp.getEndpoints().getSecond();
                secondId = second.getId();
                if (!addedVertices.contains(secondId)) {

                    addedVertices.add(secondId);
                    blankGraph.addVertex();

                    //set the match id for later
                    blankVertices.get(newVertexCounter).setMatchId(secondId);
                    second.setMatchId(newVertexCounter++);

                }

                //now add the edges in there

                //fix for windy case
                if(isWindy)
                    ((WindyGraph) blankGraph).addEdge(first.getMatchId(), second.getMatchId(), temp.getCost(), ((WindyEdge)temp).getReverseCost(), temp.isRequired());
                else
                    blankGraph.addEdge(first.getMatchId(), second.getMatchId(), temp.getCost(), temp.isRequired());
            }

            repairConnectivity(blankGraph);

            return blankGraph;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public HashSet<Integer> getEdges() {
        return mEdges;
    }

    public void setEdges(HashSet<Integer> newEdges) {
        mEdges = newEdges;
    }

    /**
     * Function to repair the subgraph if it's no longer connected after
     * the partition.  It repairs the graph in the following way:
     * <p/>
     * The strongly connected components of the graph are computed, and then connected
     * with the shortest link to and from each of them to another component
     *
     * @param subgraph
     */
    private void repairConnectivity(S subgraph) {
        /**
         * Here we exploit the fact that the graph must be completely connected,
         * but not necessarily strongly connected.  Thus, any two SCC's only require
         * unidirectional repair.
         */
        try {

            int sccN = subgraph.getVertices().size();
            int mainN = mGraph.getVertices().size();
            int totalCost = 0;

            DirectedGraph sccGraph = new DirectedGraph(subgraph.getVertices().size());
            for (Link<? extends Vertex> l : subgraph.getEdges()) {
                if (l.isDirected())
                    sccGraph.addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), 1);
                else {
                    sccGraph.addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), 1);
                    sccGraph.addEdge(l.getEndpoints().getSecond().getId(), l.getEndpoints().getFirst().getId(), 1);
                }
                totalCost += l.getCost();
            }

            //compute sccs
            int[] ans = CommonAlgorithms.stronglyConnectedComponents(sccGraph);
            int nScc = ans[0];

            if (nScc == 1)
                return; //no repair necessary

            //compute the shortest paths
            int[][] sccDist = new int[sccN + 1][sccN + 1];
            int[][] sccPath = new int[sccN + 1][sccN + 1];
            int[][] sccEdgePath = new int[sccN + 1][sccN + 1];
            CommonAlgorithms.fwLeastCostPaths(sccGraph, sccDist, sccPath, sccEdgePath);

            int[][] mainDist = new int[mainN + 1][mainN + 1];
            int[][] mainPath = new int[mainN + 1][mainN + 1];
            int[][] mainEdgePath = new int[mainN + 1][mainN + 1];
            CommonAlgorithms.fwLeastCostPaths(mGraph, mainDist, mainPath, mainEdgePath);

            //connect the sccs of the partition

            //first figure out which connections we could add
            int connCost;
            Pair<Integer> candidateKey;
            HashMap<Integer, ? extends Vertex> subgraphVertices = subgraph.getInternalVertexMap();
            HashMap<Pair<Integer>, Pair<Integer>> idConn = new HashMap<Pair<Integer>, Pair<Integer>>();
            HashMap<Pair<Integer>, Integer> costMap = new HashMap<Pair<Integer>, Integer>();
            HashMap<Pair<Integer>, Integer> edgeMap = new HashMap<Pair<Integer>, Integer>();

            DirectedGraph completeSccGraph = new DirectedGraph(nScc);
            HashMap<Integer, DirectedVertex> completeVertices = completeSccGraph.getInternalVertexMap();
            ArrayList<Boolean> realEdge = new ArrayList<Boolean>();
            //entry 0 useless
            realEdge.add(true);
            int tempI, tempJ;
            DirectedVertex vi, vj;
            HashSet<Pair<Integer>> alreadyAdded = new HashSet<Pair<Integer>>();
            Pair<Integer> tempKey;

            for (int i = 1; i <= sccN; i++) {
                tempI = ans[i];
                for (int j = 1; j <= sccN; j++) {
                    tempJ = ans[j];
                    //don't worry about internal paths
                    if (tempI == tempJ)
                        continue;

                    //if the dist is inf. then it's not actually connected
                    if (sccDist[i][j] < totalCost) {
                        completeSccGraph.addEdge(tempI, tempJ, mainDist[i][j], true);
                        realEdge.add(true);
                        tempKey = new Pair<Integer>(tempI, tempJ);

                        if (!alreadyAdded.contains(tempKey)) {
                            vi = completeVertices.get(tempI);
                            vj = completeVertices.get(tempJ);
                            if (vi.isDemandSet())
                                vi.setDemand(vi.getDemand() - 1);
                            else
                                vi.setDemand(-1);
                            if (vj.isDemandSet())
                                vj.setDemand(vj.getDemand() + 1);
                            else
                                vj.setDemand(1);

                            System.out.println("Originally, component " + ans[i] + " was connected to component " + ans[j]);
                            alreadyAdded.add(tempKey);
                        }
                        continue;
                    }

                    connCost = mainDist[subgraphVertices.get(i).getMatchId()][subgraphVertices.get(j).getMatchId()];

                    candidateKey = new Pair<Integer>(ans[i], ans[j]);
                    if (!idConn.containsKey(candidateKey)) {
                        idConn.put(candidateKey, new Pair<Integer>(i, j));
                        costMap.put(candidateKey, connCost);
                        edgeMap.put(candidateKey, mainEdgePath[subgraphVertices.get(i).getMatchId()][subgraphVertices.get(j).getMatchId()]);
                        completeSccGraph.addEdge(ans[i], ans[j], connCost, false);
                        realEdge.add(false);
                    } else if (connCost < costMap.get(candidateKey)) {
                        idConn.put(candidateKey, new Pair<Integer>(i, j));
                        costMap.put(candidateKey, connCost);
                        edgeMap.put(candidateKey, mainEdgePath[subgraphVertices.get(i).getMatchId()][subgraphVertices.get(j).getMatchId()]);
                        completeSccGraph.addEdge(ans[i], ans[j], connCost, false);
                        realEdge.add(false);
                    }
                }
            }

            //solve the min cost flow
            int[] flowanswer = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(completeSccGraph);
            HashMap<Integer, Arc> completeArcs = completeSccGraph.getInternalEdgeMap();
            int numEdges = completeSccGraph.getEdges().size();

            //to handle the asymmetric case
            boolean isWindy = false;
            WindyEdge dup;
            HashMap<Integer, WindyEdge> windyEdgeMap = null;
            if(subgraph.getClass() == WindyGraph.class) {
                isWindy = true;
                windyEdgeMap = ((WindyGraph)subgraph).getInternalEdgeMap();
            }



            for (int i = 1; i <= numEdges; i++) {
                if (!realEdge.get(i) && flowanswer[i] > 0) {
                    tempKey = new Pair<Integer>(completeArcs.get(i).getTail().getId(), completeArcs.get(i).getHead().getId());
                    //fix for windy case
                    if(isWindy) {
                        dup = windyEdgeMap.get(edgeMap.get(tempKey));
                        ((WindyGraph) subgraph).addEdge(idConn.get(tempKey).getFirst(), idConn.get(tempKey).getSecond(), dup.getCost(), dup.getReverseCost(), false);
                    }
                    else
                        subgraph.addEdge(idConn.get(tempKey).getFirst(), idConn.get(tempKey).getSecond(), costMap.get(tempKey), false);
                    System.out.println("We're connecting component " + tempKey.getFirst() + " was connected to component " + tempKey.getSecond());
                }
            }

            /*
             * PHASE TWO
             * if we're still not done, then recollapse and perform an mst
             */

            DirectedGraph sccGraph2 = new DirectedGraph(subgraph.getVertices().size());
            for (Link<? extends Vertex> l : subgraph.getEdges()) {
                if (l.isDirected())
                    sccGraph2.addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), 1);
                else {
                    sccGraph2.addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), 1);
                    sccGraph2.addEdge(l.getEndpoints().getSecond().getId(), l.getEndpoints().getFirst().getId(), 1);
                }
                totalCost += l.getCost();
            }

            //compute sccs
            int[] ans2 = CommonAlgorithms.stronglyConnectedComponents(sccGraph2);
            int nScc2 = ans2[0];
            if(nScc2 > 1)
            {
                //compute the shortest paths
                sccDist = new int[sccN + 1][sccN + 1];
                sccPath = new int[sccN + 1][sccN + 1];
                sccEdgePath = new int[sccN + 1][sccN + 1];
                CommonAlgorithms.fwLeastCostPaths(sccGraph, sccDist, sccPath, sccEdgePath);

                mainDist = new int[mainN + 1][mainN + 1];
                mainPath = new int[mainN + 1][mainN + 1];
                mainEdgePath = new int[mainN + 1][mainN + 1];
                CommonAlgorithms.fwLeastCostPaths(mGraph, mainDist, mainPath, mainEdgePath);

                //connect the sccs of the partition

                //first figure out which connections we could add
                idConn = new HashMap<Pair<Integer>, Pair<Integer>>();
                costMap = new HashMap<Pair<Integer>, Integer>();
                edgeMap = new HashMap<Pair<Integer>, Integer>();

                UndirectedGraph completeCcGraph = new UndirectedGraph(nScc);

                for (int i = 1; i <= sccN; i++) {
                    tempI = ans[i];
                    for (int j = 1; j <= sccN; j++) {
                        tempJ = ans[j];
                        //don't worry about internal paths
                        if (tempI == tempJ)
                            continue;

                        connCost = mainDist[subgraphVertices.get(i).getMatchId()][subgraphVertices.get(j).getMatchId()];

                        candidateKey = new Pair<Integer>(ans[i], ans[j]);
                        if (!idConn.containsKey(candidateKey)) {
                            idConn.put(candidateKey, new Pair<Integer>(i, j));
                            costMap.put(candidateKey, connCost);
                            edgeMap.put(candidateKey, mainEdgePath[subgraphVertices.get(i).getMatchId()][subgraphVertices.get(j).getMatchId()]);
                            completeCcGraph.addEdge(ans[i], ans[j], connCost, false);
                        } else if (connCost < costMap.get(candidateKey)) {
                            idConn.put(candidateKey, new Pair<Integer>(i, j));
                            costMap.put(candidateKey, connCost);
                            edgeMap.put(candidateKey, mainEdgePath[subgraphVertices.get(i).getMatchId()][subgraphVertices.get(j).getMatchId()]);
                            completeCcGraph.addEdge(ans[i], ans[j], connCost, false);
                        }
                    }
                }

                //solve the mst
                int[] mst = CommonAlgorithms.minCostSpanningTree(completeCcGraph);
                HashMap<Integer, Edge> completeEdges = completeCcGraph.getInternalEdgeMap();
                numEdges = completeCcGraph.getEdges().size();

                //to handle the asymmetric case
                isWindy = false;
                windyEdgeMap = null;
                if(subgraph.getClass() == WindyGraph.class) {
                    isWindy = true;
                    windyEdgeMap = ((WindyGraph)subgraph).getInternalEdgeMap();
                }



                for (int i = 1; i <= numEdges; i++) {
                    if (mst[i] > 0) {
                        tempKey = new Pair<Integer>(completeEdges.get(i).getEndpoints().getFirst().getId(), completeEdges.get(i).getEndpoints().getSecond().getId());
                        //fix for windy case
                        if(isWindy) {
                            dup = windyEdgeMap.get(edgeMap.get(tempKey));
                            ((WindyGraph) subgraph).addEdge(idConn.get(tempKey).getFirst(), idConn.get(tempKey).getSecond(), dup.getCost(), dup.getReverseCost(), false);
                        }
                        else
                        subgraph.addEdge(idConn.get(tempKey).getFirst(), idConn.get(tempKey).getSecond(), costMap.get(tempKey), false);
                        System.out.println("We're connecting component " + tempKey.getFirst() + " was connected to component " + tempKey.getSecond());
                    }
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
