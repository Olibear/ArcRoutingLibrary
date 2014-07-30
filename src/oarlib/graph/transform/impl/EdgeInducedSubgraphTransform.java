package oarlib.graph.transform.impl;

import oarlib.core.Factory;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
import oarlib.graph.transform.GraphTransformer;
import oarlib.graph.util.CommonAlgorithms;

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
                        System.out.println("In this partition, the depot has id: " + newVertexCounter);
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
                        System.out.println("In this partition, the depot has id: " + newVertexCounter);
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
                System.out.println("In this partition, the depot has id: " + newVertexCounter);
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
}
