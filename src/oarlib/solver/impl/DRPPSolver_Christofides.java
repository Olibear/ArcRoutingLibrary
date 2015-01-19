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
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.link.impl.Arc;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.DirectedVertex;

import java.util.*;

public class DRPPSolver_Christofides extends SingleVehicleSolver<DirectedVertex, Arc, DirectedGraph> {

    public DRPPSolver_Christofides(Problem<DirectedVertex, Arc, DirectedGraph> instance) throws IllegalArgumentException {
        super(instance);
    }

    private static DirectedGraph connectAndExpand(DirectedGraph gCollapsed, DirectedGraph gOrig, int[] component, int root) {
        try {
            int n = gOrig.getVertices().size();

            DirectedGraph ans = new DirectedGraph();
            TIntObjectHashMap<Arc> gColArcs = gCollapsed.getInternalEdgeMap();
            TIntObjectHashMap<Arc> gOriArcs = gOrig.getInternalEdgeMap();
            for (int i = 0; i < n; i++) {
                ans.addVertex(new DirectedVertex("req"));
            }

            //first add in all the required arcs from gOrig
            for (Arc a : gOrig.getEdges()) {
                if (a.isRequired())
                    ans.addEdge(a.getTail().getId(), a.getHead().getId(), "req", a.getCost());
            }

            //then add all the guys from the MSA
            Set<Integer> msaArcs = CommonAlgorithms.minSpanningArborescence(gCollapsed, root);

            Arc toCopy;
            for (Integer i : msaArcs) {
                toCopy = gOriArcs.get(gColArcs.get(i).getMatchId());
                ans.addEdge(toCopy.getTail().getId(), toCopy.getHead().getId(), "from MSA", toCopy.getCost(), false);
            }

            return ans;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unused")
    private static DirectedGraph connectAndExpandWithShortestPaths(DirectedGraph gCollapsed, DirectedGraph gOrig, int[] component, int root, HashMap<Pair<Integer>, Pair<Integer>> bestConnections) {
        try {
            int n = gOrig.getVertices().size();

            DirectedGraph ans = new DirectedGraph();
            TIntObjectHashMap<Arc> gColArcs = gCollapsed.getInternalEdgeMap();
            TIntObjectHashMap<Arc> gOriArcs = gOrig.getInternalEdgeMap();
            for (int i = 0; i < n; i++) {
                ans.addVertex(new DirectedVertex("req"));
            }

            //first add in all the required arcs from gOrig
            for (Arc a : gOrig.getEdges()) {
                if (a.isRequired())
                    ans.addEdge(a.getTail().getId(), a.getHead().getId(), "req", a.getCost());
            }

            //then add all the guys from the MSA
            Set<Integer> msaArcs = CommonAlgorithms.minSpanningArborescence(gCollapsed, root);

            Arc toCopy;
            Pair<Integer> tempKey;
            for (Integer i : msaArcs) {
                toCopy = gColArcs.get(i);
                tempKey = new Pair<Integer>(toCopy.getTail().getId(), toCopy.getHead().getId());
                ans.addEdge(bestConnections.get(tempKey).getFirst(), bestConnections.get(tempKey).getSecond(), "from MSA", toCopy.getCost(), false);
            }

            return ans;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DirectedGraph formGc1(DirectedGraph g) {
        try {
            DirectedGraph ans = new DirectedGraph();
            //figure out Nr from Ar
            HashSet<Integer> nrIds = new HashSet<Integer>();
            ArrayList<Arc> reqArcs = new ArrayList<Arc>();
            for (Arc a : g.getEdges()) {
                if (a.isRequired()) {
                    reqArcs.add(a);
                    nrIds.add(a.getHead().getId());
                    nrIds.add(a.getTail().getId());
                }
            }

            TIntObjectHashMap<DirectedVertex> gVertices = g.getInternalVertexMap();
            int i = 1;
            for (Integer id : nrIds) {
                gVertices.get(id).setMatchId(i++);
                ans.addVertex(new DirectedVertex("req node"), id);
            }
            for (Arc a : reqArcs) {
                ans.addEdge(a.getTail().getMatchId(), a.getHead().getMatchId(), "req arc", a.getCost(), a.getId());
            }

            //now make it complete
            int n = g.getVertices().size();
            int[][] dist = new int[n + 1][n + 1];
            int[][] path = new int[n + 1][n + 1];
            int[][] edgePath = new int[n + 1][n + 1];
            CommonAlgorithms.fwLeastCostPaths(g, dist, path, edgePath);
            Arc toAdd;
            for (DirectedVertex v1 : ans.getVertices()) {
                for (DirectedVertex v2 : ans.getVertices()) {
                    if (v1.getId() == v2.getId())
                        continue;
                    toAdd = ans.constructEdge(v1.getId(), v2.getId(), "shortest path costs", dist[v1.getMatchId()][v2.getMatchId()]);
                    toAdd.setRequired(false);
                    ans.addEdge(toAdd);

                }
            }

            return ans;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static DirectedGraph formGc2(DirectedGraph g) {
        DirectedGraph copy = g.getDeepCopy();
        List<Arc> temp;
        int m = copy.getEdges().size();
        int tempLength;
        Arc a, a2;

        //first eliminate same cost, parallel arcs
        TIntObjectHashMap<Arc> copyArcs = copy.getInternalEdgeMap();
        for (int i = 1; i <= m; i++) {
            if (!copyArcs.containsKey(i))
                continue; //might have removed

            a = copyArcs.get(i);
            temp = copy.findEdges(a.getEndpoints());
            tempLength = temp.size();

            for (int j = 0; j < tempLength; j++) {
                a2 = temp.get(j);
                if (a2.getId() != a.getId() && a.getCost() == a2.getCost()) {
                    if (!a2.isRequired()) {
                        copy.removeEdge(a2); //maybe concurrent mod issues
                    }
                }
            }
        }

        //next eliminate redundant arcs, (cij = cik + ckj)
        int n = copy.getVertices().size();
        int[][] dist = new int[n + 1][n + 1];
        int[][] path = new int[n + 1][n + 1];
        int[][] edgePath = new int[n + 1][n + 1];
        CommonAlgorithms.fwLeastCostPaths(copy, dist, path, edgePath);

        int tempCost, tailId, headId;
        for (int i = 1; i <= m; i++) {
            if (!copyArcs.containsKey(i))
                continue;

            a = copyArcs.get(i);
            if (a.isRequired())
                continue;
            tempCost = a.getCost();
            tailId = a.getTail().getId();
            headId = a.getHead().getId();
            for (int j = 1; j <= n; j++) {
                if (tempCost == dist[tailId][j] + dist[j][headId]) {
                    copy.removeEdge(a);
                    break;
                }
            }
        }
        return copy;
    }

    private static DirectedGraph collapseGraph(DirectedGraph g, int[] component) throws IllegalArgumentException {
        //form the directed graph with only required arcs corresponding to edges,
        //then figure out connected components.  Finally, collapse around these sets,
        //and choose the min cost arc joining two clusters to be the representative.
        try {
            int n = g.getVertices().size();

            if (component.length != n + 1)
                throw new IllegalArgumentException();

            int numReqArcs = 0;
            for (Arc a : g.getEdges()) {
                if (a.isRequired()) {
                    numReqArcs++;
                }
            }
            int[] edgei = new int[numReqArcs + 1];
            int[] edgej = new int[numReqArcs + 1];
            int i = 1;
            for (Arc a : g.getEdges()) {
                if (a.isRequired()) {
                    edgei[i] = a.getTail().getId();
                    edgej[i++] = a.getHead().getId();
                }
            }

            CommonAlgorithms.connectedComponents(n, numReqArcs, edgei, edgej, component);

            //now collapse them
            DirectedGraph ans = new DirectedGraph();
            for (int j = 0; j < component[0]; j++) {
                ans.addVertex(new DirectedVertex("collapsed"));
            }

            //figure out who to collapse
            int tempCost, tailId, headId;
            Pair<Integer> tempKey;
            //keys are the components its connecting, and values are the best cost / arc id pair going between them
            HashMap<Pair<Integer>, Pair<Integer>> bestConnections = new HashMap<Pair<Integer>, Pair<Integer>>();
            for (Arc a : g.getEdges()) {
                //we only want arcs that aren't internal to the connected components.
                if (!a.isRequired()) {
                    tempCost = a.getCost();
                    tailId = component[a.getTail().getId()];
                    headId = component[a.getHead().getId()];
                    if (tailId == headId)
                        continue;
                    tempKey = new Pair<Integer>(tailId, headId);
                    if (!bestConnections.containsKey(tempKey) || tempCost < bestConnections.get(tempKey).getFirst()) {
                        bestConnections.put(tempKey, new Pair<Integer>(tempCost, a.getId()));
                    }
                }
            }

            for (Pair<Integer> key : bestConnections.keySet()) {
                ans.addEdge(key.getFirst(), key.getSecond(), "components", bestConnections.get(key).getFirst(), bestConnections.get(key).getSecond());
            }

            return ans;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unused")
    private static DirectedGraph collapseGraphWithShortestPaths(DirectedGraph g, int[] component, HashMap<Pair<Integer>, Pair<Integer>> bestConnections) throws IllegalArgumentException {
        //form the directed graph with only required arcs corresponding to edges,
        //then figure out connected components.  Finally, collapse around these sets,
        //and choose the min cost arc joining two clusters to be the representative.
        try {
            int n = g.getVertices().size();

            if (component.length != n + 1)
                throw new IllegalArgumentException();

            int numReqArcs = 0;
            for (Arc a : g.getEdges()) {
                if (a.isRequired()) {
                    numReqArcs++;
                }
            }
            int[] edgei = new int[numReqArcs + 1];
            int[] edgej = new int[numReqArcs + 1];
            int i = 1;
            for (Arc a : g.getEdges()) {
                if (a.isRequired()) {
                    edgei[i] = a.getTail().getId();
                    edgej[i++] = a.getHead().getId();
                }
            }

            CommonAlgorithms.connectedComponents(n, numReqArcs, edgei, edgej, component);

            //now collapse them
            DirectedGraph ans = new DirectedGraph();
            for (int j = 0; j < component[0]; j++) {
                ans.addVertex(new DirectedVertex("collapsed"));
            }

            //shortest paths for costs
            int[][] dist = new int[n + 1][n + 1];
            int[][] path = new int[n + 1][n + 1];
            int[][] edgePath = new int[n + 1][n + 1];
            CommonAlgorithms.fwLeastCostPaths(g, dist, path, edgePath);

            //figure out who to collapse
            int tempCost, tailId, headId;
            Pair<Integer> tempKey;
            //keys are the components its connecting, and values are the best cost / arc id pair going between them
            //HashMap<Pair<Integer>,Pair<Integer>> bestConnections = new HashMap<Pair<Integer>, Pair<Integer>>();
            for (DirectedVertex dv1 : g.getVertices()) {
                for (DirectedVertex dv2 : g.getVertices()) {
                    tailId = component[dv1.getId()];
                    headId = component[dv2.getId()];
                    if (tailId == headId)
                        continue;
                    tempKey = new Pair<Integer>(tailId, headId);
                    if (!bestConnections.containsKey(tempKey) || dist[dv1.getId()][dv2.getId()] < dist[bestConnections.get(tempKey).getFirst()][bestConnections.get(tempKey).getSecond()]) {
                        bestConnections.put(tempKey, new Pair<Integer>(dv1.getId(), dv2.getId()));
                    }
                }
            }

            for (Pair<Integer> key : bestConnections.keySet()) {
                ans.addEdge(key.getFirst(), key.getSecond(), "components", dist[bestConnections.get(key).getFirst()][bestConnections.get(key).getSecond()]);
            }

            return ans;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Collection<Tour<DirectedVertex, Arc>> solve() {

        try {
            DirectedGraph copy = mInstance.getGraph();

            //form the complete graph Gc1 = (Nr, Ar U As)
            DirectedGraph Gc1 = formGc1(copy);

            //simplify Gc1 to get Gc2
            DirectedGraph Gc2 = formGc2(Gc1);

            //Now, Gc2 is the graph that we solve the DRPP on.  Any feasible solution here will correspond
            //to one in the original graph, which we shall construct at the end.

            //Collapse the graph to its connected components with min cost arcs joining them
            int n = Gc2.getVertices().size();
            int[] component = new int[n + 1];
            DirectedGraph Gc = collapseGraph(Gc2, component);

            //now, for each root, solve the problem, and pick the cheapest
            int bestCost = Integer.MAX_VALUE;
            int tempCost;
            ArrayList<Integer> bestTour = new ArrayList<Integer>();
            TIntObjectHashMap<Arc> bestArcs = new TIntObjectHashMap<Arc>();
            for (int root = 1; root <= Gc.getVertices().size(); root++) {
                //compute a shortest spanning arborescence rooted at a component node and re/expand
                DirectedGraph Gfinal = connectAndExpand(Gc, Gc2, component, root);

                //solve an uncapacitated min-cost flow problem, and then add the appropriate arcs
                TIntObjectHashMap<DirectedVertex> gfinalVertices = Gfinal.getInternalVertexMap();
                DirectedGraph Gc2copy = Gc2.getDeepCopy(); //in order to get min cost flow to work
                for (DirectedVertex v : Gc2copy.getVertices()) {
                    v.setDemand(gfinalVertices.get(v.getId()).getDelta()); //set demands according to Gfinal
                }
                int[] flowanswer = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(Gc2copy);
                TIntObjectHashMap<Arc> indexedArcs = Gc2copy.getInternalEdgeMap();
                Arc temp;
                //add the solution to the graph (augment)
                for (int i = 1; i < flowanswer.length; i++) {
                    temp = indexedArcs.get(i);
                    for (int j = 0; j < flowanswer[i]; j++) {
                        Gfinal.addEdge(temp.getTail().getId(), temp.getHead().getId(), "min cost flow", temp.getCost(), false);
                    }
                }

                tempCost = 0;
                for (Arc a : Gfinal.getEdges())
                    if (!a.isRequired())
                        tempCost += a.getCost();
                if (tempCost < bestCost) {
                    bestCost = tempCost;
                    bestTour = CommonAlgorithms.tryHierholzer(Gfinal);
                    bestArcs = Gfinal.getInternalEdgeMap();
                }
            }

            //now reproduce the solution in the original graph
            Tour<DirectedVertex, Arc> eulerTour = new Tour<DirectedVertex, Arc>();
            Arc temp;
            DirectedVertex dv1, dv2;
            TIntObjectHashMap<DirectedVertex> gc1Vertices = Gc1.getInternalVertexMap();
            TIntObjectHashMap<DirectedVertex> copyVertices = copy.getInternalVertexMap();
            TIntObjectHashMap<Arc> copyArcs = copy.getInternalEdgeMap();
            List<Arc> tempConns = new ArrayList<Arc>();

            int nOrig = copy.getVertices().size();
            int[][] dist = new int[nOrig + 1][nOrig + 1];
            int[][] path = new int[nOrig + 1][nOrig + 1];
            int[][] edgePath = new int[nOrig + 1][nOrig + 1];
            CommonAlgorithms.fwLeastCostPaths(copy, dist, path, edgePath);

            int curr, end, next;
            boolean foundConnection = false;

            for (int i = 0; i < bestTour.size(); i++) {
                temp = bestArcs.get(bestTour.get(i));
                dv1 = copyVertices.get(gc1Vertices.get(temp.getTail().getId()).getMatchId());
                dv2 = copyVertices.get(gc1Vertices.get(temp.getHead().getId()).getMatchId());

                tempConns = copy.findEdges(new Pair<DirectedVertex>(dv1, dv2));

                if (temp.isRequired()) //we must find it
                {
                    for (Arc a : tempConns) {
                        if (a.getCost() == temp.getCost() && a.isRequired()) {
                            eulerTour.appendEdge(a);
                            break;
                        }
                    }
                } else if (tempConns.size() > 0) //just throw in this edge if it exists
                {
                    foundConnection = false;
                    for (Arc a : tempConns) {
                        if (a.getCost() == temp.getCost()) {
                            eulerTour.appendEdge(a);
                            foundConnection = true;
                            break;
                        }
                    }
                    if (!foundConnection) {
                        curr = dv1.getId();
                        end = dv2.getId();
                        do {
                            next = path[curr][end];
                            eulerTour.appendEdge(copyArcs.get(edgePath[curr][end]));
                        } while ((curr = next) != end);
                    }
                } else {
                    curr = dv1.getId();
                    end = dv2.getId();
                    do {
                        next = path[curr][end];
                        eulerTour.appendEdge(copyArcs.get(edgePath[curr][end]));
                    } while ((curr = next) != end);
                }
            }

            mInstance.setSol(eulerTour);

            HashSet<Tour<DirectedVertex, Arc>> ret = new HashSet<Tour<DirectedVertex, Arc>>();
            ret.add(eulerTour);

            return ret;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.DIRECTED, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String getSolverName() {
        return "Christofides Directed Rural Postman Heuristic";
    }

    @Override
    public Solver instantiate(Problem p) {
        return new DRPPSolver_Christofides(p);
    }

    @Override
    protected Problem getInstance() {
        return mInstance;
    }

    @Override
    protected boolean checkGraphRequirements() {
        // make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            DirectedGraph mGraph = mInstance.getGraph();
            if (!CommonAlgorithms.isStronglyConnected(mGraph))
                return false;
        }
        return true;
    }

}
