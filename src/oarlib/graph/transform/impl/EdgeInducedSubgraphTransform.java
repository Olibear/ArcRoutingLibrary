package oarlib.graph.transform.impl;

import oarlib.core.*;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.transform.GraphTransformer;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 *
 * Transform to fetch the subgraph induced by the edges with the ids specified.
 * Created by Oliver Lum on 7/26/2014.
 */
public class EdgeInducedSubgraphTransform<S extends Graph<?,?>> implements GraphTransformer<S,S> {

    S mGraph;
    Factory<S> graphGen;
    HashSet<Integer> mEdges;
    boolean inclDepot;

    /**
     * This transformer takes the input graph and the specified edge ids, and returns the subgraph induced by said edges.
     * @param input - the whole graph from which the subgraph is generated
     * @param sFactory - a factory that constructs an empty graph of type S to put our ans in
     * @param edges - the edge ids which induce the desired subgraph
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
     * @param input - the whole graph from which the subgraph is generated
     * @param sFactory - a factory that constructs an empty graph of type S to put our ans in
     * @param edges - the edge ids which induce the desired subgraph
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

            for (Integer i : mEdges) {

                temp = indexedEdges.get(i);
                first = temp.getEndpoints().getFirst();
                firstId = first.getId();

                if (!addedVertices.contains(firstId)) {
                    addedVertices.add(firstId);
                    blankGraph.addVertex();

                    //set the match id for later
                    blankVertices.get(newVertexCounter).setMatchId(firstId);
                    if(firstId == depotId) {
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
                    if(secondId == depotId) {
                        blankGraph.setDepotId(newVertexCounter);
                    }
                    second.setMatchId(newVertexCounter++);

                }

                //now add the edges in there
                blankGraph.addEdge(first.getMatchId(), second.getMatchId(), temp.getCost());
            }

            //if the depot isn't in the list, then add it, and connect up with the shortest path costs
            if(inclDepot && !addedVertices.contains(depotId))
            {
                //add it
                blankGraph.addVertex();
                Vertex depot  = indexedVertices.get(depotId);
                blankVertices.get(newVertexCounter).setMatchId(depotId);
                depot.setMatchId(newVertexCounter);
                blankGraph.setDepotId(newVertexCounter);

                //connect depot to partition
                int n = mGraph.getVertices().size();
                int[] dijkstraDist = new int[n+1];
                int[] dijkstraPath = new int[n+1];
                int[] dijkstraEdges = new int[n+1];
                CommonAlgorithms.dijkstrasAlgorithm(mGraph, depotId,dijkstraDist,dijkstraPath,dijkstraEdges);

                int bestConnectId = -1; //the id of the vertex with the shortest distance to the depot node
                boolean minSet = false;
                double bestCost = 0;
                for(Integer i : addedVertices)
                {
                    if(bestCost < dijkstraDist[i] || !minSet)
                    {
                        bestCost = dijkstraDist[i];
                        bestConnectId = i;
                        minSet = true;
                    }
                }

                //convert it to blankGraph ids
                int bestConnectMatchId = indexedVertices.get(bestConnectId).getMatchId();
                blankGraph.addEdge(depot.getMatchId(), bestConnectMatchId, dijkstraDist[bestConnectId]);

                //connect partition to depot
                CommonAlgorithms.dijkstrasAlgorithm(mGraph, bestConnectId, dijkstraDist, dijkstraPath, dijkstraEdges);
                blankGraph.addEdge(bestConnectMatchId, depot.getMatchId(), dijkstraDist[depotId]);

            }
            repairConnectivity(blankGraph);

            return blankGraph;
        } catch(Exception e)
        {
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
     *
     * The strongly connected components of the graph are computed, and then connected
     * with the shortest link to and from each of them to another component
     *
     * @param subgraph
     */
    private void repairConnectivity(S subgraph)
    {
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

            if(nScc == 1)
                return; //no repair necessary

            //compute the shortest paths
            int[][] sccDist = new int[sccN+1][sccN+1];
            int[][] sccPath = new int[sccN+1][sccN+1];
            CommonAlgorithms.fwLeastCostPaths(sccGraph, sccDist, sccPath);

            int[][] mainDist = new int[mainN+1][mainN+1];
            int[][] mainPath = new int[mainN+1][mainN+1];
            CommonAlgorithms.fwLeastCostPaths(mGraph, mainDist, mainPath);

            //connect the sccs of the partition

            //first figure out which connections we could add
            int connCost;
            Pair<Integer> candidateKey;
            HashMap<Integer, ? extends Vertex> subgraphVertices = subgraph.getInternalVertexMap();
            HashMap<Pair<Integer>, Pair<Integer>> idConn = new HashMap<Pair<Integer>, Pair<Integer>>();
            HashMap<Pair<Integer>, Integer> costMap = new HashMap<Pair<Integer>, Integer>();

            DirectedGraph completeSccGraph = new DirectedGraph(nScc);
            HashMap<Integer, DirectedVertex> completeVertices = completeSccGraph.getInternalVertexMap();
            ArrayList<Boolean> realEdge = new ArrayList<Boolean>();
            //entry 0 useless
            realEdge.add(true);
            int tempI, tempJ;
            DirectedVertex vi, vj;
            HashSet<Pair<Integer>> alreadyAdded = new HashSet<Pair<Integer>>();
            Pair<Integer> tempKey;

            for(int i = 1; i <= sccN; i++)
            {
                tempI = ans[i];
                for(int j = 1; j <= sccN; j++)
                {
                    tempJ = ans[j];
                    //don't worry about internal paths
                    if(tempI == tempJ)
                        continue;

                    //if the dist is inf. then it's not actually connected
                    if(sccDist[i][j] < totalCost) {
                        completeSccGraph.addEdge(tempI,tempJ,sccDist[i][j]);
                        realEdge.add(true);
                        tempKey = new Pair<Integer>(tempI, tempJ);

                        if(!alreadyAdded.contains(tempKey)) {
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
                    if(!idConn.containsKey(candidateKey))
                    {
                        idConn.put(candidateKey, new Pair<Integer>(i,j));
                        costMap.put(candidateKey, connCost);
                        completeSccGraph.addEdge(ans[i], ans[j], connCost);
                        realEdge.add(false);
                    }
                    else if(connCost < costMap.get(candidateKey))
                    {
                        idConn.put(candidateKey, new Pair<Integer>(i,j));
                        costMap.put(candidateKey, connCost);
                        completeSccGraph.addEdge(ans[i], ans[j], connCost);
                        realEdge.add(false);
                    }
                }
            }

            /*
            Debug
             */
            for(int i = 1; i <= nScc; i++)
                System.out.println("The demand of vertex" + i + " is " + completeVertices.get(i).getDemand());

            //solve the min cost flow
            int[] flowanswer = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(completeSccGraph);
            HashMap<Integer, Arc> completeArcs = completeSccGraph.getInternalEdgeMap();
            int numEdges = completeSccGraph.getEdges().size();
            for(int i = 1; i <= numEdges; i++)
            {
                if(!realEdge.get(i) && flowanswer[i] > 0) {
                    tempKey = new Pair<Integer>(completeArcs.get(i).getTail().getId(), completeArcs.get(i).getHead().getId());
                    subgraph.addEdge(idConn.get(tempKey).getFirst(), idConn.get(tempKey).getSecond(), costMap.get(tempKey));
                    System.out.println("We're connecting component " + tempKey.getFirst() + " was connected to component " + tempKey.getSecond() );
                }
            }

            return;

        } catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
    }
}
