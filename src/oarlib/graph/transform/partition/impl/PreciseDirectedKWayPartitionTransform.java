package oarlib.graph.transform.partition.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.transform.partition.PartitionTransformer;
import oarlib.graph.transform.rebalance.CostRebalancer;
import oarlib.link.impl.Arc;
import oarlib.vertex.impl.DirectedVertex;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oliverlum on 9/13/14.
 */
public class PreciseDirectedKWayPartitionTransform implements PartitionTransformer<DirectedGraph> {

    private DirectedGraph mGraph;
    private HashMap<Integer, Integer> mCostMap;
    private boolean mWeighNonReq;
    private boolean usingCostRebalancer;

    public PreciseDirectedKWayPartitionTransform(DirectedGraph input) {
        this(input, false, null);
    }

    public PreciseDirectedKWayPartitionTransform(DirectedGraph input, boolean weighNonReq) {
        this(input, weighNonReq, null);
    }

    public PreciseDirectedKWayPartitionTransform(DirectedGraph input, boolean weighNonReq, CostRebalancer<DirectedGraph> costRebalancer) {
        mGraph = input;
        mWeighNonReq = weighNonReq;
        if (costRebalancer != null) {
            mCostMap = costRebalancer.rebalance();
            usingCostRebalancer = true;
        } else {
            usingCostRebalancer = false;
        }

    }

    @Override
    public void setGraph(DirectedGraph input) {
        mGraph = input;
    }

    @Override
    public DirectedGraph transformGraph() {
        try {
            int m = mGraph.getEdges().size();
            //ans
            DirectedGraph ans = new DirectedGraph(m);

            //setup
            Arc temp;
            DirectedVertex tail, head;
            TIntObjectHashMap<DirectedVertex> ansVertices = ans.getInternalVertexMap();
            TIntObjectHashMap<Arc> mEdges = mGraph.getInternalEdgeMap();

            int tempCost;

            for (Integer i : mEdges.keys()) {
                temp = mEdges.get(i);
                tempCost = temp.getCost();
                if (usingCostRebalancer)
                    tempCost = mCostMap.get(i);
                head = temp.getEndpoints().getSecond();
                tail = temp.getEndpoints().getFirst();

                //assign the cost:
                if (temp.isRequired() || mWeighNonReq)
                    ansVertices.get(i).setCost(tempCost);
                else
                    ansVertices.get(i).setCost(0);

                //figure out the conns
                for (ArrayList<Arc> toAdd : head.getNeighbors().values()) {
                    for (Arc e : toAdd) {
                        //to avoid redundancy and self conns
                        if (e.getId() < i) {
                            ans.addEdge(i, e.getId(), 1);
                        }
                    }
                }

                for (ArrayList<Arc> toAdd : tail.getNeighbors().values()) {
                    for (Arc e : toAdd) {
                        //to avoid redundancy and self conns
                        if (e.getId() < i) {
                            ans.addEdge(i, e.getId(), 1);
                        }
                    }
                }
            }

            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
