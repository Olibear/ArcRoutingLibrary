package oarlib.solver.impl;

import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
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
import oarlib.graph.transform.rebalance.CostRebalancer;
import oarlib.graph.transform.rebalance.impl.DuplicateEdgeCostRebalancer;
import oarlib.graph.transform.rebalance.impl.IndividualDistanceToDepotRebalancer;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.improvements.ImprovementProcedure;
import oarlib.improvements.impl.*;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.MultiVehicleProblem;
import oarlib.problem.impl.multivehicle.MultiVehicleWRPP;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.route.impl.Tour;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 8/14/14.
 */
public class MultiWRPPSolver extends MultiVehicleSolver<WindyVertex, WindyEdge> {

    private MultiVehicleWRPP mInstance;
    private WindyGraph mGraph;
    private String mInstanceName;
    private boolean lastRun; // to control when to export

    private static final Logger LOGGER = Logger.getLogger(MultiWRPPSolver.class);

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public MultiWRPPSolver(MultiVehicleWRPP instance, String instanceName) throws IllegalArgumentException {
        super(instance);
        mInstance = instance;
        mGraph = mInstance.getGraph();
        mInstanceName = instanceName;
        lastRun = false;
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
    protected Collection<Route<WindyVertex, WindyEdge>> solve() {

        int bestObj = Integer.MAX_VALUE;
        ArrayList<Route<WindyVertex, WindyEdge>> record = new ArrayList<Route<WindyVertex, WindyEdge>>();

        try {

            //partition
            int reqCounter = 0;
            for (WindyEdge we : mGraph.getEdges()) {
                if (we.isRequired())
                    reqCounter++;
            }
            HashMap<Integer, Integer> sol = partition(null);
            HashMap<Integer, Integer> bestSol = new HashMap<Integer, Integer>();

            ArrayList<Route<WindyVertex, WindyEdge>> ans = new ArrayList<Route<WindyVertex, WindyEdge>>();
            int maxCost = 0;
            int numRebalances = 2;
            for (int j = 1; j <= numRebalances; j++) {

                if (j == numRebalances)
                    lastRun = true;
                mGraph = mInstance.getGraph();

                HashMap<Integer, HashSet<Integer>> partitions = new HashMap<Integer, HashSet<Integer>>();
                TIntObjectHashMap<WindyEdge> mGraphEdges = mGraph.getInternalEdgeMap();
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

                //try improve
                ArrayList<Route<WindyVertex, WindyEdge>> toImprove = new ArrayList<Route<WindyVertex,WindyEdge>>();
                Route temp;
                for(Route r: ans) {
                    toImprove.add(WRPPSolver_Win.reclaimTour(r, mGraph));
                }
                Change1to1 ip = new Change1to1(mGraph, toImprove);
                Collection<Route<WindyVertex, WindyEdge>> improved = ip.improveSolution();

                //DEBUG: display routes
                int routeCounter = 1;
                int minCost = Integer.MAX_VALUE;
                int tempCost;
                int numReqLinks;

                maxCost = 0;
                for (Route r : ans) {
                    tempCost = r.getCost();
                    if (tempCost > maxCost)
                        maxCost = tempCost;
                    if (tempCost < minCost)
                        minCost = tempCost;
                }

                if (maxCost < bestObj) {
                    bestObj = maxCost;
                    bestSol = sol;
                    record = ans;
                }

                sol = partition(new DuplicateEdgeCostRebalancer(mGraph, new IndividualDistanceToDepotRebalancer(mGraph, .01)));
            }

            GraphDisplay gd = new GraphDisplay(GraphDisplay.Layout.YifanHu, mGraph, mInstanceName);
            gd.exportWithPartition(GraphDisplay.ExportType.PDF, bestSol);

            currSol = record;
            return record;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    protected HashMap<Integer, Integer> partition(CostRebalancer costRebalancer) {

        try {

            //initialize transformer for turning edge-weighted grpah into vertex-weighted graph
            PreciseWindyKWayPartitionTransform transformer = new PreciseWindyKWayPartitionTransform(mGraph, true, costRebalancer);

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
        WRPPSolver_Benavent_H1 solver = new WRPPSolver_Benavent_H1(subInstance, lastRun);

        long start, end;
        start = System.currentTimeMillis();
        Route ret = solver.solve();
        end = System.currentTimeMillis();
        LOGGER.debug("It took " + (end - start) + " milliseconds to run the sub-solver.");

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
        ans += "CapacitatedWRPPSolver: Printing current solution for instance " + mInstanceName + "...";
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
