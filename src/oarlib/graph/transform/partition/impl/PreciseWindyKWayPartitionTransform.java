package oarlib.graph.transform.partition.impl;

import oarlib.core.WindyEdge;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.partition.PartitionTransformer;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oliverlum on 9/13/14.
 */
public class PreciseWindyKWayPartitionTransform implements PartitionTransformer<WindyGraph> {

    private WindyGraph mGraph;

    private boolean mWeighNonReq = false;

    public PreciseWindyKWayPartitionTransform(WindyGraph input) {
        mGraph = input;
    }

    public PreciseWindyKWayPartitionTransform(WindyGraph input, boolean weighNonReq) {
        mGraph = input;
        mWeighNonReq = weighNonReq;
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
            HashMap<Integer, WindyVertex> ansVertices = ans.getInternalVertexMap();
            HashMap<Integer, WindyEdge> mEdges = mGraph.getInternalEdgeMap();

            for (int i = 1; i <= m; i++) {
                temp = mEdges.get(i);
                head = temp.getEndpoints().getSecond();

                //assign the cost:
                if (temp.isRequired() || mWeighNonReq)
                    ansVertices.get(i).setCost((int) ((temp.getCost() + temp.getReverseCost()) * .5));
                else
                    ansVertices.get(i).setCost(0);

                //figure out the conns
                for (ArrayList<WindyEdge> toAdd : head.getNeighbors().values()) {
                    for (WindyEdge e : toAdd) {
                        //to avoid redundancy and self conns
                        if (e.getId() != i) {
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
