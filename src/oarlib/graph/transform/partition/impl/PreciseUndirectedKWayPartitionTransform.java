package oarlib.graph.transform.partition.impl;

import oarlib.core.Edge;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.transform.partition.PartitionTransformer;
import oarlib.vertex.impl.UndirectedVertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 9/13/14.
 *
 * This partition transformer is a second generation transformer; it tries to be less haphazard about
 * the partition by creating a graph that has a vertex for each link, and vertices are connected if it's possible
 * to consecutively traverse the two links.
 */
public class PreciseUndirectedKWayPartitionTransform implements PartitionTransformer<UndirectedGraph> {

    private UndirectedGraph mGraph;

    public PreciseUndirectedKWayPartitionTransform(UndirectedGraph input) {
        mGraph = input;
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
            HashMap<Integer, UndirectedVertex> ansVertices = ans.getInternalVertexMap();
            HashMap<Integer, Edge> mEdges = mGraph.getInternalEdgeMap();

            for (int i = 1; i <= m; i++) {
                temp = mEdges.get(i);
                tail = temp.getEndpoints().getFirst();
                head = temp.getEndpoints().getSecond();

                //assign the cost:
                if(temp.isRequired())
                    ansVertices.get(i).setCost(temp.getCost());
                else
                    ansVertices.get(i).setCost(0);

                //figure out the conns
                for(ArrayList<Edge> toAdd: tail.getNeighbors().values()) {
                    for (Edge e : toAdd) {
                        //to avoid redundancy and self conns
                        if(e.getId() > i) {
                            ans.addEdge(i, e.getId(), 1);
                        }
                    }
                }
                for(ArrayList<Edge> toAdd: head.getNeighbors().values()) {
                    for (Edge e : toAdd) {
                        //to avoid redundancy and self conns
                        if(e.getId() > i) {
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
