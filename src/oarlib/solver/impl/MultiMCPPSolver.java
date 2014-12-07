package oarlib.solver.impl;

import oarlib.core.*;
import oarlib.graph.factory.impl.MixedGraphFactory;
import oarlib.graph.impl.MixedGraph;
import oarlib.graph.io.GraphFormat;
import oarlib.graph.io.GraphWriter;
import oarlib.graph.io.PartitionFormat;
import oarlib.graph.io.PartitionReader;
import oarlib.graph.transform.impl.EdgeInducedSubgraphTransform;
import oarlib.graph.transform.partition.impl.PreciseMixedKWayPartitionTransform;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.MixedEdge;
import oarlib.problem.impl.MultiVehicleProblem;
import oarlib.problem.impl.cpp.MixedCPP;
import oarlib.problem.impl.multivehicle.MultiVehicleMCPP;
import oarlib.vertex.impl.MixedVertex;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 8/12/14.
 */
public class MultiMCPPSolver extends MultiVehicleSolver<MixedVertex, MixedEdge> {

    MultiVehicleMCPP mInstance;

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public MultiMCPPSolver(MultiVehicleMCPP instance) throws IllegalArgumentException {
        super(instance);
        mInstance = instance;
    }

    @Override
    protected boolean checkGraphRequirements() {
        //make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            MixedGraph mixedGraph = mInstance.getGraph();
            if (!CommonAlgorithms.isStronglyConnected(mixedGraph))
                return false;
        }
        return true;
    }

    @Override
    protected MultiVehicleProblem getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<Route<MixedVertex, MixedEdge>> solve() {

        try {

            //partition
            MixedGraph mGraph = mInstance.getGraph();
            HashMap<Integer, Integer> sol = partition();

            HashMap<Integer, HashSet<Integer>> partitions = new HashMap<Integer, HashSet<Integer>>();

            for (Integer i : sol.keySet()) {
                if (!partitions.containsKey(sol.get(i)))
                    partitions.put(sol.get(i), new HashSet<Integer>());
                partitions.get(sol.get(i)).add(i);
            }

            //now create the subgraphs
            HashSet<Route<MixedVertex, MixedEdge>> ans = new HashSet<Route<MixedVertex, MixedEdge>>();
            for (Integer part : partitions.keySet()) {
                ans.add(route(partitions.get(part)));
            }

            currSol = ans;
            return ans;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.MIXED_CHINESE_POSTMAN;
    }

    protected HashMap<Integer, Integer> partition() {

        try {

            //initialize transformer for turning edge-weighted graph into vertex-weighted graph
            MixedGraph mGraph = mInstance.getGraph();
            PreciseMixedKWayPartitionTransform transformer = new PreciseMixedKWayPartitionTransform(mGraph);

            //transform the graph
            MixedGraph vWeightedTest = transformer.transformGraph();

            String filename = "/Users/oliverlum/Desktop/RandomGraph.graph";

            //write it to a file
            GraphWriter gw = new GraphWriter(GraphFormat.Name.METIS);
            gw.writeGraph(vWeightedTest, filename);

            //num parts to partition into
            int numParts = mInstance.getmNumVehicles();

            //partition the graph
            runMetis(numParts, filename);

            //now read the partition and reconstruct the induced subgraphs on which we solve the MCPP to get our final solution
            PartitionReader pr = new PartitionReader(PartitionFormat.Name.METIS);

            return pr.readPartition(filename + ".part." + numParts);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected Route route(HashSet<Integer> ids) {

        MixedGraph mGraph = mInstance.getGraph();

        MixedGraphFactory mgf = new MixedGraphFactory();
        EdgeInducedSubgraphTransform<MixedGraph> subgraphTransform = new EdgeInducedSubgraphTransform<MixedGraph>(mGraph, mgf, null, true);

        subgraphTransform.setEdges(ids);
        MixedGraph subgraph = subgraphTransform.transformGraph();

        //now solve the MCPP on it
        MixedCPP subInstance = new MixedCPP(subgraph);
        MCPPSolver_Frederickson solver = new MCPPSolver_Frederickson(subInstance);
        Route ret = solver.solve();

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

        for (Link l : mInstance.getGraph().getEdges())
            addedCost -= l.getCost();


        String ans = "=======================================================";
        ans += "\n";
        ans += "\n";
        ans += "CapacitatedDCPPSolver: Printing current solution...";
        ans += "\n";
        ans += "\n";
        ans += "=======================================================";
        for (Route<MixedVertex, MixedEdge> r : currSol) {
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


        ans += "=======================================================";
        ans += "\n";
        ans += "\n";
        ans += "Vertices: " + mInstance.getGraph().getVertices().size() + "\n";
        ans += "Edges: " + mInstance.getGraph().getEdges().size() + "\n";
        ans += "Max Route Length: " + maxLength + "\n";
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
