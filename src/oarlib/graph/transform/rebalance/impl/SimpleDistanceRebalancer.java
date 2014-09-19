package oarlib.graph.transform.rebalance.impl;

import oarlib.core.Graph;
import oarlib.core.Vertex;
import oarlib.exceptions.FormatMismatchException;
import oarlib.graph.transform.rebalance.RebalanceTransformer;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Naive first attempt at rebalance step for the k-Vehicle solvers; this just takes the distance from
 * the depot to the closest vertex in the partition, and divides the cost up equally among members of the partition.
 * <p/>
 * Created by oliverlum on 8/9/14.
 */
public class SimpleDistanceRebalancer<S extends Graph<?, ?>> extends RebalanceTransformer<S> {

    public SimpleDistanceRebalancer(S input, HashMap<Integer, Integer> partition) throws FormatMismatchException {
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
     * this graph).  All vertices take a hit equal to the distance from the depot to the
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

        //figure out the shortest distance from depot to each part
        HashMap<Integer, Pair<Integer>> minDist = new HashMap<Integer, Pair<Integer>>();
        HashMap<Integer, Integer> numVerticesPerPart = new HashMap<Integer, Integer>();
        int partNumber;
        for (int i = 1; i <= n; i++) {
            partNumber = mPartition.get(i);
            if (!minDist.containsKey(partNumber) || minDist.get(partNumber).getSecond() > dist[i])
                minDist.put(partNumber, new Pair<Integer>(i, dist[i]));
            if (!numVerticesPerPart.containsKey(partNumber))
                numVerticesPerPart.put(partNumber, 1);
            else
                numVerticesPerPart.put(partNumber, numVerticesPerPart.get(partNumber) + 1);
        }

        //update the vertex weights for each
        HashMap<Integer, ? extends Vertex> indexedVertices = mGraph.getInternalVertexMap();
        for (int i = 1; i <= n; i++) {
            partNumber = mPartition.get(i);
            indexedVertices.get(i).setCost(indexedVertices.get(i).getCost() + weight * minDist.get(partNumber).getSecond() / numVerticesPerPart.get(partNumber));
        }

        return mGraph;
    }
}
