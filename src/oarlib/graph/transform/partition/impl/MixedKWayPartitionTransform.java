package oarlib.graph.transform.partition.impl;

import oarlib.core.MixedEdge;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.transform.partition.PartitionTransformer;
import oarlib.vertex.impl.MixedVertex;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Graph transformer that takes an edge-weighted graph, and produces a vertex-weighted graph such that a
 * a k-way weighted vertex partition of the transformed graph corresponds to an equal k-way edge partition
 * in the original graph.  We anticipate using this to turn our single-vehicle solvers into capacitated solvers.
 *
 * @author oliverlum
 */
public class MixedKWayPartitionTransform implements PartitionTransformer<MixedGraph> {

    private MixedGraph mGraph;

    public MixedKWayPartitionTransform(MixedGraph input) {
        mGraph = input;
    }

    @Override
    public MixedGraph transformGraph() {

        try {
            int n = mGraph.getVertices().size();
            //ans
            MixedGraph ans = new MixedGraph(n);

            //setup
            MixedVertex temp;
            HashMap<Integer, MixedVertex> ansVertices = ans.getInternalVertexMap();
            HashSet<MixedEdge> edges = mGraph.getEdges();
            int firstId, secondId;

            for (MixedEdge e : edges) {
                if (e.isDirected()) {
                    //do the directed thing
                    firstId = e.getHead().getId();

                    temp = ansVertices.get(firstId);
                    temp.setCost(temp.getCost() + (2 * e.getCost()));

                    ans.addEdge(e.getTail().getId(), e.getHead().getId(), 1);
                } else {
                    //do the undirected thing
                    firstId = e.getEndpoints().getFirst().getId();
                    secondId = e.getEndpoints().getSecond().getId();

                    temp = ansVertices.get(firstId);
                    temp.setCost(temp.getCost() + e.getCost());

                    temp = ansVertices.get(secondId);
                    temp.setCost(temp.getCost() + e.getCost());

                    ans.addEdge(firstId, secondId, 1);

                }
            }
            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setGraph(MixedGraph input) {
        mGraph = input;
    }

}
