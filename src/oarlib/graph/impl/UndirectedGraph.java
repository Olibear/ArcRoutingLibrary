package oarlib.graph.impl;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.util.Pair;
import oarlib.link.impl.Edge;
import oarlib.vertex.impl.UndirectedVertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * First attempts at an Undirected Graph.
 *
 * @author Oliver
 */
public class UndirectedGraph extends MutableGraph<UndirectedVertex, Edge> {
    //constructors
    public UndirectedGraph() {
        super();
    }

    public UndirectedGraph(int n) {
        super(n);
    }


    //====================================================
    //
    // Graph Override
    //
    //====================================================

    @Override
    public void addVertex(UndirectedVertex v) {
        super.addVertex(v);
    }

    @Override
    public void addEdge(Edge e) throws InvalidEndpointsException {
        Pair<UndirectedVertex> endpoints = e.getEndpoints();
        endpoints.getFirst().addToNeighbors(endpoints.getSecond(), e);
        endpoints.getSecond().addToNeighbors(endpoints.getFirst(), e);
        UndirectedVertex toUpdate = endpoints.getFirst();
        toUpdate.setDegree(toUpdate.getDegree() + 1);
        toUpdate = e.getEndpoints().getSecond();
        toUpdate.setDegree(toUpdate.getDegree() + 1);
        super.addEdge(e);
    }

    @Override
    public void clearEdges() {
        super.clearEdges();
        for (UndirectedVertex v : this.getVertices()) {
            v.setDegree(0);
            v.clearNeighbors();
        }
    }

    @Override
    public void removeEdge(Edge e) throws IllegalArgumentException {
        if (!this.getEdges().contains(e))
            throw new IllegalArgumentException();
        Pair<UndirectedVertex> endpoints = e.getEndpoints();
        endpoints.getFirst().removeFromNeighbors(endpoints.getSecond(), e);
        endpoints.getSecond().removeFromNeighbors(endpoints.getFirst(), e);
        UndirectedVertex toUpdate = endpoints.getFirst();
        toUpdate.setDegree(toUpdate.getDegree() - 1);
        toUpdate = e.getEndpoints().getSecond();
        toUpdate.setDegree(toUpdate.getDegree() - 1);
        super.removeEdge(e);
    }

    @Override
    public List<Edge> findEdges(Pair<UndirectedVertex> endpoints) {
        UndirectedVertex first = endpoints.getFirst();
        HashMap<UndirectedVertex, ArrayList<Edge>> firstNeighbors = first.getNeighbors();
        return firstNeighbors.get(endpoints.getSecond());
    }

    @Override
    public oarlib.core.Graph.Type getType() {
        return Graph.Type.UNDIRECTED;
    }

    @Override
    public UndirectedGraph getDeepCopy() {
        try {

            UndirectedGraph ans = new UndirectedGraph();
            ans.setDepotId(getDepotId());

            TIntObjectHashMap<Edge> indexedEdges = this.getInternalEdgeMap();
            TIntObjectHashMap<UndirectedVertex> indexedVertices = this.getInternalVertexMap();
            UndirectedVertex temp, temp2;
            int n = this.getVertices().size();
            for (int i = 1; i <= n; i++) {
                temp = new UndirectedVertex("deep copy original"); //the new guy
                temp2 = indexedVertices.get(i);
                temp.setCoordinates(temp2.getX(), temp2.getY());
                ans.addVertex(temp, i);
            }
            Edge e, e2;
            TIntArrayList forSorting = new TIntArrayList(indexedEdges.keys());
            forSorting.sort();
            int m = forSorting.size();
            for (int i = 0; i < m; i++) {
                e = indexedEdges.get(forSorting.get(i));
                e2 = new Edge("deep copy original", new Pair<UndirectedVertex>(ans.getInternalVertexMap().get(e.getEndpoints().getFirst().getId()), ans.getInternalVertexMap().get(e.getEndpoints().getSecond().getId())), e.getCost());
                e2.setRequired(e.isRequired());
                e2.setMatchId(e.getId());
                ans.addEdge(e2, e.getId());
            }

            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Edge constructEdge(int i, int j, String desc, int cost)
            throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0)
            throw new InvalidEndpointsException();
        return new Edge(desc, new Pair<UndirectedVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost);
    }

    @Override
    public UndirectedVertex constructVertex(String desc) {
        return new UndirectedVertex(desc);
    }

}
