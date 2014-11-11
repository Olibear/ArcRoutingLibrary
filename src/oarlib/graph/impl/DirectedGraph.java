package oarlib.graph.impl;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import oarlib.link.impl.Arc;
import oarlib.core.Graph;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.util.Pair;
import oarlib.vertex.impl.DirectedVertex;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Reperesentation of a Directed Graph; that is, it can only contain arcs, and directed vertices
 *
 * @param <A> Arc that this graph will use
 * @author Oliver
 */
public class DirectedGraph extends MutableGraph<DirectedVertex, Arc> {

    //constructors
    public DirectedGraph() {
        super();
    }

    public DirectedGraph(int n) {
        super(n);
    }

    public DirectedGraph(int n, int depotId) {
        super(n, depotId);
    }

    //====================================================
    //
    // Graph Overrides
    //
    //====================================================

    @Override
    public void addVertex(DirectedVertex v) {
        super.addVertex(v);
    }

    @Override
    public void clearEdges() {
        super.clearEdges();
        for (DirectedVertex v : this.getVertices()) {
            v.setInDegree(0);
            v.setOutDegree(0);
            v.clearNeighbors();
        }
    }

    @Override
    public void addEdge(Arc e) throws InvalidEndpointsException {
        e.getTail().addToNeighbors(e.getHead(), e);
        DirectedVertex toUpdate = e.getTail();
        toUpdate.setOutDegree(toUpdate.getOutDegree() + 1);
        toUpdate = e.getHead();
        toUpdate.setInDegree(toUpdate.getInDegree() + 1);
        super.addEdge(e);
    }

    @Override
    public void removeEdge(Arc e) throws IllegalArgumentException {
        if (!this.getEdges().contains(e))
            throw new IllegalArgumentException();
        e.getTail().removeFromNeighbors(e.getHead(), e);
        DirectedVertex toUpdate = e.getTail();
        toUpdate.setOutDegree(toUpdate.getOutDegree() - 1);
        toUpdate = e.getHead();
        toUpdate.setInDegree(toUpdate.getInDegree() - 1);
        super.removeEdge(e);
    }

    @Override
    public List<Arc> findEdges(Pair<DirectedVertex> endpoints) {
        List<Arc> ret = new ArrayList<Arc>();
        DirectedVertex first = endpoints.getFirst();
        HashMap<DirectedVertex, ArrayList<Arc>> firstNeighbors = first.getNeighbors();
        if (!firstNeighbors.containsKey(endpoints.getSecond()))
            return new ArrayList<Arc>();
        ret.addAll(firstNeighbors.get(endpoints.getSecond()));
        return ret;
    }

    @Override
    public oarlib.core.Graph.Type getType() {
        return Graph.Type.DIRECTED;
    }

    @Override
    public DirectedGraph getDeepCopy() {
        try {
            DirectedGraph ans = new DirectedGraph();
            ans.setDepotId(getDepotId());
            DirectedVertex temp, temp2;
            Arc a, a2;
            TIntObjectHashMap<DirectedVertex> indexedVertices = this.getInternalVertexMap();
            TIntObjectHashMap<Arc> indexedArcs = this.getInternalEdgeMap();
            int n = this.getVertices().size();
            int m = this.getEdges().size();
            for (int i = 1; i <= n; i++) {
                temp = new DirectedVertex("deep copy original"); //the new guy
                temp2 = indexedVertices.get(i); //the old guy
                if (temp2.isDemandSet())
                    temp.setDemand(temp2.getDemand());
                temp.setCoordinates(temp2.getX(), temp2.getY());
                ans.addVertex(temp, i);
            }
            TIntArrayList forSorting = new TIntArrayList(indexedArcs.keys());
            forSorting.sort();
            m = forSorting.size();

            for (int i = 0; i < m; i++) {
                a = indexedArcs.get(forSorting.get(i));
                a2 = new Arc("deep copy original", new Pair<DirectedVertex>(ans.getInternalVertexMap().get(a.getTail().getId()), ans.getInternalVertexMap().get(a.getHead().getId())), a.getCost());
                if (a.isCapacitySet())
                    a2.setCapacity(a.getCapacity());
                a2.setRequired(a.isRequired());
                a2.setMatchId(a.getId());
                ans.addEdge(a2);
            }
            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Arc constructEdge(int i, int j, String desc, int cost)
            throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0)
            throw new InvalidEndpointsException();
        return new Arc(desc, new Pair<DirectedVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost);

    }

    @Override
    public DirectedVertex constructVertex(String desc) {
        return new DirectedVertex(desc);
    }

}
