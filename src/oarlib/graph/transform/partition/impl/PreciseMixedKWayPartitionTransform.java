package oarlib.graph.transform.partition.impl;

import oarlib.core.Arc;
import oarlib.core.MixedEdge;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.transform.partition.PartitionTransformer;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.MixedVertex;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oliverlum on 9/13/14.
 */
public class PreciseMixedKWayPartitionTransform implements PartitionTransformer<MixedGraph> {

    private MixedGraph mGraph;

    public PreciseMixedKWayPartitionTransform(MixedGraph input) {
        mGraph = input;
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
            HashMap<Integer, MixedVertex> ansVertices = ans.getInternalVertexMap();
            HashMap<Integer, MixedEdge> mEdges = mGraph.getInternalEdgeMap();

            for (int i = 1; i <= m; i++) {
                temp = mEdges.get(i);
                head = temp.getEndpoints().getSecond();

                //assign the cost:
                if(temp.isRequired())
                    ansVertices.get(i).setCost(temp.getCost());
                else
                    ansVertices.get(i).setCost(0); //we're gonna throw them all in anyways

                //figure out the conns
                for (ArrayList<MixedEdge> toAdd : head.getNeighbors().values()) {
                    for (MixedEdge e : toAdd) {
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
