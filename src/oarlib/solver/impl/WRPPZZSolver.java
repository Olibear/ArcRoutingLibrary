/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013-2016 Oliver Lum
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
 *
 */
package oarlib.solver.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.*;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.impl.ZigZagGraph;
import oarlib.graph.transform.impl.ZigZagToWindyTransform1;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Utils;
import oarlib.link.impl.Arc;
import oarlib.link.impl.WindyEdge;
import oarlib.link.impl.ZigZagLink;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.DirectedVertex;
import oarlib.vertex.impl.WindyVertex;
import oarlib.vertex.impl.ZigZagVertex;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by oliverlum on 6/20/15.
 */
public class WRPPZZSolver extends SingleVehicleSolver<ZigZagVertex, ZigZagLink, ZigZagGraph> {

    Logger LOGGER = Logger.getLogger(WRPPZZSolver.class);

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public WRPPZZSolver(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> instance) throws IllegalArgumentException {
        super(instance);
    }

    private static void computeTour(ZigZagGraph graph, Route<WindyVertex, WindyEdge> t) {
        //calculate the cost, and print it, while reclaiming as well

        List<WindyEdge> route = t.getPath();
        ArrayList<Boolean> dir = t.getTraversalDirection();
        ZigZagLink temp;
        Tour<ZigZagVertex, ZigZagLink> zzAns = new Tour<ZigZagVertex, ZigZagLink>();
        int i = 0;
        double cost = 0;

        //this data structure keeps track of who we've serviced.
        //A value of 1 means it has been serviced in the forward direction
        //A value of 2 means it has been serviced in the reverse direction
        //A value of 3 means all service requirements have been fulfilled.
        HashMap<Integer, Integer> traversed = new HashMap<Integer, Integer>();


        //this data structure keeps track of optionally meanderable edges whose cost calculation is postponed.
        //A value of 4 means temp is meander optional, but was only traversed forward in the solution.
        //A value of 5 means temp is meander optional, but was only traversed backward in the solution.
        //A value of 6 means temp is meander optional, and was traversed both ways in the solution.
        HashMap<Integer, Integer> postponed = new HashMap<Integer, Integer>();

        for (WindyEdge we : route) {
            temp = graph.getEdge(we.getMatchId());
            //zzAns.appendEdge(temp);

            //cost computation

            //case 1: it's not required, then just add it
            if (!temp.isRequired() && !temp.isReverseRequired()) {
                if (dir.get(i))
                    cost += temp.getCost();
                else
                    cost += temp.getReverseCost();
            }
            //if it's required, but not meanderable...
            else if (temp.getStatus() == ZigZagLink.ZigZagStatus.NOT_AVAILABLE) {
                //case 2: if it's only required in one direction

                //required forward
                if (!temp.isReverseRequired()) {
                    if (dir.get(i)) {
                        cost += temp.getCost();
                        if (!traversed.containsKey(temp.getId())) {
                            cost += temp.getServiceCost();
                            traversed.put(temp.getId(), 3);
                        }
                    } else {
                        cost += temp.getReverseCost();
                    }
                }
                //required reverse
                else if (!temp.isRequired()) {
                    if (dir.get(i)) {
                        cost += temp.getCost();
                    } else {
                        cost += temp.getReverseCost();
                        if (!traversed.containsKey(temp.getId())) {
                            cost += temp.getReverseServiceCost();
                            traversed.put(temp.getId(), 3);
                        }
                    }
                } else {
                    if (dir.get(i)) {
                        cost += temp.getCost();
                        if (!traversed.containsKey(temp.getId())) {
                            cost += temp.getServiceCost();
                            traversed.put(temp.getId(), 1);
                        } else if (traversed.get(temp.getId()) == 2) {
                            cost += temp.getServiceCost();
                            traversed.put(temp.getId(), 3);
                        }
                    } else {
                        cost += temp.getReverseCost();
                        if (!traversed.containsKey(temp.getId())) {
                            cost += temp.getReverseServiceCost();
                            traversed.put(temp.getId(), 2);
                        } else if (traversed.get(temp.getId()) == 1) {
                            cost += temp.getReverseServiceCost();
                            traversed.put(temp.getId(), 3);
                        }
                    }
                }

            }
            //case 3: if it's meander required, we add it, and simply incur the meander cost later
            else if (temp.getStatus() == ZigZagLink.ZigZagStatus.MANDATORY) {
                if (!traversed.containsKey(temp.getId())) {
                    cost += temp.getZigzagCost() + temp.getServiceCost() + temp.getReverseServiceCost();
                    traversed.put(temp.getId(), 3);
                } else if (dir.get(i))
                    cost += temp.getCost();
                else
                    cost += temp.getReverseCost();
            }
            //if it's optional...
            else if (temp.getStatus() == ZigZagLink.ZigZagStatus.OPTIONAL) {
                if (dir.get(i)) {
                    cost += temp.getCost();

                    if (!postponed.containsKey(temp.getId()))
                        postponed.put(temp.getId(), 4);
                    else if (postponed.get(temp.getId()) == 5)
                        postponed.put(temp.getId(), 6);
                } else {
                    cost += temp.getReverseCost();

                    if (!postponed.containsKey(temp.getId()))
                        postponed.put(temp.getId(), 5);
                    else if (postponed.get(temp.getId()) == 4)
                        postponed.put(temp.getId(), 6);
                }
            }

            i++;
        }

        for (int id : postponed.keySet()) {
            temp = graph.getEdge(id);
            //case 4: but only required in one direction, model it as a required edge

            //required forward
            if (!temp.isReverseRequired()) {
                if (postponed.get(id) == 5)
                    cost += temp.getZigzagCost() + temp.getServiceCost() - temp.getReverseCost();
                else
                    cost += temp.getServiceCost();
            }
            //required reverse
            else if (!temp.isRequired()) {
                if (postponed.get(id) == 4)
                    cost += temp.getZigzagCost() + temp.getReverseServiceCost() - temp.getCost();
                else
                    cost += temp.getReverseServiceCost();
            }
            //case 5: but bidirectional, add it; if we happen to traverse in both directions, we reap savings, otherwise meander
            else {
                cost += temp.getServiceCost() + temp.getReverseServiceCost();
                if (postponed.get(id) == 4)
                    cost += temp.getZigzagCost() - temp.getCost();
                else if (postponed.get(id) == 5)
                    cost += temp.getZigzagCost() - temp.getReverseCost();
            }
        }

        System.out.println("The final zig-zag solution cost is: " + cost);

        return;
    }

    private void completeConnect(WindyGraph g, WindyGraph simplified) {

        int firstId = 0;
        int secondId = 0;

        int BIG = 0;
        for (ZigZagLink zzl : mInstance.getGraph().getEdges())
            BIG += zzl.getCost() + zzl.getReverseCost();

        try {
            //set up the directed graph
            DirectedGraph connected = new DirectedGraph(g.getVertices().size());
            for (WindyEdge we : g.getEdges()) {
                if (we.getCost() != BIG)
                    connected.addEdge(we.getFirstEndpointId(), we.getSecondEndpointId(), 1);
                if (we.getReverseCost() != BIG)
                    connected.addEdge(we.getSecondEndpointId(), we.getFirstEndpointId(), 1);
            }

            int[] components = CommonAlgorithms.stronglyConnectedComponents(connected);


            WindyVertex firstVertex, secondVertex;
            ArrayList<WindyEdge> conns;
            for (WindyEdge we : simplified.getEdges()) {
                if (we.isRequired()) {
                    firstId = we.getFirstEndpointId();
                    secondId = we.getSecondEndpointId();

                    if (components[firstId] != components[secondId]) {
                        firstVertex = simplified.getVertex(firstId);
                        secondVertex = simplified.getVertex(secondId);

                        conns = firstVertex.getNeighbors().get(secondVertex);
                        for (WindyEdge we2 : conns) {
                            //checksum should be sufficient
                            if (we2.getCost() + we2.getReverseCost() != we.getCost() + we.getReverseCost())
                                g.addEdge(we2.getFirstEndpointId(), we2.getSecondEndpointId(), "", we2.getCost(), we2.getReverseCost(), we2.getId(), false);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Collection<Tour> windySolve(WindyGraph simplified) {
        try {
            /*
             * Connect up the required components of the graph, just as in WRPP1.
			 * Match ids in windyReq correspond to edge ids in copy after this.
			 */
            WindyGraph windyReq = WRPPSolver_Win.connectRequiredComponents(simplified);

            /*
             * NEW TO THIS SOLVER: we need to find vertices that have become effectively disconnected, and add
             * parllel edges to connect them.
             */
            completeConnect(windyReq, simplified);

            //calculate average cost of edges in Er', so add up (cij + cji)/2, and then divide by num edges of windyReq
            double averageCost = WRPPSolver_Benavent_H1.calculateAverageCost(windyReq);

            //construct E1 and E2
            HashSet<Integer> E1 = new HashSet<Integer>();
            HashSet<Integer> E2 = new HashSet<Integer>();

			/*
             * Build out the edge sets E1 and E2, which hold the particularly asymmetric edges from windyReq, and
			 * the everyone else respectively. We do so by searching windyReq for asymmetric edges, and adding their
			 * match ids (ids in copy) to E1.  Then we go through copy's edges, and add the rest to E2.
			 */
            WRPPSolver_Benavent_H1.buildEdgeSets(E1, E2, windyReq, simplified, averageCost);

			/*
             * Build Gdr, which is a graph that has only the edges in E1 in it as arcs directed in the cheap direction.
			 * The purpose of doing so is to set demands for the flow problem we're about to solve.
			 */
            DirectedGraph Gdr = WRPPSolver_Benavent_H1.buildGdr(simplified, E1);
            HashSet<Integer> L = new HashSet<Integer>();
            if (!CommonAlgorithms.isEulerian(Gdr)) {

				/*
                 * Build Gaux, which is a graph that is the directed graph induced by copy, PLUS
				 * an extra arc for each edge in E1 in the expensive direction, with cost = (cji - cij)/2.
				 * The match ids here will be ids in copy for the inf. capacity arcs, and -1 for the
				 * artificial ones.
				 */
                DirectedGraph Gaux = WRPPSolver_Benavent_H1.buildGaux(simplified, E1);

                //set up the flow problem on Gaux using demands from Gdr
                TIntObjectHashMap<DirectedVertex> indexedVertices = Gdr.getInternalVertexMap();
                for (DirectedVertex v : Gaux.getVertices()) {
                    v.setDemand(indexedVertices.get(v.getId()).getDelta());
                }


                //solve the flow problem on Gaux with demands from Gdr
                int flowanswer[] = CommonAlgorithms.shortestSuccessivePathsMinCostNetworkFlow(Gaux);

				/*
                 * Create a list of ids L (in copy) which represent guys that are likely to appear in the min cost flow
				 * solution we solve to construct the optimal windy tour.
				 */
                L = WRPPSolver_Benavent_H1.buildL(Gaux, E1, E2, flowanswer);
            }

			/*
             * Perform the same euler augmentation process the same as in WRPP1, except that this time,
			 * when we solve the matching, we want the edges we marked in L to be of zero cost
			 * to coerce the matching to use these.
			 */
            WRPPSolver_Benavent_H1.eulerAugment(simplified, windyReq, L);
            DirectedGraph ans = WRPPSolver_Win.constructOptimalWindyTour(windyReq);

            ans.setDepotId(simplified.getDepotId());
            WRPPSolver_Win.eliminateRedundantCycles(ans, windyReq, simplified);

            ArrayList<Integer> tour;
            tour = CommonAlgorithms.tryHierholzer(ans);

            Tour<DirectedVertex, Arc> eulerTour = new Tour<DirectedVertex, Arc>();
            TIntObjectHashMap<Arc> indexedEdges = ans.getInternalEdgeMap();
            for (int i = 0; i < tour.size(); i++) {
                eulerTour.appendEdge(indexedEdges.get(tour.get(i)));
            }

            HashSet<Tour> ret = new HashSet<Tour>();
            ret.add(eulerTour);
            return ret;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected boolean checkGraphRequirements() {
        // make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            UndirectedGraph connectedCheck = new UndirectedGraph(mInstance.getGraph().getVertices().size());
            try {
                for (ZigZagLink l : mInstance.getGraph().getEdges())
                    connectedCheck.addEdge(l.getFirstEndpointId(), l.getSecondEndpointId(), 1);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            if (!CommonAlgorithms.isConnected(connectedCheck))
                return false;
        }
        return true;
    }

    @Override
    protected Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<Tour> solve() {

        ZigZagGraph mGraph = mInstance.getGraph();
        ZigZagToWindyTransform1 transformer = new ZigZagToWindyTransform1(mGraph);
        WindyGraph simplifiedGraph = transformer.transformGraph();

        Collection<Tour> simplifiedAns = windySolve(simplifiedGraph);
        for (Tour t : simplifiedAns)
            computeTour(mGraph, Utils.reclaimTour(t, simplifiedGraph));

        //should never happen
        return simplifiedAns;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String getSolverName() {
        return "Simple Heuristic for the Windy Rural Postman Problem";
    }

    @Override
    public Solver<ZigZagVertex, ZigZagLink, ZigZagGraph> instantiate(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> p) {
        return new WRPPZZSolver(p);
    }

    @Override
    public HashMap<String, Double> getProblemParameters() {
        return new HashMap<String, Double>();
    }
}
