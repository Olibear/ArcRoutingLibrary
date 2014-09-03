package oarlib.graph.transform.rebalance.impl;

import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.exceptions.FormatMismatchException;
import oarlib.graph.transform.rebalance.RebalanceTransformer;

import java.lang.reflect.Array;
import java.util.*;

/**
 * A post processing rebalancer that divides the cost of the route among its constituent vertices.
 */
public class SimpleRouteRebalancer<S extends Graph<?,?>> extends RebalanceTransformer<S> {

    ArrayList<Route> workingSol;
    /**
     * Super constructor for any Rebalance transformers.
     *
     * @param input     - the input graph.
     * @param partition - an ArrayList that has an entry for each vertex in the graph; entry i has value j if vertex
     *                  with internal id i is currently assigned to partition j.
     * @throws oarlib.exceptions.FormatMismatchException - if the ArrayList is of the wrong size.
     */
    public SimpleRouteRebalancer(S input, HashMap<Integer, Integer> partition, ArrayList<Route> sol) throws FormatMismatchException {
        super(input, partition);
        workingSol = sol;
    }

    @Override
    public void setGraph(S input) {
        mGraph = input;
    }

    public void setSol(ArrayList<Route> newSol)
    {
        workingSol = newSol;
    }

    @Override
    public S transformGraph() {
        /*
         * figure out who belongs to which route, and how many times they appear (we don't want to doubly penalize a
         * vertex simply because it shows up in two routes, so we'll average the costs to determine its addition
         */

        ArrayList<HashSet<Integer>> verticesInRoute = new ArrayList<HashSet<Integer>>();
        ArrayList<Integer> numAppearances = new ArrayList<Integer>(mGraph.getVertices().size());
        int firstId, secondId;
        HashSet<Integer> toAdd;
        for(int i = 0; i < workingSol.size(); i ++)
        {
            Route r = workingSol.get(i);
            toAdd = new HashSet<Integer>();
            List<? extends Link<? extends Vertex>> path = r.getRoute();
            HashMap<Integer, Integer> mapping = r.getMapping();
            for(Link<? extends Vertex> l : path)
            {
                if(mapping.isEmpty()) {
                    firstId = l.getEndpoints().getFirst().getId();
                    secondId = l.getEndpoints().getSecond().getId();
                }
                else {
                    firstId = mapping.get(l.getEndpoints().getFirst().getId());
                    secondId = mapping.get(l.getEndpoints().getSecond().getId());
                }

                toAdd.add(firstId);
                toAdd.add(secondId);
                numAppearances.set(firstId, numAppearances.get(firstId) + 1);
                numAppearances.set(secondId, numAppearances.get(secondId) + 1);
            }
            verticesInRoute.add(toAdd);
        }

        //now actually do the addition
        int penalty;
        for(Vertex v : mGraph.getVertices())
        {
            penalty = 0;
            for(int i = 0; i < workingSol.size(); i++)
            {
                if(verticesInRoute.get(i).contains(v.getId()))
                    penalty += workingSol.get(i).getCost();
            }
            v.setCost(v.getCost() + (penalty / numAppearances.get(v.getId())));
        }

        return null;
    }
}
