package oarlib.graph.transform.impl;

import oarlib.core.Factory;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
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

            System.out.println("blankGraph has: " + blankGraph.getEdges().size() + " links before.");
            repairConnectivity(blankGraph);
            System.out.println("blankGraph has: " + blankGraph.getEdges().size() + " links after.");

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
            System.out.println("blankGraph (unrepaired) has: " + nScc + " SCCs.");

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
            int connCost;
            Pair<Integer> candidateKey;
            HashMap<Integer, ? extends Vertex> subgraphVertices = subgraph.getInternalVertexMap();
            HashMap<Pair<Integer>, Pair<Integer>> idConn = new HashMap<Pair<Integer>, Pair<Integer>>();
            HashMap<Pair<Integer>, Integer> costMap = new HashMap<Pair<Integer>, Integer>();
            for(int i = 1; i <= sccN; i++)
            {
                for(int j = 1; j <= sccN; j++)
                {
                    //don't worry about internal paths
                    if(ans[i] == ans[j])
                        continue;

                    //if the dist is inf. then it's not actually connected
                    if(sccDist[i][j] < totalCost)
                        continue;

                    connCost = mainDist[subgraphVertices.get(i).getMatchId()][subgraphVertices.get(j).getMatchId()];

                    candidateKey = new Pair<Integer>(ans[i], ans[j]);
                    if(!idConn.containsKey(candidateKey))
                    {
                        idConn.put(candidateKey, new Pair<Integer>(i,j));
                        costMap.put(candidateKey, connCost);
                    }
                    else if(connCost < costMap.get(candidateKey))
                    {
                        idConn.put(candidateKey, new Pair<Integer>(i,j));
                        costMap.put(candidateKey, connCost);
                    }
                }
            }

            //now connect
            HashSet<Integer> connected = new HashSet<Integer>();
            for(Pair<Integer> key: idConn.keySet())
            {
                /*
                if(connected.contains(key.getFirst()) && connected.contains(key.getSecond())) {
                    continue;
                }*/

                subgraph.addEdge(idConn.get(key).getFirst(), idConn.get(key).getSecond(), costMap.get(key));
                connected.add(key.getFirst());
                connected.add(key.getSecond());
            }

            /**
             * DEBUG CHECK IF CONNECTIVITY REPAIR WORKED
             */
            System.out.println("Debug Start");
            DirectedGraph debugGraph = new DirectedGraph(subgraph.getVertices().size());
            for (Link<? extends Vertex> l : subgraph.getEdges()) {
                if (l.isDirected())
                    debugGraph.addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), 1);
                else {
                    debugGraph.addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), 1);
                    debugGraph.addEdge(l.getEndpoints().getSecond().getId(), l.getEndpoints().getFirst().getId(), 1);
                }
                totalCost += l.getCost();
            }

            int[] resultSCC = CommonAlgorithms.stronglyConnectedComponents(debugGraph);
            System.out.println("numSCCs after repair: " + resultSCC[0] + ".");
            System.out.println("Debug Stop");

            return;

        } catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
    }
}
