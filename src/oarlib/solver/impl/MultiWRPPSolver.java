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
import oarlib.core.MultiVehicleSolver;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.display.GraphDisplay;
import oarlib.graph.factory.impl.WindyGraphFactory;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.transform.impl.EdgeInducedRequirementTransform;
import oarlib.graph.transform.partition.impl.PreciseWindyKWayPartitionTransform;
import oarlib.graph.transform.rebalance.CostRebalancer;
import oarlib.graph.transform.rebalance.impl.ClosestRequiredEdgeRebalancer;
import oarlib.graph.transform.rebalance.impl.DuplicateEdgeCostRebalancer;
import oarlib.graph.transform.rebalance.impl.IndividualDistanceToDepotRebalancer;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.graph.util.Utils;
import oarlib.improvements.metaheuristics.impl.OnePassBenaventIPFramework;
import oarlib.link.impl.WindyEdge;
import oarlib.metrics.AverageTraversalMetric;
import oarlib.metrics.RouteOverlapMetric;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.auxiliary.PartitioningProblem;
import oarlib.problem.impl.io.PartitionFormat;
import oarlib.problem.impl.io.PartitionReader;
import oarlib.problem.impl.io.ProblemFormat;
import oarlib.problem.impl.io.ProblemWriter;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 8/14/14.
 */
public class MultiWRPPSolver extends MultiVehicleSolver<WindyVertex, WindyEdge, WindyGraph> {

    private static final Logger LOGGER = Logger.getLogger(MultiWRPPSolver.class);
    private WindyGraph mGraph;
    private String mInstanceName;
    private double bestWeight;
    private GraphDisplay mDisplay;

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public MultiWRPPSolver(Problem<WindyVertex, WindyEdge, WindyGraph> instance, String instanceName) {
        this(instance, instanceName, null);
    }

    public MultiWRPPSolver(Problem<WindyVertex, WindyEdge, WindyGraph> instance, String instanceName, GraphDisplay display) {
        super(instance);
        mGraph = mInstance.getGraph();
        mInstanceName = instanceName;
        mDisplay = display;
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
    protected Collection<Route<WindyVertex, WindyEdge>> solve() {

        double bestObj = Integer.MAX_VALUE;
        Collection<Route<WindyVertex, WindyEdge>> record = new ArrayList<Route<WindyVertex, WindyEdge>>();

        try {

            //partition
            HashMap<Integer, Integer> sol = partition(null);
            HashMap<Integer, Integer> bestSol = new HashMap<Integer, Integer>();

            ArrayList<Route<WindyVertex, WindyEdge>> ans = new ArrayList<Route<WindyVertex, WindyEdge>>();
            double maxCost;
            Pair<Double> bounds = calculateSimpleBounds();
            double upperBound = bounds.getSecond();
            double lowerBound = bounds.getFirst();
            double numRuns = 10;
            double interval = (upperBound - lowerBound) / numRuns;
            double numSolPerWeight = 5;
            double currWeightBest;

            String outputFile = "/Users/oliverlum/Desktop/100runs_" + mInstanceName + ".txt";
            PrintWriter pw = new PrintWriter(outputFile, "UTF-8");

            //For the closest edge rebalancer
            int n = mGraph.getVertices().size();
            int[][] dist = new int[n+1][n+1];
            int[][] path = new int[n+1][n+1];
            CommonAlgorithms.fwLeastCostPaths(mGraph, dist,path);

            for (int j = 1; j <= numRuns; j++) {
                currWeightBest = Double.MAX_VALUE;
                for (int k = 1; k <= numSolPerWeight; k++) {

                    //new beta stuff
                    ClosestRequiredEdgeRebalancer<WindyGraph> beta = new ClosestRequiredEdgeRebalancer<WindyGraph>(mGraph, new WindyGraphFactory(), .9, new IndividualDistanceToDepotRebalancer(mGraph, lowerBound + j * interval));
                    beta.setDistMatrix(dist);
                    sol = partition(new DuplicateEdgeCostRebalancer(mGraph, beta));

                    mGraph = mInstance.getGraph();

                    HashMap<Integer, HashSet<Integer>> partitions = new HashMap<Integer, HashSet<Integer>>();

                    for (Integer i : sol.keySet()) {
                        if (!partitions.containsKey(sol.get(i)))
                            partitions.put(sol.get(i), new HashSet<Integer>());
                        partitions.get(sol.get(i)).add(i);
                    }

                    //now create the subgraphs
                    ans.clear();
                    for (Integer part : partitions.keySet()) {
                        if (partitions.get(part).isEmpty())
                            continue;
                        ans.add(route(partitions.get(part)));
                    }

                    //improvement
                    OnePassBenaventIPFramework improver = new OnePassBenaventIPFramework(mInstance, ans);
                    Collection<Route<WindyVertex, WindyEdge>> improved = improver.improveSolution();

                    maxCost = mInstance.getObjectiveFunction().evaluate(improved);
                    if (maxCost < currWeightBest) {
                        currWeightBest = maxCost;
                    }

                    //record keeping
                    if (maxCost < bestObj) {
                        bestObj = maxCost;
                        bestSol = sol;
                        record = improved;
                        bestWeight = lowerBound + j * interval;
                    }
                }
                pw.println((lowerBound + j * interval) + "," + currWeightBest + ";");
            }

            if (mDisplay != null) {
                mDisplay.setLayout(GraphDisplay.Layout.YifanHu);
                mDisplay.setGraph(mGraph);
                mDisplay.setInstanceName(mInstanceName);
                mDisplay.exportWithPartition(GraphDisplay.ExportType.PDF, bestSol);

                for (Route<WindyVertex, WindyEdge> r : record) {
                    try {
                        mDisplay.setInstanceName(mInstance.getName() + "_" + r.getCost());
                        mDisplay.exportRoute(GraphDisplay.ExportType.PDF, r);

                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            }


            mInstance.setSol(record);

            LOGGER.info("For instance " + mInstanceName + ", the best weight was " + bestWeight);
            pw.close();
            return record;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Pair<Double> calculateSimpleBounds() {
        double idealLowerBound = .5;
        double idealUpperBound = 1.5;

        int k = mInstance.getmNumVehicles();
        int Er = 0;
        for (WindyEdge we : mGraph.getEdges())
            if (we.isRequired())
                Er++;

        double lowerBound = idealLowerBound * 2 * k / Er;
        double upperBound = idealUpperBound * 2 * k / Er;

        return new Pair<Double>(lowerBound, upperBound);
    }


    /**
     * method to calculate the bounds for the search space of the 'alpha' parameter
     *
     * @return - a pair of numbers (lower bound, upper bound) based on our computational experiments
     */
    private Pair<Double> calculateBounds() {

        //calculate shortest paths
        int n = mGraph.getVertices().size();
        int[] dist = new int[n + 1];
        int[] path = new int[n + 1];
        CommonAlgorithms.dijkstrasAlgorithm(mGraph, mGraph.getDepotId(), dist, path);

        double totalEdgeCost = 0;
        double totalAddedCost = 0;

        int higherCost;
        for (WindyEdge tempEdge : mGraph.getEdges()) {
            totalEdgeCost += (tempEdge.getCost() + tempEdge.getReverseCost()) * .5;
        }

        double lowerBound = 0;
        double upperBound = 0;
        double currentWeight = .001;

        switch (mInstance.getmNumVehicles()) {
            case 3:
                while (totalAddedCost / totalEdgeCost < .1) {
                    totalAddedCost = 0;
                    for (WindyEdge tempEdge : mGraph.getEdges()) {
                        higherCost = Math.max(dist[tempEdge.getEndpoints().getFirst().getId()], dist[tempEdge.getEndpoints().getSecond().getId()]);
                        totalAddedCost += (int) (currentWeight * higherCost);
                    }
                    currentWeight += .001;
                }
                lowerBound = Math.max(0, currentWeight - .02);
                upperBound = lowerBound + .04;
                return new Pair<Double>(lowerBound, upperBound);

            case 5:
                while (totalAddedCost / totalEdgeCost < .2) {
                    totalAddedCost = 0;
                    for (WindyEdge tempEdge : mGraph.getEdges()) {
                        higherCost = Math.max(dist[tempEdge.getEndpoints().getFirst().getId()], dist[tempEdge.getEndpoints().getSecond().getId()]);
                        totalAddedCost += (int) (currentWeight * higherCost);
                    }
                    currentWeight += .001;
                }
                lowerBound = Math.max(0, currentWeight - .02);
                upperBound = lowerBound + .04;
                return new Pair<Double>(lowerBound, upperBound);

            case 10:
                while (totalAddedCost / totalEdgeCost < .333333) {
                    totalAddedCost = 0;
                    for (WindyEdge tempEdge : mGraph.getEdges()) {
                        higherCost = Math.max(dist[tempEdge.getEndpoints().getFirst().getId()], dist[tempEdge.getEndpoints().getSecond().getId()]);
                        totalAddedCost += (int) (currentWeight * higherCost);
                    }
                    currentWeight += .001;
                }
                lowerBound = Math.max(0, currentWeight - .02);
                upperBound = lowerBound + .04;
                return new Pair<Double>(lowerBound, upperBound);

            default:
                return new Pair<Double>(0.0, .04);
        }

    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.MULTI_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String getSolverName() {
        return "Min-Max Windy Rural Postman Problem Solver";
    }

    @Override
    public MultiWRPPSolver instantiate(Problem<WindyVertex, WindyEdge, WindyGraph> p) {
        return new MultiWRPPSolver(p, p.getName(), mDisplay);
    }

    @Override
    public HashMap<String, Double> getProblemParameters() {
        HashMap<String, Double> ret = new HashMap<String, Double>();
        ret.put("Best Weight", new Double(bestWeight));
        return ret;
    }

    protected HashMap<Integer, Integer> partition(CostRebalancer costRebalancer) {

        try {

            //initialize transformer for turning edge-weighted grpah into vertex-weighted graph
            PreciseWindyKWayPartitionTransform transformer = new PreciseWindyKWayPartitionTransform(mGraph, true, costRebalancer);

            //transform the graph
            WindyGraph vWeightedTest = transformer.transformGraph();

            String filename = "C:\\Users\\Oliver\\Desktop\\RandomGraph.graph";

            //write it to a file
            ProblemWriter gw = new ProblemWriter(ProblemFormat.Name.METIS);
            gw.writeInstance(new PartitioningProblem(vWeightedTest, null, null), filename);

            //num parts to partition into
            int numParts = mInstance.getmNumVehicles();

            //partition the graph
            runMetis(numParts, filename);

            //now read the partition and reconstruct the induced subrgraphs on which we solve the WPP to get our final solution
            PartitionReader pr = new PartitionReader(PartitionFormat.Name.METIS);

            return pr.readPartition(filename + ".part." + numParts);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    protected Route route(HashSet<Integer> ids) {

        //check out a clean instance
        WindyGraph mGraph = mInstance.getGraph();

        WindyGraphFactory wgf = new WindyGraphFactory();
        EdgeInducedRequirementTransform<WindyGraph> subgraphTransform = new EdgeInducedRequirementTransform<WindyGraph>(mGraph, wgf, ids);

        //check to make sure we have at least 1 required edge
        TIntObjectHashMap<WindyEdge> mEdges = mGraph.getInternalEdgeMap();
        boolean hasReq = false;
        for (Integer i : ids) {
            if (mEdges.get(i).isRequired())
                hasReq = true;
        }
        if (!hasReq)
            return new Tour();

        WindyGraph subgraph = subgraphTransform.transformGraph();

        //now solve the WPP on it
        WindyRPP subInstance = new WindyRPP(subgraph, mInstanceName);
        WRPPSolver_Benavent_H1 solver = new WRPPSolver_Benavent_H1(subInstance, false);

        long start, end;
        start = System.currentTimeMillis();
        Route ret = solver.solve().iterator().next();
        end = System.currentTimeMillis();
        LOGGER.debug("It took " + (end - start) + " milliseconds to run the sub-solver.");

        return Utils.reclaimTour(ret, mGraph);
    }

    @Override
    public String printCurrentSol() throws IllegalStateException {

        Collection<Route<WindyVertex, WindyEdge>> currSol = mInstance.getSol();

        if (currSol == null)
            LOGGER.error("It does not appear as though this solver has been run yet!", new IllegalStateException());

        int tempCost;
        int numZeroRoutes = 0;
        int totalCost = 0;
        int minLength = Integer.MAX_VALUE;
        int maxLength = Integer.MIN_VALUE;
        double percentVariance, averageCost, averageCostNoEmpty;
        double deviationFromAverage, deviationFromAverageNoEmpty;
        int addedCost = 0;
        int origTotalCost;

        for (WindyEdge l : mInstance.getGraph().getEdges())
            addedCost -= (l.getCost() + l.getReverseCost()) / 2;

        origTotalCost = -1 * addedCost;


        String ans = "=======================================================";
        ans += "\n";
        ans += "\n";
        ans += this.getSolverName() + ": Printing current solution for instance " + mInstanceName + "...";
        ans += "\n";
        ans += "\n";
        ans += "=======================================================";
        for (Route<WindyVertex, WindyEdge> r : currSol) {
            //gather metrics
            tempCost = r.getCost();

            if (tempCost == 0)
                numZeroRoutes++;

            if (tempCost < minLength)
                minLength = tempCost;

            if (tempCost > maxLength)
                maxLength = tempCost;

            totalCost += tempCost;

            ans += "\n";
            ans += "Route: " + r.toString() + "\n";
            ans += "Route Cost: " + tempCost + "\n";
            ans += "Route Required Cost: " + r.getReqCost() + "\n";
            ans += "Route Unrequired Cost: " + (tempCost - r.getReqCost()) + "\n";
            ans += "\n";
        }

        percentVariance = ((double) maxLength - minLength) / maxLength;
        averageCost = (double) totalCost / currSol.size();
        averageCostNoEmpty = (double) totalCost / (currSol.size() - numZeroRoutes);
        deviationFromAverage = ((double) maxLength - averageCost) / maxLength;
        deviationFromAverageNoEmpty = ((double) maxLength - averageCostNoEmpty) / maxLength;
        addedCost += totalCost;

        WindyRPP tempInstance = new WindyRPP(mInstance.getGraph());
        WRPPSolver_Benavent_H1 tempSolver = new WRPPSolver_Benavent_H1(tempInstance);
        int oneVObjective = tempSolver.solve().iterator().next().getCost();
        int totalCostShare = origTotalCost / mInstance.getmNumVehicles();
        int solutionCostShare = oneVObjective / mInstance.getmNumVehicles();

        ans += "=======================================================";
        ans += "\n";
        ans += "\n";
        ans += "Vertices: " + mInstance.getGraph().getVertices().size() + "\n";
        ans += "Edges: " + mInstance.getGraph().getEdges().size() + "\n";
        ans += "Total Edge Cost: " + origTotalCost + "\n";
        ans += "Total Edge Cost / num vehicles (Total Cost Share): " + totalCostShare + "\n";
        ans += "Cost of 1-Vehicle Solution: " + oneVObjective + "\n";
        ans += "Cost of 1-Vehicle Solution / num vehicles (Solution Cost Share): " + solutionCostShare + "\n";
        ans += "Max Route Length: " + maxLength + "\n";
        ans += "Max Route Length / Total Cost Share: " + (double) maxLength / totalCostShare + "\n";
        ans += "Max Route Length / Solution Cost Share: " + (double) maxLength / solutionCostShare + "\n";
        ans += "Min Route Length: " + minLength + "\n";
        ans += "Average Route Length: " + averageCost + "\n";
        ans += "Average RouteLength (excluding empty): " + averageCostNoEmpty + "\n";
        ans += "% variance: " + 100.0 * percentVariance + "\n";
        ans += "% deviation from average length: " + 100.0 * deviationFromAverage + "\n";
        ans += "% deviation from average length (excluding empty): " + 100.0 * deviationFromAverageNoEmpty + "\n";
        ans += "Added cost: " + addedCost + "\n";
        ans += "ROI: " + new RouteOverlapMetric(mGraph).evaluate(currSol) + "\n";
        ans += "ATD: " + new AverageTraversalMetric(mGraph).evaluate(currSol) + "\n";
        ans += "Best Weight: " + bestWeight + "\n";
        ans += "\n";
        ans += "\n";
        ans += "=======================================================";

        return ans;
    }
}
