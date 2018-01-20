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
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.MutableGraph;
import oarlib.exceptions.InvalidEndpointsException;
import oarlib.graph.util.Pair;
import oarlib.link.impl.WindyEdge;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WindyGraph extends MutableGraph<WindyVertex, WindyEdge> {

    private static final Logger LOGGER = Logger.getLogger(WindyGraph.class);

    //region Constructors
    public WindyGraph() {
        super();
    }

    public WindyGraph(int n) {
        super(n);
    }

    public WindyGraph(int n, int depotId) {
        super(n, depotId);
    }
    //endregion

    //region Adders and Factory methods that take reverse cost

    public void addEdge(int i, int j, int cost, int reverseCost)
            throws InvalidEndpointsException {
        this.addEdge(i, j, "", cost, reverseCost);
    }

    public void addEdge(int i, int j, String desc, int cost, int reverseCost)
            throws InvalidEndpointsException {
        this.addEdge(this.constructEdge(i, j, desc, cost, reverseCost));
    }

    public void addEdge(int i, int j, String desc, int cost, int reverseCost, int matchId)
            throws InvalidEndpointsException {
        this.addEdge(this.constructEdge(i, j, desc, cost, reverseCost), matchId);
    }

    public void addEdge(int i, int j, int cost, int reverseCost, boolean isRequired)
            throws InvalidEndpointsException {
        this.addEdge(this.constructEdge(i, j, "", cost, reverseCost, isRequired));
    }

    public void addEdge(int i, int j, String desc, int cost, int reverseCost, boolean isRequired)
            throws InvalidEndpointsException {
        this.addEdge(this.constructEdge(i, j, desc, cost, reverseCost, isRequired));
    }

    public void addEdge(int i, int j, String desc, int cost, int reverseCost, int matchId, boolean isRequired)
            throws InvalidEndpointsException {
        this.addEdge(this.constructEdge(i, j, desc, cost, reverseCost, isRequired), matchId);
    }

    public WindyEdge constructEdge(int i, int j, String desc, int cost, int reverseCost)
            throws InvalidEndpointsException {
        if (!getInternalVertexMap().containsKey(i) || !getInternalVertexMap().containsKey(j) || i < 0 || j < 0) {
            LOGGER.error("The endpoint indices passed in do not seem to fall within the valid range of this graph.");
            throw new InvalidEndpointsException();
        }
        return new WindyEdge(desc, new Pair<WindyVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost, reverseCost);

    }

    public WindyEdge constructEdge(int i, int j, String desc, int cost, int reverseCost, boolean isRequired)
            throws InvalidEndpointsException {
        if (!getInternalVertexMap().containsKey(i) || !getInternalVertexMap().containsKey(j) || i < 0 || j < 0) {
            LOGGER.error("The endpoint indices passed in do not seem to fall within the valid range of this graph.");
            throw new InvalidEndpointsException();
        }
        return new WindyEdge(desc, new Pair<WindyVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost, reverseCost, isRequired);

    }
    //endregion

    //region Graph Overrides

    @Override
    public boolean isWindy() {
        return true;
    }

    @Override
    public void addVertex(WindyVertex v) {
        super.addVertex(v);
    }

    @Override
    public List<WindyEdge> findEdges(Pair<WindyVertex> endpoints) {
        WindyVertex first = endpoints.getFirst();
        HashMap<WindyVertex, ArrayList<WindyEdge>> firstNeighbors = first.getNeighbors();
        return firstNeighbors.get(endpoints.getSecond());
    }

    @Override
    public oarlib.core.Graph.Type getType() {
        return Graph.Type.WINDY;
    }

    @Override
    public void addEdge(WindyEdge e) throws InvalidEndpointsException {
        Pair<WindyVertex> endpoints = e.getEndpoints();
        endpoints.getFirst().addToNeighbors(endpoints.getSecond(), e);
        endpoints.getSecond().addToNeighbors(endpoints.getFirst(), e);

        incidenceMap.get(e.getFirstEndpointId()).add(e.getId());
        incidenceMap.get(e.getSecondEndpointId()).add(e.getId());

        WindyVertex toUpdate = endpoints.getFirst();
        toUpdate.setDegree(toUpdate.getDegree() + 1);
        toUpdate = e.getEndpoints().getSecond();
        toUpdate.setDegree(toUpdate.getDegree() + 1);
        super.addEdge(e);
    }

    @Override
    public void clearEdges() {
        super.clearEdges();
        for (WindyVertex v : this.getVertices()) {
            v.setDegree(0);
            v.clearNeighbors();
        }
    }

    @Override
    public void removeEdge(WindyEdge e) throws IllegalArgumentException {
        if (!this.getEdges().contains(e)) {
            LOGGER.error("This graph does not appear to contain the specified link.");
            throw new IllegalArgumentException();
        }
        Pair<WindyVertex> endpoints = e.getEndpoints();
        endpoints.getFirst().removeFromNeighbors(endpoints.getSecond(), e);
        endpoints.getSecond().removeFromNeighbors(endpoints.getFirst(), e);

        incidenceMap.get(e.getFirstEndpointId()).remove(e.getId());
        incidenceMap.get(e.getSecondEndpointId()).remove(e.getId());

        WindyVertex toUpdate = endpoints.getFirst();
        toUpdate.setDegree(toUpdate.getDegree() - 1);
        toUpdate = e.getEndpoints().getSecond();
        toUpdate.setDegree(toUpdate.getDegree() - 1);
        super.removeEdge(e);
    }

    @Override
    public WindyGraph getDeepCopy() {
        try {
            WindyGraph ans = new WindyGraph();
            TIntObjectHashMap<WindyEdge> indexedEdges = this.getInternalEdgeMap();
            TIntObjectHashMap<WindyVertex> indexedVertices = this.getInternalVertexMap();
            WindyVertex temp, temp2;
            int n = this.getVertices().size();

            for (int i = 1; i <= n; i++) {
                temp2 = indexedVertices.get(i);
                temp = new WindyVertex(temp2.getLabel()); //the new guy
                temp.setCoordinates(temp2.getX(), temp2.getY());
                if (temp2.isDemandSet())
                    temp.setDemand(temp2.getDemand());
                ans.addVertex(temp, temp2.getId());
            }
            WindyEdge e, e2;
            TIntArrayList forSortingE = new TIntArrayList(indexedEdges.keys());
            forSortingE.sort();
            int m = forSortingE.size();
            for (int i = 0; i < m; i++) {
                e = indexedEdges.get(forSortingE.get(i));
                e2 = new WindyEdge(e.getLabel(), new Pair<WindyVertex>(ans.getVertex(e.getFirstEndpointId()), ans.getVertex(e.getSecondEndpointId())), e.getCost(), e.getReverseCost());
                e2.setMatchId(e.getId());
                e2.setRequired(e.isRequired());
                e2.setZone(e.getZone());
                e2.setType(e.getType());
                e2.setMaxSpeed(e.getMaxSpeed());
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
    public WindyEdge constructEdge(int i, int j, String desc, int cost)
            throws InvalidEndpointsException {
        if (!getInternalVertexMap().containsKey(i) || !getInternalVertexMap().containsKey(j) || i < 0 || j < 0) {
            LOGGER.error("The endpoint indices passed in do not seem to fall within the valid range of this graph.");
            throw new InvalidEndpointsException();
        }
        return new WindyEdge(desc, new Pair<WindyVertex>(this.getInternalVertexMap().get(i), this.getInternalVertexMap().get(j)), cost, cost);

    }

    @Override
    public WindyVertex constructVertex(String desc) {
        return new WindyVertex(desc);
    }
    //endregion


}
