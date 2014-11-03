package oarlib.graph.transform.partition.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.WindyEdge;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.partition.PartitionTransformer;
import oarlib.graph.transform.rebalance.CostRebalancer;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oliverlum on 9/13/14.
 */
public class PreciseWindyKWayPartitionTransform implements PartitionTransformer<WindyGraph> {

    private WindyGraph mGraph;
    private HashMap<Integer, Integer> mCostMap;
    private boolean mWeighNonReq;
    private boolean usingCostRebalancer;

    public PreciseWindyKWayPartitionTransform(WindyGraph input) {
        this(input, false, null);
    }

    public PreciseWindyKWayPartitionTransform(WindyGraph input, boolean weighNonReq) {
        this(input, weighNonReq, null);
    }

    public PreciseWindyKWayPartitionTransform(WindyGraph input, boolean weighNonReq, CostRebalancer<WindyGraph> costRebalancer) {
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
    public void setGraph(WindyGraph input) {
        mGraph = input;
    }

    @Override
    public WindyGraph transformGraph() {
        try {
            int m = mGraph.getEdges().size();
            //ans
            WindyGraph ans = new WindyGraph(m);

            //setup
            WindyEdge temp;
            WindyVertex tail, head;
            TIntObjectHashMap<WindyVertex> ansVertices = ans.getInternalVertexMap();
            TIntObjectHashMap<WindyEdge> mEdges = mGraph.getInternalEdgeMap();

            int tempCost;

            for (int i : mEdges.keys()) {
                temp = mEdges.get(i);
                tempCost = (int) ((temp.getCost() + temp.getReverseCost()) * .5);
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
                for (ArrayList<WindyEdge> toAdd : head.getNeighbors().values()) {
                    for (WindyEdge e : toAdd) {
                        //to avoid redundancy and self conns
                        if (e.getId() < i) {
                            ans.addEdge(i, e.getId(), 1);
                        }
                    }
                }

                for (ArrayList<WindyEdge> toAdd : tail.getNeighbors().values()) {
                    for (WindyEdge e : toAdd) {
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
