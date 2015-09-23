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
import oarlib.link.impl.MixedEdge;
import oarlib.vertex.impl.MixedVertex;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


/**
 * Representation of  Mixed Graph; that is, it can use both edges and arcs, in tandem with mixed vertices
 *
 * @author Oliver
 */
public class MixedGraph extends MutableGraph<MixedVertex, MixedEdge> {

    private static Logger LOGGER = Logger.getLogger(MixedGraph.class);

    //constructors
    public MixedGraph() {
        super();
    }

    public MixedGraph(int n) {
        super(n);
    }

    public MixedGraph(int n, int depotId) {
        super(n, depotId);
    }

    //===============================================
    //
    // Adders and Factory methods with isDirected
    //
    //===============================================

    public void addEdge(int i, int j, String desc, int cost, boolean isDirected) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0) {
            LOGGER.error("The endpoint indices passed in do not seem to fall within the valid range of this graph.");
            throw new InvalidEndpointsException();
        }
        this.addEdge(this.constructEdge(i, j, desc, cost, isDirected));
    }

    public MixedEdge constructEdge(int i, int j, String desc, int cost, boolean isDirected)
            throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0) {
            LOGGER.error("The endpoint indices passed in do not seem to fall within the valid range of this graph.");
            throw new InvalidEndpointsException();
        }
        return new MixedEdge(desc, new Pair<MixedVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost, isDirected);

    }

    public MixedEdge constructEdge(int i, int j, String desc, int cost, boolean isDirected, boolean isRequired)
            throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0) {
            LOGGER.error("The endpoint indices passed in do not seem to fall within the valid range of this graph.");
            throw new InvalidEndpointsException();
        }
        MixedEdge ret = new MixedEdge(desc, new Pair<MixedVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost, isDirected);
        ret.setRequired(isRequired);
        return ret;

    }

    public void addEdge(int i, int j, int cost, boolean isDirected, boolean isRequired) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0)
            throw new InvalidEndpointsException();
        MixedEdge temp = this.constructEdge(i, j, "", cost, isDirected, isRequired);
        this.addEdge(temp);
    }

    //===============================================
    //
    // Graph Overrides
    //
    //===============================================

    @Override
    public boolean isWindy() {
        return false;
    }

    @Override
    public void addVertex(MixedVertex v) {
        super.addVertex(v);
    }

    @Override
    public void addEdge(MixedEdge e) throws InvalidEndpointsException {
        //handle the two different cases
        if (!this.getVertices().contains(e.getEndpoints().getFirst()) || !this.getVertices().contains(e.getEndpoints().getSecond())) {
            LOGGER.error("The specified endpoints do not appear to exist in this graph.");
            throw new InvalidEndpointsException();
        }
        if (e.isDirected()) {
            e.getEndpoints().getFirst().addToNeighbors(e.getEndpoints().getSecond(), e);
            MixedVertex toUpdate = e.getEndpoints().getFirst();
            toUpdate.addToIncidentLinks(e);
            toUpdate.setOutDegree(toUpdate.getOutDegree() + 1);
            toUpdate.setDegree(toUpdate.getDegree() + 1);
            toUpdate = e.getEndpoints().getSecond();
            toUpdate.addToIncidentLinks(e);
            toUpdate.setInDegree(toUpdate.getInDegree() + 1);
            toUpdate.setDegree(toUpdate.getDegree() + 1);
            super.addEdge(e);
        } else {
            Pair<MixedVertex> endpoints = e.getEndpoints();
            endpoints.getFirst().addToNeighbors(endpoints.getSecond(), e);
            endpoints.getSecond().addToNeighbors(endpoints.getFirst(), e);
            MixedVertex toUpdate = endpoints.getFirst();
            toUpdate.addToIncidentLinks(e);
            toUpdate.setDegree(toUpdate.getDegree() + 1);
            toUpdate = e.getEndpoints().getSecond();
            toUpdate.addToIncidentLinks(e);
            toUpdate.setDegree(toUpdate.getDegree() + 1);
            super.addEdge(e);
        }
    }

    @Override
    public void clearEdges() {
        super.clearEdges();
        for (MixedVertex v : this.getVertices()) {
            v.setDegree(0);
            v.setInDegree(0);
            v.setOutDegree(0);
            v.clearNeighbors();
        }
    }

    @Override
    public void removeEdge(MixedEdge e) throws IllegalArgumentException {
        if (!this.getEdges().contains(e)) {
            LOGGER.error("This graph does not appear to contain the specified link.");
            throw new IllegalArgumentException();
        }
        try {
            if (e.isDirected()) {
                e.getEndpoints().getFirst().removeFromNeighbors(e.getEndpoints().getSecond(), e);
                MixedVertex toUpdate = e.getEndpoints().getFirst();
                toUpdate.removeFromIncidentLinks(e);
                toUpdate.setOutDegree(toUpdate.getOutDegree() - 1);
                toUpdate = e.getEndpoints().getSecond();
                toUpdate.removeFromIncidentLinks(e);
                toUpdate.setInDegree(toUpdate.getInDegree() - 1);
                super.removeEdge(e);
            } else {
                Pair<MixedVertex> endpoints = e.getEndpoints();
                endpoints.getFirst().removeFromNeighbors(endpoints.getSecond(), e);
                endpoints.getSecond().removeFromNeighbors(endpoints.getFirst(), e);
                MixedVertex toUpdate = endpoints.getFirst();
                toUpdate.removeFromIncidentLinks(e);
                toUpdate.setDegree(toUpdate.getDegree() - 1);
                toUpdate = e.getEndpoints().getSecond();
                toUpdate.removeFromIncidentLinks(e);
                toUpdate.setDegree(toUpdate.getDegree() - 1);
                super.removeEdge(e);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<MixedEdge> findEdges(Pair<MixedVertex> endpoints) {
        List<MixedEdge> ret = new ArrayList<MixedEdge>();
        HashSet<MixedEdge> temp = new HashSet<MixedEdge>(); //to make sure we don't add two copies of an edge
        MixedVertex first = endpoints.getFirst();
        HashMap<MixedVertex, ArrayList<MixedEdge>> firstNeighbors = first.getNeighbors();
        if (firstNeighbors.get(endpoints.getSecond()) != null)
            temp.addAll(firstNeighbors.get(endpoints.getSecond()));
        MixedVertex second = endpoints.getSecond();
        HashMap<MixedVertex, ArrayList<MixedEdge>> secondNeighbors = second.getNeighbors();
        if (secondNeighbors.get(first) != null) {
            for (MixedEdge me : secondNeighbors.get(first))
                if (!me.isDirected())
                    temp.add(me);
        }
        ret.addAll(temp);
        return ret;
    }

    @Override
    public oarlib.core.Graph.Type getType() {
        return Graph.Type.MIXED;
    }

    @Override
    public MixedGraph getDeepCopy() {
        try {
            MixedGraph ans = new MixedGraph();
            TIntObjectHashMap<MixedEdge> indexedEdges = this.getInternalEdgeMap();
            TIntObjectHashMap<MixedVertex> indexedVertices = this.getInternalVertexMap();
            MixedVertex temp, temp2;
            int n = this.getVertices().size();
            int m = this.getEdges().size();
            for (int i = 1; i <= n; i++) {
                temp = new MixedVertex("deep copy original"); //the new guy
                temp2 = indexedVertices.get(i); //the old guy
                if (temp2.isDemandSet())
                    temp.setDemand(temp2.getDemand());
                temp.setCoordinates(temp2.getX(), temp2.getY());
                ans.addVertex(temp, i);
            }
            MixedEdge e, e2;
            TIntArrayList forSorting = new TIntArrayList(indexedEdges.keys());
            forSorting.sort();
            m = forSorting.size();
            for (int i = 0; i < m; i++) {
                e = indexedEdges.get(forSorting.get(i));
                e2 = new MixedEdge("deep copy original", new Pair<MixedVertex>(ans.getInternalVertexMap().get(e.getEndpoints().getFirst().getId()), ans.getInternalVertexMap().get(e.getEndpoints().getSecond().getId())), e.getCost(), e.isDirected());
                e2.setMatchId(e.getId());
                e2.setRequired(e.isRequired());
                ans.addEdge(e2, e.getId());
            }

            ans.setDepotId(getDepotId());
            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public MixedEdge constructEdge(int i, int j, String desc, int cost)
            throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0) {
            LOGGER.error("The endpoint indices passed in do not seem to fall within the valid range of this graph.");
            throw new InvalidEndpointsException();
        }
        return new MixedEdge(desc, new Pair<MixedVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost);

    }

    @Override
    public MixedVertex constructVertex(String desc) {
        return new MixedVertex(desc);
    }
}
