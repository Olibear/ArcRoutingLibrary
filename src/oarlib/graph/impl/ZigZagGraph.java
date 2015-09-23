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
import oarlib.link.impl.ZigZagLink;
import oarlib.vertex.impl.ZigZagVertex;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A class meant to hold data for an instance of the Windy Rural Postman Problem with Zig-Zagging,
 * this is essentially a windy graph that has some zig-zaggable links, (although the problem calls for
 * mandatory zig-zags, and mandatory two-way traversals, these can be modelled in the context of a
 * windy graph).
 * <p/>
 * Created by oliverlum on 3/22/15.
 */
public class ZigZagGraph extends MutableGraph<ZigZagVertex, ZigZagLink> {

    private static Logger LOGGER = Logger.getLogger(UndirectedGraph.class);

    public ZigZagGraph() {
        super();
    }

    public ZigZagGraph(int n) {
        super(n);
    }

    @Override
    public ZigZagLink constructEdge(int i, int j, String desc, int cost) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0) {
            LOGGER.error("The endpoint indices passed in do not seem to fall within the valid range of this graph.");
            throw new InvalidEndpointsException();
        }
        //with some defaults
        return new ZigZagLink(desc, new Pair<ZigZagVertex>(getVertex(i), getVertex(j)), cost, cost, 3 * cost, 10, 10, ZigZagLink.ZigZagStatus.OPTIONAL);
    }

    //for more specificity
    public ZigZagLink constructEdge(int i, int j, String desc, int cost, int reverseCost, double zigzagcost, int serviceCost, int reverseServiceCost, ZigZagLink.ZigZagStatus status) throws InvalidEndpointsException {
        if (i > this.getVertices().size() || j > this.getVertices().size() || i < 0 || j < 0) {
            LOGGER.error("The endpoint indices passed in do not seem to fall within the valid range of this graph.");
            throw new InvalidEndpointsException();
        }
        //with some defaults
        return new ZigZagLink(desc, new Pair<ZigZagVertex>(getVertex(i), getVertex(j)), cost, reverseCost, zigzagcost, serviceCost, reverseServiceCost, status);
    }

    @Override
    public ZigZagVertex constructVertex(String desc) {
        return new ZigZagVertex(desc);
    }


    //====================================================
    //
    // Graph Override
    //
    //====================================================

    @Override
    public boolean isWindy() {
        return true;
    }

    @Override
    public void addVertex(ZigZagVertex v) {
        super.addVertex(v);
    }

    @Override
    public List<ZigZagLink> findEdges(Pair<ZigZagVertex> endpoints) {
        ZigZagVertex first = endpoints.getFirst();
        HashMap<ZigZagVertex, ArrayList<ZigZagLink>> firstNeighbors = first.getNeighbors();
        return firstNeighbors.get(endpoints.getSecond());
    }

    @Override
    public oarlib.core.Graph.Type getType() {
        return Graph.Type.WINDY;
    }

    @Override
    public void addEdge(ZigZagLink e) throws InvalidEndpointsException {
        Pair<ZigZagVertex> endpoints = e.getEndpoints();
        endpoints.getFirst().addToNeighbors(endpoints.getSecond(), e);
        endpoints.getSecond().addToNeighbors(endpoints.getFirst(), e);
        ZigZagVertex toUpdate = endpoints.getFirst();
        toUpdate.addToIncidentLinks(e);
        toUpdate.setDegree(toUpdate.getDegree() + 1);
        toUpdate = e.getEndpoints().getSecond();
        toUpdate.addToIncidentLinks(e);
        toUpdate.setDegree(toUpdate.getDegree() + 1);
        super.addEdge(e);
    }

    @Override
    public void clearEdges() {
        super.clearEdges();
        for (ZigZagVertex v : this.getVertices()) {
            v.setDegree(0);
            v.clearNeighbors();
        }
    }

    @Override
    public void removeEdge(ZigZagLink e) throws IllegalArgumentException {
        if (!this.getEdges().contains(e)) {
            LOGGER.error("This graph does not appear to contain the specified link.");
            throw new IllegalArgumentException();
        }
        Pair<ZigZagVertex> endpoints = e.getEndpoints();
        endpoints.getFirst().removeFromNeighbors(endpoints.getSecond(), e);
        endpoints.getSecond().removeFromNeighbors(endpoints.getFirst(), e);
        ZigZagVertex toUpdate = endpoints.getFirst();
        toUpdate.removeFromIncidentLinks(e);
        toUpdate.setDegree(toUpdate.getDegree() - 1);
        toUpdate = e.getEndpoints().getSecond();
        toUpdate.removeFromIncidentLinks(e);
        toUpdate.setDegree(toUpdate.getDegree() - 1);
        super.removeEdge(e);
    }

    @Override
    public ZigZagGraph getDeepCopy() {
        try {
            ZigZagGraph ans = new ZigZagGraph();
            TIntObjectHashMap<ZigZagLink> indexedEdges = this.getInternalEdgeMap();
            TIntObjectHashMap<ZigZagVertex> indexedVertices = this.getInternalVertexMap();
            ZigZagVertex temp, temp2;
            int n = this.getVertices().size();
            for (int i = 1; i <= n; i++) {
                temp = new ZigZagVertex("deep copy original"); //the new guy
                temp2 = indexedVertices.get(i);
                temp.setCoordinates(temp2.getX(), temp2.getY());
                ans.addVertex(temp, i);
            }
            ZigZagLink e, e2;
            TIntArrayList forSorting = new TIntArrayList(indexedEdges.keys());
            forSorting.sort();
            int m = forSorting.size();
            for (int i = 0; i < m; i++) {
                e = indexedEdges.get(forSorting.get(i));
                e2 = new ZigZagLink("deep copy original", new Pair<ZigZagVertex>(ans.getInternalVertexMap().get(e.getEndpoints().getFirst().getId()), ans.getInternalVertexMap().get(e.getEndpoints().getSecond().getId())), e.getCost(), e.getReverseCost(), e.getZigzagCost(), e.getServiceCost(), e.getReverseServiceCost(), e.getStatus());
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


}
