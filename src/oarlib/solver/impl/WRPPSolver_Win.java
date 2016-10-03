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
package oarlib.solver.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.SingleVehicleSolver;
import oarlib.core.Solver;
import oarlib.exceptions.NegativeCycleException;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.graph.util.Utils;
import oarlib.link.impl.Arc;
import oarlib.link.impl.Edge;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.UndirectedVertex;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.util.*;

public class WRPPSolver_Win extends SingleVehicleSolver<WindyVertex, WindyEdge, WindyGraph> {

    private static final Logger LOGGER = Logger.getLogger(WRPPSolver_Win.class);

    public WRPPSolver_Win(Problem<WindyVertex, WindyEdge, WindyGraph> instance) throws IllegalArgumentException {
        super(instance);
    }

    /**
     * Carries out the connection procedure contained in Benavent's paper, solving an MST problem on the conncted components
     * induced by the required edges of g.
     *
     * @param g - the original windy graph representing the WRP Problem.
     * @return - the connected 'required' graph on which we solve the WPP
     */
    public static WindyGraph connectRequiredComponents(WindyGraph g) {
        try {
            int n = g.getVertices().size(); //num vertices
            int m = g.getEdges().size(); // num edges


			/*
             * The connected graph we're going to return, it will have only the required edges
			 * plus the edges that are added as a result of solving the MST problem.
			 */
            WindyGraph windyReq = new WindyGraph();

            //has the same number of vertices as g
            TIntObjectHashMap<WindyVertex> gVertices = g.getInternalVertexMap();
            WindyVertex tempVertex;
            for (int i = 1; i <= n; i++) {
                tempVertex = new WindyVertex("original");
                tempVertex.setCoordinates(gVertices.get(i).getX(), gVertices.get(i).getY());
                windyReq.addVertex(tempVertex);
            }

            //edges from the original graph
            TIntObjectHashMap<WindyEdge> indexedWindyEdges = g.getInternalEdgeMap();

            WindyEdge temp;
            int mreq = 0; //num required edges

            //lists for setting up the connected components problem
            ArrayList<Integer> edge1 = new ArrayList<Integer>();
            ArrayList<Integer> edge2 = new ArrayList<Integer>();
            edge1.add(null); //so indices match
            edge2.add(null);

			/*
             *  Cycle through the original edges, and add them to windyReq if they're required.
			 *  Also, add to our list of edges to solve the connected components graph
			 */
            for (int i = 1; i <= m; i++) {
                temp = indexedWindyEdges.get(i);
                if (temp.isRequired()) {
                    mreq++;
                    edge1.add(temp.getEndpoints().getFirst().getId());
                    edge2.add(temp.getEndpoints().getSecond().getId());
                    windyReq.addEdge(temp.getEndpoints().getFirst().getId(), temp.getEndpoints().getSecond().getId(), "original", temp.getCost(), temp.getReverseCost(), i, true);
                }
            }

            //now figure out the connected components
            int[] component = new int[n + 1];
            int[] nodei = new int[mreq + 1];
            int[] nodej = new int[mreq + 1];
            Integer[] e1 = edge1.toArray(new Integer[edge1.size()]);
            Integer[] e2 = edge2.toArray(new Integer[edge2.size()]);

            for (int i = 1; i <= mreq; i++) {
                nodei[i] = e1[i];
                nodej[i] = e2[i];
            }

            CommonAlgorithms.connectedComponents(n, mreq, nodei, nodej, component);

            if (component[0] == 1)
                return windyReq;

            /**
             * We need to keep track of vertices in the graph that have no incident required edges.
             * They will show up as separate components, but we have no obligation to connect them
             * in this phase of the heuristic.
             */
            HashSet<Integer> extraComponents = new HashSet<Integer>();
            HashSet<Integer> extraVertices = new HashSet<Integer>();
            for (WindyVertex wv : windyReq.getVertices()) {
                if (wv.getDegree() == 0) {
                    extraComponents.add(component[wv.getId()]);
                    extraVertices.add(wv.getId());
                }
            }

            //now find shortest paths in the original graph to set up the MST graph
            int[] dist = new int[n + 1];
            int[] path = new int[n + 1];
            int[] edgePath = new int[n + 1];

            //now create a complete collapsed graph over which we shall solve an MST problem
            UndirectedGraph mstGraph = new UndirectedGraph();
            int mstN = component[0] - extraComponents.size(); //we want 1 vertex for each connected component (subtract off the unconnected guys)
            if (mstN <= 1)
                return windyReq;
            for (int i = 1; i <= mstN; i++) {
                mstGraph.addVertex(new UndirectedVertex("original"));
            }


			/*
             * We need to figure out which of the components is real, and don't correspond to these
			 * unconnected vertices.  To do so, we set up this list that only holds legitimate indices.
			 */
            ArrayList<Integer> realComponents = new ArrayList<Integer>();
            realComponents.add(0);
            for (int i = 1; i <= component[0]; i++) {
                if (!extraComponents.contains(i))
                    realComponents.add(i);
            }

            int comp1, comp2;
            Double averagePathCost1;
            HashMap<Pair<Integer>, Integer> minCostPathVal = new HashMap<Pair<Integer>, Integer>(); //key is components being connected, value is best cost btw them.
            HashMap<Pair<Integer>, Pair<Integer>> minCostPathNodes = new HashMap<Pair<Integer>, Pair<Integer>>();
            Pair<Integer> tempKey;
            //figure out the min cost path from each component to each component
            for (int i = 1; i <= n; i++) {
                //don't add if it's a bogus component
                if (extraVertices.contains(i))
                    continue;
                CommonAlgorithms.dijkstrasAlgorithm(g, i, dist, path, edgePath);
                comp1 = realComponents.indexOf(component[i]);
                for (int j = 1; j <= n; j++) {
                    //don't add if it's a bogus component
                    if (extraVertices.contains(j))
                        continue;
                    comp2 = realComponents.indexOf(component[j]);

                    //don't care about internal distances
                    if (comp1 == comp2)
                        continue;

                    //the average path cost in the original graph from i to j
                    averagePathCost1 = calculateAveragePathCost(g, i, j, path, edgePath);
                    if (comp1 < comp2)
                        tempKey = new Pair<Integer>(comp1, comp2);
                    else
                        tempKey = new Pair<Integer>(comp2, comp1);

                    //If we found a shorter path, record it.
                    if (!minCostPathVal.containsKey(tempKey) || ((int) (2 * averagePathCost1) < minCostPathVal.get(tempKey))) {
                        minCostPathVal.put(tempKey, (int) (2 * averagePathCost1));
                        minCostPathNodes.put(tempKey, new Pair<Integer>(i, j));
                    }
                }
            }

            //now set up the mst graph
            for (Pair<Integer> key : minCostPathVal.keySet()) {
                mstGraph.addEdge(key.getFirst(), key.getSecond(), "MST Graph", minCostPathVal.get(key));
            }

            //calculate the min-cost spanning tree
            int[] mst = CommonAlgorithms.randomizedLowCostSpanningTree(mstGraph, 5);

            //now add back the mst paths to the windy graph
            int limi = mst.length;
            Edge selected;
            WindyEdge toAdd;
            TIntObjectHashMap<Edge> mstEdges = mstGraph.getInternalEdgeMap();
            Pair<Integer> pathToAdd;
            int curr, next, end;
            for (int i = 1; i < limi; i++) {
                if (mst[i] == 1) {
                    selected = mstEdges.get(i);
                    comp1 = selected.getEndpoints().getFirst().getId();
                    comp2 = selected.getEndpoints().getSecond().getId();

                    tempKey = new Pair<Integer>(comp1, comp2);
                    pathToAdd = minCostPathNodes.get(tempKey);
                    //now add to windy copy the new 'required' edges
                    curr = pathToAdd.getFirst();
                    end = pathToAdd.getSecond();

                    CommonAlgorithms.dijkstrasAlgorithm(g, curr, dist, path, edgePath);

                    do {
                        next = path[end];
                        toAdd = indexedWindyEdges.get(edgePath[end]);
                        windyReq.addEdge(toAdd.getEndpoints().getFirst().getId(), toAdd.getEndpoints().getSecond().getId(), "mst added", toAdd.getCost(), toAdd.getReverseCost(), edgePath[end], toAdd.isRequired());
                    } while ((end = next) != curr);
                }
            }

            return windyReq;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method to remove any cycles of non-required edges, or added edges
     *
     * @param ans - the directed graph representing that optimal windy tour.
     */
    public static void eliminateRedundantCycles(DirectedGraph ans, WindyGraph windyReq, WindyGraph orig) {
        try {

            boolean hasSelfDummyArc = false;
            for (Arc a : ans.getEdges())
                if (a.isRequired() && a.getEndpoints().getFirst().getId() == a.getEndpoints().getSecond().getId())
                    hasSelfDummyArc = true;

            int debug = 0;
            for (Arc a : ans.getEdges())
                if (a.isRequired())
                    debug++;

            /*
             * Improvement Procedure 1
			 * Look for two-vertex cycles, and eliminate them.
			 */
            TIntObjectHashMap<DirectedVertex> ansVertices = ans.getInternalVertexMap();
            int n = ans.getVertices().size();
            DirectedVertex v;
            HashMap<DirectedVertex, ArrayList<Arc>> vNeighbors, dvNeighbors;
            ArrayList<Arc> vdvConnections, dvvConnections;
            int xij, xji, toRemove;
            ArrayList<DirectedVertex> vNeighborsList;
            for (int i = 1; i <= n; i++) {
                v = ansVertices.get(i);
                vNeighbors = v.getNeighbors();

                vNeighborsList = new ArrayList<DirectedVertex>();
                for (DirectedVertex dv : vNeighbors.keySet())
                    vNeighborsList.add(dv);

                int indexToRemove;
                boolean removedReqVDV, removedReqDVV;
                for (DirectedVertex dv : vNeighborsList) {
                    dvNeighbors = dv.getNeighbors();
                    if (v.getId() < dv.getId() && dvNeighbors.containsKey(v)) //to avoid redundant checking
                    {
                        vdvConnections = vNeighbors.get(dv);
                        xij = vdvConnections.size(); //num connections from v to dv
                        dvvConnections = dvNeighbors.get(v);
                        xji = dvvConnections.size(); //num connections from dv to v
                        if ((xij + xji) >= 3) //we might be able to remove some.
                        {
                            if (xij == xji)
                                toRemove = xij - 1;
                            else
                                toRemove = Math.min(xij, xji);

                            //remove toRemove arcs in each direction
                            removedReqVDV = false;
                            removedReqDVV = false;
                            for (int j = 0; j < toRemove; j++) {
                                if (vdvConnections.get(0).isRequired()) {
                                    removedReqVDV = true;
                                } else if (dvvConnections.get(0).isRequired()) {
                                    removedReqDVV = true;
                                }
                                ans.removeEdge(vdvConnections.get(0)); // I think this should work, so long as this vdvConnections and dvvConnections are passed by reference
                                ans.removeEdge(dvvConnections.get(0));
                            }

                            //make sure if we removed any required guys, that we set one of the remaining dups to the
                            if (removedReqVDV) {
                                if (vdvConnections.size() != 0)
                                    vdvConnections.get(0).setRequired(true);
                            } else if (removedReqDVV) {
                                if (dvvConnections.size() != 0)
                                    dvvConnections.get(0).setRequired(true);
                            }
                        }
                    }
                }
            }

			/*
             * Improvement Procedure 2
			 * Looks for cycles that consist of arcs which we can get rid of,
			 * (i.e. case a makes sure we don't get rid of our only traversal
			 * of a required arc)
			 */
            TIntObjectHashMap<WindyEdge> windyReqEdges = windyReq.getInternalEdgeMap();
            DirectedGraph flowGraph = new DirectedGraph();
            for (int i = 1; i <= n; i++) {
                flowGraph.addVertex(new DirectedVertex("flow"));
            }
            Arc temp;
            WindyEdge windyReqTemp;
            int tempCost;
            boolean isReq;
            for (int i = 1; i <= n; i++) {
                v = ansVertices.get(i);
                vNeighbors = v.getNeighbors();
                for (DirectedVertex dv : vNeighbors.keySet()) {
                    vdvConnections = vNeighbors.get(dv);
                    isReq = vdvConnections.get(0).isRequired();
                    xij = vdvConnections.size();
                    tempCost = vdvConnections.get(0).getCost();

                    if (xij >= 3 && isReq) //a
                    {
                        temp = flowGraph.constructEdge(dv.getId(), v.getId(), "a", -2 * tempCost);
                        temp.setCapacity((int) Math.floor((xij - 1) / 2.0));
                        flowGraph.addEdge(temp);
                    } else if (xij >= 2 && !isReq) //b
                    {
                        temp = flowGraph.constructEdge(dv.getId(), v.getId(), "b", -2 * tempCost);
                        temp.setCapacity((int) Math.floor((xij) / 2.0));
                        flowGraph.addEdge(temp);
                    } else if (xij > 0) //c
                    {
                        windyReqTemp = windyReqEdges.get(vdvConnections.get(0).getMatchId()); // the corresponding guy in windyReq
                        if (windyReqTemp.getCost() == tempCost) {
                            tempCost = windyReqTemp.getReverseCost() - tempCost;
                        } else
                            tempCost = windyReqTemp.getCost() - tempCost;

                        temp = flowGraph.constructEdge(dv.getId(), v.getId(), "c", tempCost);
                        temp.setMatchId(vdvConnections.get(0).getMatchId());
                        temp.setCapacity(xij);
                        flowGraph.addEdge(temp);
                    }

                }
            }

            int flowanswer[] = solvePseudoMinCostFlow(flowGraph);

            int curr, end, next, cost;
            TIntObjectHashMap<Arc> flowArcs = flowGraph.getInternalEdgeMap();
            ArrayList<Arc> removeCandidates;
            Arc changeDir;
            WindyEdge replaceDir;
            DirectedVertex u1, u2;

            int indexToRemove;
            for (int i = 1; i < flowanswer.length; i++) {
                temp = flowArcs.get(i);
                u1 = ansVertices.get(temp.getTail().getId());
                u2 = ansVertices.get(temp.getHead().getId());

                if (temp.getLabel().equals("a") || temp.getLabel().equals("b")) {
                    //remove two copies of arc ji from ans
                    removeCandidates = u2.getNeighbors().get(u1);
                    for (int j = 0; j < flowanswer[i]; j++) {
                        indexToRemove = 0;
                        while (removeCandidates.get(indexToRemove).isRequired())
                            indexToRemove++;
                        ans.removeEdge(removeCandidates.get(indexToRemove));
                        while (removeCandidates.get(indexToRemove).isRequired())
                            indexToRemove++;
                        ans.removeEdge(removeCandidates.get(indexToRemove));
                    }
                } else if (flowanswer[i] > 0)//c
                {
                    //change the orientation of arc ji
                    removeCandidates = u2.getNeighbors().get(u1);
                    changeDir = removeCandidates.get(0);
                    replaceDir = windyReqEdges.get(changeDir.getMatchId());
                    if (changeDir.getCost() == replaceDir.getCost())
                        cost = replaceDir.getReverseCost();
                    else
                        cost = replaceDir.getCost();

                    if (changeDir.getSecondEndpointId() == 2 && changeDir.getFirstEndpointId() == 8)
                        System.out.println("DEBUG");
                    ans.addEdge(changeDir.getHead().getId(), changeDir.getTail().getId(), "swapDir", cost, replaceDir.isRequired());
                    ans.removeEdge(removeCandidates.get(0));
                    for (int j = 1; j < flowanswer[i]; j++) {
                        ans.addEdge(changeDir.getHead().getId(), changeDir.getTail().getId(), "swapDir", cost, false);
                        ans.removeEdge(removeCandidates.get(0));
                    }
                }
            }

            //if we deleted the depot self arc, then add it back now.
            boolean dummyAdded = false;
            int ansDepotId = ans.getDepotId();
            DirectedVertex depot = ans.getInternalVertexMap().get(ansDepotId);
            if (depot.getNeighbors().keySet().isEmpty()) {
                ans.addEdge(ansDepotId, ansDepotId, 2, true);
                dummyAdded = true;
            }

            //now reconnect with MST
            int[] components = CommonAlgorithms.stronglyConnectedComponents(ans);
            int extraVertices = 0;
            for (DirectedVertex dv : ans.getVertices()) {
                if (dv.getInDegree() == 0 && dv.getOutDegree() == 0)
                    extraVertices++;
            }
            if (components[0] - extraVertices != 1) {
                //create a windy graph out of the ans graph
                //each arc gets an edge; overdoing it, but should be okay
                //then add windy edges from the original as not required
                //call connectRequiredComponents
                //then check for non-required edges, and add accordingly
                WindyGraph connectMe = new WindyGraph();
                for (int i = 1; i <= n; i++) {
                    connectMe.addVertex(new WindyVertex("reconnection step"));
                }
                for (Arc a : ans.getEdges()) {
                    connectMe.addEdge(a.getTail().getId(), a.getHead().getId(), "reconnection step", 100 * a.getCost(), 100 * a.getCost(), true);
                }
                for (WindyEdge we : orig.getEdges()) {
                    if (!we.isRequired())
                        connectMe.addEdge(we.getEndpoints().getFirst().getId(), we.getEndpoints().getSecond().getId(), "connection step", we.getCost(), we.getReverseCost(), we.isRequired());
                }
                WindyGraph windyReq2 = connectRequiredComponents(connectMe);
                for (WindyEdge we : windyReq2.getEdges()) {
                    if (!we.isRequired()) {
                        ans.addEdge(we.getEndpoints().getFirst().getId(), we.getEndpoints().getSecond().getId(), "forward", we.getCost(), false);
                        ans.addEdge(we.getEndpoints().getSecond().getId(), we.getEndpoints().getFirst().getId(), "backward", we.getReverseCost(), false);
                    }
                }
            }

            //Improvement Procedure 3
            //compute an euler tour, and then replace non-req paths with shortest paths from the full graph
            ArrayList<Integer> tour = CommonAlgorithms.tryHierholzer(ans);

            TIntObjectHashMap<Arc> ansArcs = ans.getInternalEdgeMap();
            TIntObjectHashMap<WindyEdge> origEdges = orig.getInternalEdgeMap();
            int m = ans.getEdges().size();
            int startId = -1;
            int endId = -1;
            boolean midPath = false;
            int nextEdge;
            WindyEdge origTemp;
            int[] dist = new int[n + 1];
            int[] path = new int[n + 1];
            int[] edgePath = new int[n + 1];

            for (int i = 1; i <= m; i++) {
                temp = ansArcs.get(tour.get(i - 1));
                if (!temp.isRequired() && startId < 0) //start non-req path
                {
                    startId = temp.getTail().getId();
                    midPath = true;

                    CommonAlgorithms.dijkstrasAlgorithm(orig, startId, dist, path, edgePath);

                    //cleanup
                    if (i == m) {
                        endId = temp.getHead().getId();
                        //add the shortest path from startId to endId
                        curr = startId;
                        end = endId;
                        if (curr != end) {
                            do {
                                next = path[end];
                                nextEdge = edgePath[end];
                                origTemp = origEdges.get(nextEdge);
                                if (origTemp.getEndpoints().getFirst().getId() != next)
                                    cost = origTemp.getReverseCost();
                                else
                                    cost = origTemp.getCost();
                                ans.addEdge(next, end, "final", cost, false);

                            } while ((end = next) != curr);
                        }
                        midPath = false;
                    }

                    //remove the edge
                    ans.removeEdge(temp);
                } else if (temp.isRequired() && startId > 0 && endId < 0) //end non-req path
                {
                    endId = temp.getTail().getId();
                    //add the shortest path from startId to endId
                    curr = startId;
                    end = endId;
                    if (curr != end) {
                        do {
                            next = path[end];
                            nextEdge = edgePath[end];
                            origTemp = origEdges.get(nextEdge);
                            if (origTemp.getEndpoints().getFirst().getId() != next)
                                cost = origTemp.getReverseCost();
                            else
                                cost = origTemp.getCost();
                            ans.addEdge(next, end, "final", cost, false);

                        } while ((end = next) != curr);
                    }

                    //reset
                    startId = -1;
                    endId = -1;
                    midPath = false;
                } else if (midPath) {
                    //cleanup
                    if (i == m) {
                        endId = temp.getHead().getId();
                        //add the shortest path from startId to endId
                        curr = startId;
                        end = endId;
                        if (curr != end) {
                            do {
                                next = path[end];
                                nextEdge = edgePath[end];
                                origTemp = origEdges.get(nextEdge);
                                if (origTemp.getEndpoints().getFirst().getId() != next)
                                    cost = origTemp.getReverseCost();
                                else
                                    cost = origTemp.getCost();
                                ans.addEdge(next, end, "final", cost, false);

                            } while ((end = next) != curr);
                        }
                        midPath = false;
                    }

                    //remove the edge
                    ans.removeEdge(temp);
                }
            }

            if (!hasSelfDummyArc && dummyAdded) {
                depot = ans.getVertex(ans.getDepotId());
                Arc dummyArc = depot.getNeighbors().get(depot).get(0);
                ans.removeEdge(dummyArc);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DirectedGraph constructOptimalWindyTour(WindyGraph g) throws IllegalArgumentException {
        if (!CommonAlgorithms.isEulerian(g))
            throw new IllegalArgumentException();
        try {
            //construct the optimal tour on the Eulerian Windy Graph.
            int n = g.getVertices().size();
            int m = g.getEdges().size();

            //construct the digraph for the min-cost flow solution
            DirectedGraph flowGraph = new DirectedGraph(n);
            flowGraph.setDepotId(g.getDepotId());

            TIntObjectHashMap<DirectedVertex> flowVertices = flowGraph.getInternalVertexMap();
            TIntObjectHashMap<WindyEdge> windyEdges = g.getInternalEdgeMap();
            WindyEdge e;
            Arc temp;
            int artID = 0;
            int tempCost;
            for (int i = 1; i <= m; i++) {
                e = windyEdges.get(i);
                tempCost = Math.abs(e.getCost() - e.getReverseCost());
                //add an artificial one in the greater cost direction
                if (e.getCost() > e.getReverseCost()) {
                    temp = new Arc("orig", new Pair<DirectedVertex>(flowVertices.get(e.getEndpoints().getFirst().getId()), flowVertices.get(e.getEndpoints().getSecond().getId())), tempCost);
                    temp.setCapacity(2);
                    temp.setRequired(e.isRequired());
                    temp.setMatchId(e.getId());
                    flowGraph.addEdge(temp);
                    artID = temp.getId();
                } else {
                    temp = new Arc("orig", new Pair<DirectedVertex>(flowVertices.get(e.getEndpoints().getSecond().getId()), flowVertices.get(e.getEndpoints().getFirst().getId())), tempCost);
                    temp.setCapacity(2);
                    temp.setRequired(e.isRequired());
                    temp.setMatchId(e.getId());
                    flowGraph.addEdge(temp);
                    artID = temp.getId();
                }
                //add one in each direction
                flowGraph.addEdge(new Arc("orig", new Pair<DirectedVertex>(flowVertices.get(e.getEndpoints().getFirst().getId()), flowVertices.get(e.getEndpoints().getSecond().getId())), 2 * e.getCost()), artID);
                if (e.getFirstEndpointId() != e.getSecondEndpointId()) //to deal with self-arcs
                    flowGraph.addEdge(new Arc("orig", new Pair<DirectedVertex>(flowVertices.get(e.getEndpoints().getSecond().getId()), flowVertices.get(e.getEndpoints().getFirst().getId())), 2 * e.getReverseCost()), artID);

            }

            if (CommonAlgorithms.isEulerian(flowGraph)) {
                DirectedGraph ans = new DirectedGraph(n);

                for (int i = 1; i <= m; i++) {
                    e = windyEdges.get(i);
                    //add an arc in the least cost direction
                    if (e.getCost() > e.getReverseCost()) {
                        ans.addEdge(e.getEndpoints().getSecond().getId(), e.getEndpoints().getFirst().getId(), "ans", e.getReverseCost(), e.getId(), e.isRequired());
                    } else {
                        ans.addEdge(e.getEndpoints().getFirst().getId(), e.getEndpoints().getSecond().getId(), "ans", e.getCost(), e.getId(), e.isRequired());
                    }
                }
                return ans;
            }
            for (DirectedVertex v : flowGraph.getVertices()) {
                if (v.getDelta() != 0)
                    v.setDemand(-1 * v.getDelta()); //y negative?
            }
            int[] flowanswer = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(flowGraph);

            //now parse the result
            TIntObjectHashMap<Arc> flowEdges = flowGraph.getInternalEdgeMap();
            TIntObjectHashMap<WindyVertex> gVertices = g.getInternalVertexMap();
            Arc artificial;
            DirectedGraph ans = new DirectedGraph();
            DirectedVertex tempVertex;
            for (int i = 1; i <= n; i++) {
                tempVertex = new DirectedVertex("ans");
                tempVertex.setCoordinates(gVertices.get(i).getX(), gVertices.get(i).getY());
                ans.addVertex(tempVertex);
            }

            for (int i = 1; i < flowanswer.length; i++) {
                temp = flowEdges.get(i);
                if (temp.isCapacitySet()) //this is an artificial edge, ignore it
                    continue;
                //look at the relevant artificial arc's flow, and determine which direction to go
                artificial = flowEdges.get(temp.getMatchId());
                if (artificial.getHead().getId() == temp.getHead().getId() && flowanswer[temp.getMatchId()] == 2) // artificial and temp in same direction
                {
                    ans.addEdge(temp.getTail().getId(), temp.getHead().getId(), "ans", temp.getCost() / 2, artificial.getMatchId(), artificial.isRequired());
                    for (int j = 1; j <= flowanswer[i]; j++) {
                        ans.addEdge(temp.getTail().getId(), temp.getHead().getId(), "ans", temp.getCost() / 2, artificial.getMatchId(), false);
                    }
                } else if (artificial.getHead().getId() == temp.getTail().getId() && flowanswer[temp.getMatchId()] == 0) {
                    ans.addEdge(temp.getTail().getId(), temp.getHead().getId(), "ans", temp.getCost() / 2, artificial.getMatchId(), artificial.isRequired());
                    for (int j = 1; j <= flowanswer[i]; j++) {
                        ans.addEdge(temp.getTail().getId(), temp.getHead().getId(), "ans", temp.getCost() / 2, artificial.getMatchId(), false);
                    }
                }
            }

            //should be done now
            if (!CommonAlgorithms.isEulerian(ans)) {
                LOGGER.error("The flow augmentation failed."); //should never happen
                CommonAlgorithms.isEulerian(ans);
            }

            //compute cost
            HashSet<Arc> arcSet = ans.getEdges();
            int cost = 0;
            for (Arc a : arcSet) {
                cost += a.getCost();
            }
            LOGGER.debug("Cost is: " + cost);

            return ans;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Solves the min-cost matching problem over a complete graph
     * consisting of corresponding vertices for each odd vertex in the windyReq graph
     *
     * @param fullGraph - the whole graph so we can solve a shortest path in it.
     * @param g         - the required graph generated by connectRequiredComponents
     */
    public static void eulerAugment(WindyGraph fullGraph, WindyGraph g) {

		/*
		 * fullGraph is copy, and g is windyReq
		 */
        try {
            int n = fullGraph.getVertices().size(); //num vertices

            //solve shortest paths in fullGraph
            int[] dist = new int[n + 1];
            int[] path = new int[n + 1];
            int[] edgePath = new int[n + 1];

            //setup the complete graph composed entirely of the unbalanced vertices
            UndirectedGraph matchingGraph = new UndirectedGraph();

            //setup our graph of unbalanced vertices
            for (WindyVertex v : g.getVertices()) {
                if (v.getDegree() % 2 == 1) {
                    matchingGraph.addVertex(new UndirectedVertex("oddVertex"), v.getId());
                }
            }

            //connect with least cost edges
            double costCandidate;
            Collection<UndirectedVertex> oddVertices = matchingGraph.getVertices();
            HashMap<Pair<Integer>, Edge> traverseIj = new HashMap<Pair<Integer>, Edge>(); //key is (i,j) where i < j, and value is true if the shortest average path cost is i to j, false if it's j to i
            Pair<Integer> candidateKey;
            for (UndirectedVertex v : oddVertices) {
                CommonAlgorithms.dijkstrasAlgorithm(fullGraph, v.getMatchId(), dist, path, edgePath);
                for (UndirectedVertex v2 : oddVertices) {
                    //only add one edge per pair of vertices
                    if (v.getId() == v2.getId())
                        continue;

                    costCandidate = calculateAveragePathCost(fullGraph, v.getMatchId(), v2.getMatchId(), path, edgePath);
                    candidateKey = new Pair<Integer>(v2.getId(), v.getId());

                    if (!traverseIj.containsKey(candidateKey) || costCandidate < traverseIj.get(candidateKey).getCost()) {
                        traverseIj.remove(candidateKey);
                        traverseIj.put(new Pair<Integer>(v.getId(), v2.getId()), new Edge("matchingEdge", new Pair<UndirectedVertex>(v, v2), (int) (2 * costCandidate)));
                    }
                }
            }

            for (Edge e : traverseIj.values())
                matchingGraph.addEdge(e);

            Set<Pair<UndirectedVertex>> matchingSolution = CommonAlgorithms.minCostMatching(matchingGraph);

            //now add the corresponding edges back in the windy graph
            int curr, end, next, nextEdge;
            TIntObjectHashMap<WindyEdge> indexedEdges = fullGraph.getInternalEdgeMap();
            WindyEdge temp;
            for (Pair<UndirectedVertex> p : matchingSolution) {

                //minCostMatching doesn't discriminate between 1 - 2 and 2 - 1 so we need to
                if (traverseIj.containsKey(new Pair<Integer>(p.getFirst().getId(), p.getSecond().getId()))) {
                    curr = p.getFirst().getMatchId();
                    end = p.getSecond().getMatchId();
                } else {
                    curr = p.getSecond().getMatchId();
                    end = p.getFirst().getMatchId();
                }

                CommonAlgorithms.dijkstrasAlgorithm(fullGraph, curr, dist, path, edgePath);

                do {
                    next = path[end];
                    nextEdge = edgePath[end];
                    temp = indexedEdges.get(nextEdge);
                    g.addEdge(temp.getEndpoints().getFirst().getId(), temp.getEndpoints().getSecond().getId(), "to make even", temp.getCost(), temp.getReverseCost(), nextEdge, temp.isRequired());
                } while ((end = next) != curr);
            }

            //should be Eulerian now
            if (!CommonAlgorithms.isEulerian(g))
                LOGGER.error("The UCPP augmentation failed.");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static int[] solvePseudoMinCostFlow(DirectedGraph g) {
        try {
            DirectedGraph copy = g.getDeepCopy();
            int n = copy.getVertices().size();
            int m = copy.getEdges().size();
            int[] ans = new int[m + 1];
            int[] realIds = new int[m + 1];
            int[] artIds = new int[m + 1];
            for (int i = 1; i <= m; i++) {
                realIds[i] = i;
            }

            int[] dist;
            int[] path;
            int[] edgePath;
            boolean foundImprovement = true;
            int maxFlow;
            TIntObjectHashMap<Arc> flowArcs = copy.getInternalEdgeMap();
            Arc temp, temp2;
            while (foundImprovement) {
                foundImprovement = false;
                dist = new int[n + 1];
                path = new int[n + 1];
                edgePath = new int[n + 1];

                try {
                    CommonAlgorithms.slfShortestPaths(copy, copy.getDepotId(), dist, path, edgePath);
                } catch (NegativeCycleException e) {

                    foundImprovement = true;

                    maxFlow = Integer.MAX_VALUE;
                    //calculate max flow we can push
                    int lim = e.getViolatingEdgePath().length;
                    int[] problemEdgePath = e.getViolatingEdgePath();
                    for (int i = 0; i < lim; i++) {
                        temp = flowArcs.get(problemEdgePath[i]);
                        if (temp.getCapacity() < maxFlow) {
                            maxFlow = temp.getCapacity();
                        }
                    }

                    //push it and manage residuals
                    for (int i = 0; i < lim; i++) {
                        //next = path[curr][end];
                        temp = flowArcs.get(problemEdgePath[i]);

                        if (artIds[temp.getMatchId()] == temp.getId()) // if we're artificial
                        {
                            ans[temp.getMatchId()] -= maxFlow;
                            //if we don't have a real arc corresponding to this, then add one
                            if (realIds[temp.getMatchId()] == 0) {
                                temp2 = new Arc("real insertion", new Pair<DirectedVertex>(temp.getHead(), temp.getTail()), -temp.getCost());
                                temp2.setCapacity(maxFlow);
                                copy.addEdge(temp2, temp.getMatchId());
                                realIds[temp.getMatchId()] = temp2.getId();
                            }
                            //if we do have a real arc, update its capacity
                            else {
                                temp2 = flowArcs.get(realIds[temp.getMatchId()]);
                                if (temp2.isCapacitySet())
                                    temp2.setCapacity(temp2.getCapacity() + maxFlow);
                            }

                            //update capacities
                            temp.setCapacity(temp.getCapacity() - maxFlow);
                            //remove if the capcity is zero
                            if (temp.getCapacity() == 0) {
                                artIds[temp.getMatchId()] = 0;
                                copy.removeEdge(temp);
                            }
                        } else if (realIds[temp.getMatchId()] == temp.getId())//we're real
                        {
                            ans[temp.getMatchId()] += maxFlow;
                            //if  we don't have an artificial arc corresponding to this, then add one
                            if (artIds[temp.getMatchId()] == 0) {
                                temp2 = new Arc("real insertion", new Pair<DirectedVertex>(temp.getHead(), temp.getTail()), -temp.getCost());
                                temp2.setCapacity(maxFlow);
                                copy.addEdge(temp2, temp.getMatchId());
                                artIds[temp.getMatchId()] = temp2.getId();
                            } else {
                                temp2 = flowArcs.get(artIds[temp.getMatchId()]);
                                temp2.setCapacity(temp2.getCapacity() + maxFlow);
                            }

                            //update capacity of temp
                            if (temp.isCapacitySet())//if no capacity set, then we assume infinite capacity
                            {
                                temp.setCapacity(temp.getCapacity() - maxFlow);
                                //remove if the capcity is zero
                                if (temp.getCapacity() == 0) {
                                    realIds[temp.getMatchId()] = 0;
                                    copy.removeEdge(temp);
                                }
                            }
                        } else {
                            LOGGER.error("BADDD: In the third improvement procedure for Benavent's Solver, we have a flow arc that " +
                                    "isn't recognized as real or artificial.");
                        }
                    }


                }
            }

            return ans;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isValidAugmentation(WindyGraph orig, DirectedGraph ans) {
        HashSet<Integer> reqIndices = new HashSet<Integer>();
        for (WindyEdge we : orig.getEdges()) {
            if (we.isRequired())
                reqIndices.add(we.getId());
        }

        //check to make sure there's a corresponding edge in orig
        TIntObjectHashMap<WindyVertex> origVertices = orig.getInternalVertexMap();
        boolean foundCopy;
        for (Arc a : ans.getEdges()) {
            foundCopy = false;
            List<WindyEdge> candidates = orig.findEdges(new Pair<WindyVertex>(origVertices.get(a.getTail().getId()), origVertices.get(a.getHead().getId())));
            if (candidates.size() == 0) {
                return false;
            }
            for (WindyEdge we : candidates) {
                if (a.getTail().getId() == we.getEndpoints().getFirst().getId()) //forward
                {
                    if (a.getCost() == we.getCost()) {
                        foundCopy = true;
                        if (reqIndices.contains(we.getId()))
                            reqIndices.remove(we.getId());
                    }
                } else if (a.getTail().getId() == we.getEndpoints().getSecond().getId()) // backward
                {
                    if (a.getCost() == we.getReverseCost()) {
                        foundCopy = true;
                        if (reqIndices.contains(we.getId())) ;
                        reqIndices.remove(we.getId());
                    }
                } else
                    LOGGER.debug("We've got an edge that doesn't correspond to a link in the original graph...this is bad"); //wut
            }
            if (!foundCopy)
                return false;
        }
        return reqIndices.size() <= 0;
    }

    private static double calculateAveragePathCost(WindyGraph g, int i, int j, int[] path, int[] edgePath) {
        int start, end, next, ans;
        start = i;
        end = j;
        ans = 0;
        WindyEdge temp;
        TIntObjectHashMap<WindyEdge> indexedWindyEdges = g.getInternalEdgeMap();
        do {
            next = path[end];
            temp = indexedWindyEdges.get(edgePath[end]);
            ans += temp.getCost() + temp.getReverseCost();
        } while ((end = next) != start);
        return ans / 2.0;
    }

    @Override
    protected Problem<WindyVertex, WindyEdge, WindyGraph> getInstance() {
        return mInstance;
    }

    @Override
    /**
     * Implements the WRPP1 heuristic from Benavent's paper
     */
    protected Collection<Tour> solve() {
        try {

            //get a copy to operate on
            WindyGraph copy = mInstance.getGraph().getDeepCopy();

            //solve the shortest spanning tree problem to connect the required components of the graph
            WindyGraph windyReq = connectRequiredComponents(copy);

            //solve the min-cost matching problem to produce an eulerian augmentation to the original graph
            eulerAugment(copy, windyReq);

            //solve the min-cost flow problem to produce the optimal tour on the resultant windy graph
            DirectedGraph ans = constructOptimalWindyTour(windyReq);

            //go through the improvement procedures described in Benavent that eliminate added cycles
            eliminateRedundantCycles(ans, windyReq, copy);


            //return the answer
            ArrayList<Integer> tour;
            tour = CommonAlgorithms.tryHierholzer(ans);
            Tour<DirectedVertex, Arc> eulerTour = new Tour<DirectedVertex, Arc>();
            TIntObjectHashMap<Arc> indexedEdges = ans.getInternalEdgeMap();
            for (int i = 0; i < tour.size(); i++) {
                eulerTour.appendEdge(indexedEdges.get(tour.get(i)));
            }
            mInstance.setSol(Utils.reclaimTour(eulerTour, mInstance.getGraph()));
            HashSet<Tour> ret = new HashSet<Tour>();
            ret.add(eulerTour);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, null, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String getSolverName() {
        return "Win's Heuristic for the Windy Rural Postman Problem";
    }

    @Override
    public Solver<WindyVertex, WindyEdge, WindyGraph> instantiate(Problem<WindyVertex, WindyEdge, WindyGraph> p) {
        return new WRPPSolver_Win(p);
    }

    @Override
    public HashMap<String, Double> getProblemParameters() {
        return new HashMap<String, Double>();
    }

    @Override
    protected boolean checkGraphRequirements() {
        // make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            WindyGraph mGraph = mInstance.getGraph();
            if (!CommonAlgorithms.isConnected(mGraph))
                return false;
        }
        return true;
    }
}
