package oarlib.graph.transform.impl;

import oarlib.core.*;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.GraphTransformer;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 9/19/14.
 */
public class EdgeInducedRequirementTransform<S extends Graph<?, ?>> implements GraphTransformer<S, S> {

    S mGraph;
    HashSet<Integer> mEdges;
    Factory<S> mFactory;

    /**
     * Transformer, primarily for rural problems.  This takes a set of ids, and returns the same graph, but where the only required edges are the
     * ones specified.  This ensures maximum flexibility, and free connectivity
     */
    public EdgeInducedRequirementTransform(S graph, Factory<S> sFactory, HashSet<Integer> ids) {
        mGraph = graph;
        mEdges = ids;
        mFactory = sFactory;
    }

    @Override
    public void setGraph(S input) {
        mGraph = input;
    }

    @Override
    public S transformGraph() {

        try {
            S blankGraph = mFactory.instantiate();
            blankGraph.setDepotId(mGraph.getDepotId());
            int n = mGraph.getVertices().size();
            int m = mGraph.getEdges().size();
            HashMap<Integer, ? extends Vertex> blankVertices = blankGraph.getInternalVertexMap();
            HashMap<Integer, ? extends Link<? extends Vertex>> mGraphEdges = mGraph.getInternalEdgeMap();

            boolean isWindy = mGraph.getClass() == WindyGraph.class;

            for (int i = 1; i <= n; i++) {
                blankGraph.addVertex();
                blankVertices.get(i).setMatchId(i);
            }

            Link<? extends Vertex> l;
            for (int i = 1; i <= m; i++) {
                l = mGraphEdges.get(i);

                if (isWindy) {
                    if (mEdges.contains(l.getId())) {
                        int revCost = ((WindyEdge) l).getReverseCost();
                        ((WindyGraph) blankGraph).addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), l.getCost(), ((WindyEdge) l).getReverseCost(), l.isRequired());
                    } else {
                        int revCost = ((WindyEdge) l).getReverseCost();
                        ((WindyGraph) blankGraph).addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), l.getCost(), ((WindyEdge) l).getReverseCost(), false);
                    }
                } else {
                    if (mEdges.contains(l.getId())) {
                        int revCost = ((WindyEdge) l).getReverseCost();
                        blankGraph.addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), l.getCost(), l.isRequired());
                    } else {
                        blankGraph.addEdge(l.getEndpoints().getFirst().getId(), l.getEndpoints().getSecond().getId(), l.getCost(), false);
                    }
                }
            }

            //to make sure the depot gets included
            int depotId = blankGraph.getDepotId();
            blankGraph.addEdge(depotId, depotId, 0, true);

            return blankGraph;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
