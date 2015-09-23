
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

import gnu.trove.TIntHashSet;
import gnu.trove.TIntObjectHashMap;
import oarlib.core.*;
import oarlib.display.GraphDisplay;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.link.impl.Edge;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.multivehicle.MinMaxKWRPP;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Hybrid Solver for Survey graphic generation.  Essentially the algorithm proceeds be routing using the partitioning heuristic and then
 * choosing some number of routes to ruin and recreate using Benavent's approach.  We try to select routes that are adjacent to each other
 * in the original solution for de/recon-struction.
 * <p/>
 * Created by oliverlum on 4/11/15.
 */
public class MultiWRPPSolverHybrid extends MultiVehicleSolver<WindyVertex, WindyEdge, WindyGraph> {

    private static final Logger LOGGER = Logger.getLogger(MultiWRPPSolver.class);
    private WindyGraph mGraph;
    private String mInstanceName;
    private double bestWeight;
    private GraphDisplay mDisplay;
    private int mRoutesToDestroy;

    /**
     * Default Constructor
     *
     * @param instance        - MultiVehicleWRPP instance to solve
     * @param instanceName    - the name of the problem instance (for display purposes)
     * @param gd              - the GraphDisplay object to use to export the solution / intermediate solutions
     * @param routesToDestroy - the number of routes to ruin and recreate
     * @throws IllegalArgumentException
     */
    public MultiWRPPSolverHybrid(Problem<WindyVertex, WindyEdge, WindyGraph> instance, String instanceName, GraphDisplay gd, int routesToDestroy) throws IllegalArgumentException {
        super(instance);
        mGraph = mInstance.getGraph();
        mInstanceName = instanceName;
        mDisplay = gd;
        if (mInstance.getmNumVehicles() < routesToDestroy || routesToDestroy <= 1)
            throw new IllegalArgumentException();
        mRoutesToDestroy = routesToDestroy;
        bestWeight = -1;
    }

    @Override
    protected boolean checkGraphRequirements() {
        //make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            WindyGraph mGraph = mInstance.getGraph();
            if (!CommonAlgorithms.isConnected(mGraph))
                return false;
        }
        return true;

    }

    @Override
    protected Problem<WindyVertex, WindyEdge, WindyGraph> getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<? extends Route> solve() {

        //solve using MultiWRPPSolver
        MultiWRPPSolver solver1 = new MultiWRPPSolver(mInstance, mInstanceName, mDisplay);
        Collection<Route<WindyVertex, WindyEdge>> sol1 = solver1.solve();

        //ruin
        TIntHashSet toReroute = ruin(sol1);

        //mod the instance
        WindyGraph newGraph = mGraph.getDeepCopy();
        for (WindyEdge we : newGraph.getEdges()) {
            if (!toReroute.contains(we.getId()))
                we.setRequired(false);
        }
        MinMaxKWRPP newInstance = new MinMaxKWRPP(newGraph, mInstanceName + "Part2", mRoutesToDestroy);

        //recreate using Benavent's MultiWRPPSolver
        MultiWRPPSolver_Benavent solver2 = new MultiWRPPSolver_Benavent(newInstance, newInstance.getName());
        Collection<Route<WindyVertex, WindyEdge>> sol2 = solver2.solve();

        for (Route R : sol2)
            sol1.add(R);

        //DISPLAY
        HashMap<Integer, Integer> sol = new HashMap<Integer, Integer>();
        int counter = 1;
        for (Route<WindyVertex, WindyEdge> r : sol1) {

            List<WindyEdge> path = r.getRoute();
            ArrayList<Boolean> service = r.getServicingList();
            for (int i = 0; i < path.size(); i++) {
                if (service.get(i))
                    sol.put(path.get(i).getId(), counter);
            }
            counter++;
        }

        try {
            WindyGraph toDisplay = mGraph.getDeepCopy();
            int limi = mGraph.getEdges().size();
            for (int i = 1; i <= limi; i++) {
                WindyEdge we = toDisplay.getEdge(i);
                if (!sol.containsKey(we.getId()))
                    toDisplay.removeEdge(we.getId());
            }

            GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, toDisplay, mInstanceName);
            gd.exportWithPartition(GraphDisplay.ExportType.PDF, sol);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return sol1;
    }

    /**
     * Essentially find a tree that contains the appropriate number of elements to
     *
     * @return - the edge ids of required edges that need to be re-routed
     */
    private TIntHashSet ruin(Collection<Route<WindyVertex, WindyEdge>> sol1) {

        //quick special case for just 1

        TIntObjectHashMap<Pair<Double>> averages = new TIntObjectHashMap<Pair<Double>>();
        for (Route<WindyVertex, WindyEdge> r : sol1) {

            double sumX = 0;
            double sumY = 0;
            WindyEdge we;

            //calculate the average coordinate
            TIntHashSet alreadyScanned = new TIntHashSet();
            for (int i : r.getCompactRepresentation().toNativeArray()) {
                we = mGraph.getEdge(i);
                if (!alreadyScanned.contains(we.getFirstEndpointId())) {
                    alreadyScanned.add(we.getFirstEndpointId());
                    sumX += we.getEndpoints().getFirst().getX();
                    sumY += we.getEndpoints().getFirst().getY();
                }
                if (!alreadyScanned.contains(we.getSecondEndpointId())) {
                    alreadyScanned.add(we.getSecondEndpointId());
                    sumX += we.getEndpoints().getSecond().getX();
                    sumY += we.getEndpoints().getSecond().getY();
                }
            }

            averages.put(r.getGlobalId(), new Pair<Double>(sumX / alreadyScanned.size(), sumY / alreadyScanned.size()));

        }

        //Create the complete undirected graph
        try {
            UndirectedGraph adjacencyGraph = new UndirectedGraph(sol1.size());
            int[] routeIds = averages.keys();
            double distance;
            int id1, id2;
            TIntHashSet usedVertices = new TIntHashSet();
            double minDistance = Integer.MAX_VALUE;
            int minId = -1;
            int minId2 = -1;

            for (int i = 0; i < routeIds.length; i++) {
                id1 = routeIds[i];
                for (int j = 0; j < i; j++) {
                    id2 = routeIds[j];
                    adjacencyGraph.getVertex(j + 1).setMatchId(routeIds[j]);
                    distance = Math.sqrt(Math.pow(averages.get(id1).getFirst() - averages.get(id2).getFirst(), 2) + Math.pow(averages.get(id1).getSecond() - averages.get(id2).getSecond(), 2));
                    adjacencyGraph.addEdge(i + 1, j + 1, (int) (distance * 10000));
                    if (distance < minDistance) {
                        minDistance = distance;
                        minId = i + 1;
                        minId2 = j + 1;
                    }
                }
            }

            usedVertices.add(adjacencyGraph.getVertex(minId).getMatchId());
            usedVertices.add(adjacencyGraph.getVertex(minId2).getMatchId());

            //Now figure out the min pseudotree (with mRoutesToDestroy - 1 links in it)
            for (int i = 1; i < mRoutesToDestroy - 1; i++) {
                minDistance = Integer.MAX_VALUE;
                for (Edge e : adjacencyGraph.getEdges()) {
                    if (usedVertices.contains(e.getEndpoints().getFirst().getMatchId()) && usedVertices.contains(e.getEndpoints().getSecond().getMatchId()))
                        continue;
                    if (!(usedVertices.contains(e.getEndpoints().getFirst().getMatchId()) && usedVertices.contains(e.getEndpoints().getSecond().getMatchId())))
                        continue;
                    if (e.getCost() < minDistance) {
                        minDistance = e.getCost();
                        minId = e.getId();
                    }
                }
                Edge e = adjacencyGraph.getEdge(minId);
                if (usedVertices.contains(e.getEndpoints().getFirst().getMatchId()))
                    usedVertices.add(e.getEndpoints().getSecond().getMatchId());
                else
                    usedVertices.add(e.getEndpoints().getFirst().getMatchId());
            }


            TIntHashSet ans = new TIntHashSet();
            Collection<Route<WindyVertex, WindyEdge>> toRemove = new HashSet<Route<WindyVertex, WindyEdge>>();
            for (Route<WindyVertex, WindyEdge> r : sol1) {
                if (usedVertices.contains(r.getGlobalId())) {
                    ans.addAll(r.getCompactRepresentation().toNativeArray());
                    toRemove.add(r);
                }
            }

            sol1.removeAll(toRemove);

            return ans;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.MULTI_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String getSolverName() {
        return "Min-Max K Windy Rural Postman Hybrid Solver";
    }

    @Override
    public Solver<WindyVertex, WindyEdge, WindyGraph> instantiate(Problem<WindyVertex, WindyEdge, WindyGraph> p) {
        return new MultiWRPPSolverHybrid(p, p.getName(), mDisplay, mRoutesToDestroy);
    }

    @Override
    public HashMap<String, Double> getProblemParameters() {
        HashMap<String, Double> ret = new HashMap<String, Double>();
        ret.put("Best Weight", new Double(bestWeight));
        return ret;
    }
}
