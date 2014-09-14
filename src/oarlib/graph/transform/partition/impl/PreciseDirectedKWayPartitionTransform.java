package oarlib.graph.transform.partition.impl;

import oarlib.core.Arc;
import oarlib.core.Edge;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.transform.partition.PartitionTransformer;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by oliverlum on 9/13/14.
 */
public class PreciseDirectedKWayPartitionTransform implements PartitionTransformer<DirectedGraph> {

    private DirectedGraph mGraph;

    public PreciseDirectedKWayPartitionTransform(DirectedGraph input) {
        mGraph = input;
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
            HashMap<Integer, DirectedVertex> ansVertices = ans.getInternalVertexMap();
            HashMap<Integer, Arc> mEdges = mGraph.getInternalEdgeMap();

            for (int i = 1; i <= m; i++) {
                temp = mEdges.get(i);
                head = temp.getEndpoints().getSecond();

                //assign the cost:
                if(temp.isRequired())
                    ansVertices.get(i).setCost(temp.getCost());
                else
                    ansVertices.get(i).setCost(0);

                //figure out the conns
                for (ArrayList<Arc> toAdd : head.getNeighbors().values()) {
                    for (Arc e : toAdd) {
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
