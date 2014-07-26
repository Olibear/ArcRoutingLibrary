package oarlib.graph.transform.impl;

import oarlib.core.Factory;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
import oarlib.graph.impl.MutableGraph;
import oarlib.graph.transform.GraphTransformer;

import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * Transform to fetch the subgraph induced by the edges with the ids specified.
 * Created by Oliver Lum on 7/26/2014.
 */
public class EdgeInducedSubgraphTransform<S extends Graph<?,?>> implements GraphTransformer<S,S> {

    S mGraph;
    S blankGraph;
    HashSet<Integer> mEdges;
    public EdgeInducedSubgraphTransform(S input, Factory<S> sFactory, HashSet<Integer> edges) {
        setGraph(input);
        setEdges(edges);
        blankGraph = sFactory.instantiate();
    }

    @Override
    public void setGraph(S input) {
        mGraph = input;
    }

    @Override
    public S transformGraph() {
        //go through the edge, and create the induced graph
        HashMap<Integer, ? extends Link<? extends Vertex>> indexedEdges = mGraph.getInternalEdgeMap();
        HashMap<Integer, ? extends Vertex> indexedVertices = mGraph.getInternalVertexMap();
        HashSet<Integer> addedVertices = new HashSet<Integer>();
        int firstId, secondId;
        int newVertexCounter = 1;

        for(Integer i: mEdges)
        {
            firstId = indexedEdges.get(i).getEndpoints().getFirst().getId();
            if(!addedVertices.contains(firstId))
            {
                addedVertices.add(firstId);
                blankGraph.addVertex();

                //set the match id for later
                indexedVertices.get(firstId).setMatchId(newVertexCounter++);

            }

            secondId = indexedEdges.get(i).getEndpoints().getSecond().getId();
            if(!addedVertices.contains(secondId))
            {
                addedVertices.add(secondId);
                blankGraph.addVertex();

                //set the match id for later
                indexedVertices.get(secondId).setMatchId(newVertexCounter++);
            }
        }
        return null;
    }

    public HashSet<Integer> getEdges() {
        return mEdges;
    }

    public void setEdges(HashSet<Integer> newEdges) {
        mEdges = newEdges;
    }
}
