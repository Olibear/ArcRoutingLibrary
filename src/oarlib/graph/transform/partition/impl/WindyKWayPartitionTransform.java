package oarlib.graph.transform.partition.impl;

import oarlib.core.WindyEdge;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.partition.PartitionTransformer;
import oarlib.vertex.impl.WindyVertex;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Graph transformer that takes an edge-weighted graph, and produces a vertex-weighted graph such that a
 * a k-way weighted vertex partition of the transformed graph corresponds to an equal k-way edge partition
 * in the original graph.  We anticipate using this to turn our single-vehicle solvers into capacitated solvers.
 *
 * @author oliverlum
 */
public class WindyKWayPartitionTransform implements PartitionTransformer<WindyGraph> {

    private WindyGraph mGraph;

    public WindyKWayPartitionTransform(WindyGraph input) {
        mGraph = input;
    }

    @Override
    public WindyGraph transformGraph() {

        try {
            int n = mGraph.getVertices().size();
            //ans
            WindyGraph ans = new WindyGraph(n);

            //setup
            WindyVertex temp;
            HashMap<Integer, WindyVertex> ansVertices = ans.getInternalVertexMap();
            HashSet<WindyEdge> edges = mGraph.getEdges();
            int firstId, secondId, sumCost;

            for (WindyEdge e : edges) {


                firstId = e.getEndpoints().getFirst().getId();
                secondId = e.getEndpoints().getSecond().getId();

                ans.addEdge(firstId, secondId, 1);

                if(!e.isRequired())
                    continue;
                sumCost = e.getCost() + e.getReverseCost();

                temp = ansVertices.get(firstId);
                temp.setCost(temp.getCost() + sumCost);

                temp = ansVertices.get(secondId);
                temp.setCost(temp.getCost() + sumCost);


            }
            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void setGraph(WindyGraph input) {
        mGraph = input;
    }

}
