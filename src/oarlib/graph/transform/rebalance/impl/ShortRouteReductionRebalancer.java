package oarlib.graph.transform.rebalance.impl;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.exceptions.FormatMismatchException;
import oarlib.graph.transform.rebalance.RebalanceTransformer;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oliverlum on 9/15/14.
 */
public class ShortRouteReductionRebalancer<S extends Graph<?, ?>> extends RebalanceTransformer<S> {


    ArrayList<Route> workingSol;

    /**
     * Super constructor for any Rebalance transformers.
     *
     * @param input     - the input graph.
     * @param partition - an ArrayList that has an entry for each vertex in the graph; entry i has value j if vertex
     *                  with internal id i is currently assigned to partition j.
     * @throws oarlib.exceptions.FormatMismatchException - if the ArrayList is of the wrong size.
     */
    public ShortRouteReductionRebalancer(S input, HashMap<Integer, Integer> partition, ArrayList<Route> sol) throws FormatMismatchException {
        super(input, partition);
        if (partition.keySet().size() != input.getEdges().size())
            throw new FormatMismatchException("This kind of rebalancer requires a edge-weighted partition");
        workingSol = sol;
    }

    @Override
    public void setGraph(S input) {
        mGraph = input;
    }

    /**
     * Rebalances the graph, (which means that this transformer assumes a PartitionTransformer has already been run on
     * this graph).  Each vertex that represents an edge in the least cost route reduces its cost by a factor of .9
     *
     * @return - a new vertex-weighted graph ready for METIS partitioning.
     */
    @Override
    public S transformGraph() {
        return transformGraph(.9);
    }

    /**
     * Rebalances the graph, (which means that this transformer assumes a PartitionTransformer has already been run on
     * this graph).  Each vertex that represents an edge in the least cost route reduces its cost by a factor of weight
     *
     * @return - a new vertex-weighted graph ready for METIS partitioning.
     */
    public S transformGraph(double weight) {

        //figure out the min cost guy
        double min = Double.MAX_VALUE;
        int minIndex = -1;
        Route r;
        int rCost;
        for (int i = 0; i < workingSol.size(); i++) {
            r = workingSol.get(i);
            rCost = r.getCost();
            if (r.getCost() < min) {
                min = r.getCost();
                minIndex = i;
            }
        }

        //now reduce the cost of each one
        Route minRoute = workingSol.get(minIndex);
        HashMap<Integer, ? extends Vertex> mVertices = mGraph.getInternalVertexMap();
        Vertex temp;

        for (Link l : minRoute.getRoute()) {
            //temp = mVertices.get(l.getId());
            //temp.setCost((int)(temp.getCost() * weight));
            l.setCost((int) (l.getCost() * weight));
        }

        return mGraph;

    }
}
