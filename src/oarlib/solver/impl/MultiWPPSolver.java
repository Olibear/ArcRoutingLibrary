package oarlib.solver.impl;

import oarlib.core.*;
import oarlib.display.GraphDisplay;
import oarlib.graph.factory.impl.WindyGraphFactory;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.io.GraphFormat;
import oarlib.graph.io.GraphWriter;
import oarlib.graph.io.PartitionFormat;
import oarlib.graph.io.PartitionReader;
import oarlib.graph.transform.impl.EdgeInducedRequirementTransform;
import oarlib.graph.transform.partition.impl.PreciseWindyKWayPartitionTransform;
import oarlib.graph.transform.rebalance.impl.ShortRouteReductionRebalancer;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.problem.impl.MultiVehicleWPP;
import oarlib.problem.impl.WindyRPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 8/14/14.
 */
public class MultiWPPSolver extends MultiVehicleSolver {

    MultiVehicleWPP mInstance;
    WindyGraph mGraph;
    String mInstanceName;

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public MultiWPPSolver(MultiVehicleWPP instance, String instanceName) throws IllegalArgumentException {
        super(instance);
        mInstance = instance;
        mGraph = mInstance.getGraph();
        mInstanceName = instanceName;
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
    protected MultiVehicleProblem getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<Route> solve() {

        int bestObj = Integer.MAX_VALUE;
        ArrayList<Route> record = new ArrayList<Route>();

        try {

            //partition
            int reqCounter = 0;
            for (WindyEdge we : mGraph.getEdges()) {
                if (we.isRequired())
                    reqCounter++;
            }
            HashMap<Integer, Integer> sol = partition();

            GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, mGraph, mInstanceName);
            gd.exportWithPartition(GraphDisplay.ExportType.PDF, sol);

            ArrayList<Route> ans = new ArrayList<Route>();
            int maxCost = 0;
            for (int j = 1; j <= 5; j++) {

                mGraph = mInstance.getGraph();

                HashMap<Integer, HashSet<Integer>> partitions = new HashMap<Integer, HashSet<Integer>>();
                HashMap<Integer, WindyEdge> mGraphEdges = mGraph.getInternalEdgeMap();
                HashSet<Integer> nonReqEdges = new HashSet<Integer>();

                for (Integer i : sol.keySet()) {
                    if (!partitions.containsKey(sol.get(i)))
                        partitions.put(sol.get(i), new HashSet<Integer>());
                    partitions.get(sol.get(i)).add(i);
                    if (!mGraphEdges.get(i).isRequired())
                        nonReqEdges.add(i);

                }

                //now create the subgraphs
                ans.clear();
                for (Integer part : partitions.keySet()) {
                    if (partitions.get(part).isEmpty())
                        continue;
                    //put in all the non-required ones
                    for (Integer id : nonReqEdges) {
                        partitions.get(part).add(id);
                    }

                    ans.add(route(partitions.get(part)));
                }

                //DEBUG: display routes
                int routeCounter = 1;
                int minCost = Integer.MAX_VALUE;
                int tempCost;
                int numReqLinks;

                for (Route r : ans) {
                    tempCost = r.getCost();
                    if (tempCost > maxCost)
                        maxCost = tempCost;
                    if (tempCost < minCost)
                        minCost = tempCost;
                }

                if (maxCost < bestObj) {
                    bestObj = maxCost;
                    record = ans;

                }

                ShortRouteReductionRebalancer<WindyGraph> rebalancer = new ShortRouteReductionRebalancer<WindyGraph>(mGraph, sol, ans);
                mGraph = rebalancer.transformGraph(1 - (j * .1));
                sol = partition();
            }

            currSol = record;
            return record;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_CHINESE_POSTMAN;
    }

    @Override
    protected HashMap<Integer, Integer> partition() {

        try {

            //initialize transformer for turning edge-weighted grpah into vertex-weighted graph
            PreciseWindyKWayPartitionTransform transformer = new PreciseWindyKWayPartitionTransform(mGraph, true);

            //transform the graph
            WindyGraph vWeightedTest = transformer.transformGraph();

            String filename = "/Users/oliverlum/Desktop/RandomGraph.graph";

            //write it to a file
            GraphWriter gw = new GraphWriter(GraphFormat.Name.METIS);
            gw.writeGraph(vWeightedTest, filename);

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

    @Override
    protected Route route(HashSet<Integer> ids) {

        //check out a clean instance
        WindyGraph mGraph = mInstance.getGraph();

        WindyGraphFactory wgf = new WindyGraphFactory();
        EdgeInducedRequirementTransform<WindyGraph> subgraphTransform = new EdgeInducedRequirementTransform<WindyGraph>(mGraph, wgf, ids);

        //check to make sure we have at least 1 required edge
        HashMap<Integer, WindyEdge> mEdges = mGraph.getInternalEdgeMap();
        boolean hasReq = false;
        for (Integer i : ids) {
            if (mEdges.get(i).isRequired())
                hasReq = true;
        }
        if (!hasReq)
            return new Tour();

        WindyGraph subgraph = subgraphTransform.transformGraph();

        //now solve the WPP on it
        WindyRPP subInstance = new WindyRPP(subgraph);
        WRPPSolver_Benavent_H1 solver = new WRPPSolver_Benavent_H1(subInstance);

        long start, end;
        start = System.currentTimeMillis();
        Route ret = solver.solve();
        end = System.currentTimeMillis();
        System.out.println("It took " + (end - start) + " milliseconds to run the sub-solver.");

        //set the id map for the route
        int n = subgraph.getVertices().size();
        HashMap<Integer, WindyVertex> indexedVertices = subgraph.getInternalVertexMap();
        HashMap<Integer, Integer> customIDMap = new HashMap<Integer, Integer>();
        for (int i = 1; i <= n; i++) {
            customIDMap.put(i, indexedVertices.get(i).getMatchId());
        }
        ret.setMapping(customIDMap);

        return ret;
    }

    @Override
    public String printCurrentSol() throws IllegalStateException {
        if (currSol == null)
            throw new IllegalStateException("It does not appear as though this solver has been run yet!");

        int tempCost;
        int numZeroRoutes = 0;
        int totalCost = 0;
        int minLength = Integer.MAX_VALUE;
        int maxLength = Integer.MIN_VALUE;
        double percentVariance, averageCost, averageCostNoEmpty;
        double deviationFromAverage, deviationFromAverageNoEmpty;
        int addedCost = 0;
        int origTotalCost;

        for (Link l : mInstance.getGraph().getEdges())
            addedCost -= l.getCost();

        origTotalCost = -1 * addedCost;


        String ans = "=======================================================";
        ans += "\n";
        ans += "\n";
        ans += "CapacitatedWRPPSolver: Printing current solution...";
        ans += "\n";
        ans += "\n";
        ans += "=======================================================";
        for (Route r : currSol) {
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
        int oneVObjective = tempSolver.solve().getCost();
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
        ans += "\n";
        ans += "\n";
        ans += "=======================================================";

        return ans;
    }
}
