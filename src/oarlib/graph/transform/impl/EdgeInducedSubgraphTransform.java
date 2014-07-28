package oarlib.graph.transform.impl;

import oarlib.core.Factory;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Vertex;
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
    Factory<S> graphGen;
    HashSet<Integer> mEdges;
    public EdgeInducedSubgraphTransform(S input, Factory<S> sFactory, HashSet<Integer> edges) {
        setGraph(input);
        setEdges(edges);
        graphGen = sFactory;
    }

    @Override
    public void setGraph(S input) {
        mGraph = input;
    }

    @Override
    public S transformGraph() {
        try {
            S blankGraph = graphGen.instantiate();
            //go through the edge, and create the induced graph
            HashMap<Integer, ? extends Link<? extends Vertex>> indexedEdges = mGraph.getInternalEdgeMap();
            HashMap<Integer, ? extends Vertex> indexedVertices = mGraph.getInternalVertexMap();
            HashSet<Integer> addedVertices = new HashSet<Integer>();
            int firstId, secondId;
            int newVertexCounter = 1;
            Vertex first, second;
            Link<? extends Vertex> temp;

            for (Integer i : mEdges) {
                temp = indexedEdges.get(i);
                first = temp.getEndpoints().getFirst();
                firstId = first.getId();
                if (!addedVertices.contains(firstId)) {
                    addedVertices.add(firstId);
                    blankGraph.addVertex();

                    //set the match id for later
                    first.setMatchId(newVertexCounter++);

                }

                second = temp.getEndpoints().getSecond();
                secondId = second.getId();
                if (!addedVertices.contains(secondId)) {
                    addedVertices.add(secondId);
                    blankGraph.addVertex();

                    //set the match id for later
                    second.setMatchId(newVertexCounter++);
                }

                //now add the edges in there
                blankGraph.addEdge(first.getMatchId(), second.getMatchId(), temp.getCost());
            }

            return blankGraph;
        } catch(Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public HashSet<Integer> getEdges() {
        return mEdges;
    }

    public void setEdges(HashSet<Integer> newEdges) {
        mEdges = newEdges;
    }
}
