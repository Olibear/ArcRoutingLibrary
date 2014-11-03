package oarlib.graph.transform.rebalance.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
import oarlib.exceptions.FormatMismatchException;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.rebalance.CostRebalancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by oliverlum on 10/27/14.
 * Takes each edge that we know we're going to have to duplicate and doubles its cost.  This is to stop long chains from
 * throwing off the partition
 */
public class DuplicateEdgeCostRebalancer<S extends Graph<?, ?>> extends CostRebalancer<S> {

    /**
     * Super constructor for any Rebalance transformers.
     *
     * @param input - the input graph.
     * @throws oarlib.exceptions.FormatMismatchException - if the ArrayList is of the wrong size.
     */
    public DuplicateEdgeCostRebalancer(S input) throws FormatMismatchException {
        super(input, null);
    }

    public DuplicateEdgeCostRebalancer(S input, CostRebalancer<S> nextRebalancer) {
        super(input, nextRebalancer);
    }

    public void setGraph(S input) {
        mGraph = input;
    }

    @Override
    protected HashMap<Integer, Integer> startRebalance(HashMap<Integer, Integer> input) {
        Graph<? extends Vertex, ? extends Link<? extends Vertex>> copy = mGraph.getDeepCopy();

        HashMap<Integer, Integer> ans = input;
        boolean isWindy = false;
        if (mGraph.getClass() == WindyGraph.class)
            isWindy = true;

        Stack<Integer> toCheck = new Stack<Integer>();

        for (Vertex v : copy.getVertices())
            toCheck.push(v.getId());

        //find a vertex of degree 1
        TIntObjectHashMap<? extends Vertex> indexedVertices = copy.getInternalVertexMap();
        Map<? extends Vertex, ? extends List<? extends Link<? extends Vertex>>> tempNeighbors;
        Vertex v;
        int lid;
        while (!toCheck.isEmpty()) {
            v = indexedVertices.get(toCheck.pop());
            tempNeighbors = v.getNeighbors();
            if (tempNeighbors.size() != 1)
                continue;
            for (Vertex v2 : tempNeighbors.keySet()) {
                if (tempNeighbors.get(v2).size() != 1)
                    continue;

                lid = tempNeighbors.get(v2).get(0).getId();
                copy.removeEdge(lid);
                ans.put(lid, 2 * ans.get(lid));
                if (!toCheck.contains(v2.getId()))
                    toCheck.push(v2.getId());

            }
        }

        return ans;
    }
}
