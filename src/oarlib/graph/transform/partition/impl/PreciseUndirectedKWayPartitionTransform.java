package oarlib.graph.transform.partition.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.link.impl.Edge;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.transform.partition.PartitionTransformer;
import oarlib.graph.transform.rebalance.CostRebalancer;
import oarlib.vertex.impl.UndirectedVertex;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oliverlum on 9/13/14.
 */
public class PreciseUndirectedKWayPartitionTransform implements PartitionTransformer<UndirectedGraph> {

    private UndirectedGraph mGraph;
    private HashMap<Integer, Integer> mCostMap;
    private boolean mWeighNonReq;
    private boolean usingCostRebalancer;

    public PreciseUndirectedKWayPartitionTransform(UndirectedGraph input) {
        this(input, false, null);
    }

    public PreciseUndirectedKWayPartitionTransform(UndirectedGraph input, boolean weighNonReq) {
        this(input, weighNonReq, null);
    }

    public PreciseUndirectedKWayPartitionTransform(UndirectedGraph input, boolean weighNonReq, CostRebalancer<UndirectedGraph> costRebalancer) {
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
    public void setGraph(UndirectedGraph input) {
        mGraph = input;
    }

    @Override
    public UndirectedGraph transformGraph() {
        try {
            int m = mGraph.getEdges().size();
            //ans
            UndirectedGraph ans = new UndirectedGraph(m);

            //setup
            Edge temp;
            UndirectedVertex tail, head;
            TIntObjectHashMap<UndirectedVertex> ansVertices = ans.getInternalVertexMap();
            TIntObjectHashMap<Edge> mEdges = mGraph.getInternalEdgeMap();

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
                for (ArrayList<Edge> toAdd : head.getNeighbors().values()) {
                    for (Edge e : toAdd) {
                        //to avoid redundancy and self conns
                        if (e.getId() < i) {
                            ans.addEdge(i, e.getId(), 1);
                        }
                    }
                }

                for (ArrayList<Edge> toAdd : tail.getNeighbors().values()) {
                    for (Edge e : toAdd) {
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
