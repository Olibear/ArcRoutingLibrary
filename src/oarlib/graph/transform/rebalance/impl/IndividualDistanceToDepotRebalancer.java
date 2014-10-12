package oarlib.graph.transform.rebalance.impl;

import oarlib.core.Graph;
import oarlib.core.Vertex;
import oarlib.exceptions.FormatMismatchException;
import oarlib.graph.transform.rebalance.RebalanceTransformer;
import oarlib.graph.util.CommonAlgorithms;

import java.util.HashMap;

/**
 * Created by oliverlum on 9/14/14.
 */
public class IndividualDistanceToDepotRebalancer<S extends Graph<?, ?>> extends RebalanceTransformer<S> {

    /**
     * Super constructor for any Rebalance transformers.
     *
     * @param input     - the input graph.
     * @param partition - an ArrayList that has an entry for each vertex in the graph; entry i has value j if vertex
     *                  with internal id i is currently assigned to partition j.
     * @throws oarlib.exceptions.FormatMismatchException - if the ArrayList is of the wrong size.
     */
    public IndividualDistanceToDepotRebalancer(S input, HashMap<Integer, Integer> partition) throws FormatMismatchException {
        super(input, partition);
        if (partition.keySet().size() != input.getVertices().size())
            throw new FormatMismatchException("This kind of rebalancer requires a vertex-weighted partition");
    }

    @Override
    public void setGraph(S input) {
        mGraph = input;
    }

    /**
     * Rebalances the graph, (which means that this transformer assumes a PartitionTransformer has already been run on
     * this graph).  Each vertex weight gets augmented the cost of its dijkstra distance.
     *
     * @return - a new vertex-weighted graph ready for METIS partitioning.
     */
    @Override
    public S transformGraph() {
        return transformGraph(1);
    }

    public S transformGraph(int weight) {
        //calculate shortest paths
        int n = mGraph.getVertices().size();
        int[] dist = new int[n + 1];
        int[] path = new int[n + 1];
        CommonAlgorithms.dijkstrasAlgorithm(mGraph, mGraph.getDepotId(), dist, path);

        //update the vertex weights for each
        HashMap<Integer, ? extends Vertex> indexedVertices = mGraph.getInternalVertexMap();
        for (int i = 1; i <= n; i++) {
            if (i == mGraph.getDepotId())
                continue;
            indexedVertices.get(i).setCost(indexedVertices.get(i).getCost() + dist[i]);
        }

        return mGraph;

    }
}
