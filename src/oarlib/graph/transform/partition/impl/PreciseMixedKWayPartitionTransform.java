package oarlib.graph.transform.partition.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.transform.partition.PartitionTransformer;
import oarlib.graph.transform.rebalance.CostRebalancer;
import oarlib.link.impl.MixedEdge;
import oarlib.vertex.impl.MixedVertex;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oliverlum on 9/13/14.
 */
public class PreciseMixedKWayPartitionTransform implements PartitionTransformer<MixedGraph> {

    private MixedGraph mGraph;
    private HashMap<Integer, Integer> mCostMap;
    private boolean mWeighNonReq;
    private boolean usingCostRebalancer;

    public PreciseMixedKWayPartitionTransform(MixedGraph input) {
        this(input, false, null);
    }

    public PreciseMixedKWayPartitionTransform(MixedGraph input, boolean weighNonReq) {
        this(input, weighNonReq, null);
    }

    public PreciseMixedKWayPartitionTransform(MixedGraph input, boolean weighNonReq, CostRebalancer<MixedGraph> costRebalancer) {
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
    public void setGraph(MixedGraph input) {
        mGraph = input;
    }

    @Override
    public MixedGraph transformGraph() {
        try {
            int m = mGraph.getEdges().size();
            //ans
            MixedGraph ans = new MixedGraph(m);

            //setup
            MixedEdge temp;
            MixedVertex tail, head;
            TIntObjectHashMap<MixedVertex> ansVertices = ans.getInternalVertexMap();
            TIntObjectHashMap<MixedEdge> mEdges = mGraph.getInternalEdgeMap();

            int tempCost;

            for (int i : mEdges.keys()) {
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
                for (ArrayList<MixedEdge> toAdd : head.getNeighbors().values()) {
                    for (MixedEdge e : toAdd) {
                        //to avoid redundancy and self conns
                        if (e.getId() < i) {
                            ans.addEdge(i, e.getId(), 1);
                        }
                    }
                }

                for (ArrayList<MixedEdge> toAdd : tail.getNeighbors().values()) {
                    for (MixedEdge e : toAdd) {
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
