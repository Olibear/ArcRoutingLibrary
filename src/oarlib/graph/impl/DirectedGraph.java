/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2015 Oliver Lum
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package oarlib.graph.impl;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.MutableGraph;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.util.Pair;
import oarlib.link.impl.Arc;
import oarlib.vertex.impl.DirectedVertex;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Reperesentation of a Directed Graph; that is, it can only contain arcs, and directed vertices
 *
 * @author Oliver
 */
public class DirectedGraph extends MutableGraph<DirectedVertex, Arc> {

    private static final Logger LOGGER = Logger.getLogger(DirectedGraph.class);

    //region Constructors
    public DirectedGraph() {
        super();
    }

    public DirectedGraph(int n) {
        super(n);
    }

    public DirectedGraph(int n, int depotId) {
        super(n, depotId);
    }
    //endregion

    //region Graph Overrides

    @Override
    public boolean isWindy() {
        return false;
    }

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
        toUpdate.addToIncidentLinks(e);
        toUpdate.setOutDegree(toUpdate.getOutDegree() + 1);
        toUpdate = e.getHead();
        toUpdate.addToIncidentLinks(e);
        toUpdate.setInDegree(toUpdate.getInDegree() + 1);
        super.addEdge(e);
    }

    @Override
    public void removeEdge(Arc e) throws IllegalArgumentException {
        if (!this.getEdges().contains(e)) {
            LOGGER.error("This graph does not appear to contain the specified arc.");
            throw new IllegalArgumentException();
        }
        e.getTail().removeFromNeighbors(e.getHead(), e);
        DirectedVertex toUpdate = e.getTail();
        toUpdate.removeFromIncidentLinks(e);
        toUpdate.setOutDegree(toUpdate.getOutDegree() - 1);
        toUpdate = e.getHead();
        toUpdate.removeFromIncidentLinks(e);
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
            ans.setDepotId(getDepotId());
            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public Arc constructEdge(int i, int j, String desc, int cost)
            throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0) {
            LOGGER.error("The endpoint indices passed in do not seem to fall within the valid range of this graph.");
            throw new InvalidEndpointsException();
        }
        return new Arc(desc, new Pair<DirectedVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost);

    }

    @Override
    public DirectedVertex constructVertex(String desc) {
        return new DirectedVertex(desc);
    }

    //endregion

}
