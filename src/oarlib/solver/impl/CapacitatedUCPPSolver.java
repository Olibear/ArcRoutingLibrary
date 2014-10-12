package oarlib.solver.impl;

import oarlib.core.CapacitatedVehicleSolver;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.factory.impl.UndirectedGraphFactory;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.io.GraphFormat;
import oarlib.graph.io.GraphWriter;
import oarlib.graph.io.PartitionFormat;
import oarlib.graph.io.PartitionReader;
import oarlib.graph.transform.impl.EdgeInducedSubgraphTransform;
import oarlib.graph.transform.partition.impl.PreciseUndirectedKWayPartitionTransform;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.problem.impl.CapacitatedUCPP;
import oarlib.problem.impl.UndirectedCPP;
import oarlib.vertex.impl.UndirectedVertex;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Oliver Lum on 7/25/2014.
 */
public class CapacitatedUCPPSolver extends CapacitatedVehicleSolver {

    CapacitatedUCPP mInstance;

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public CapacitatedUCPPSolver(CapacitatedUCPP instance) throws IllegalArgumentException {
        super(instance);
        mInstance = instance;
    }

    @Override
    protected boolean checkGraphRequirements() {

        // make sure the graph is connected
        if (mInstance.getGraph() == null)
            return false;
        else {
            UndirectedGraph mGraph = mInstance.getGraph();
            if (!CommonAlgorithms.isConnected(mGraph))
                return false;
        }
        return true;
    }

    @Override
    protected CapacitatedUCPP getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<Route> solve() {

        try {

            //partition
            UndirectedGraph mGraph = mInstance.getGraph();
            HashMap<Integer, Integer> sol = partition();

            HashMap<Integer, HashSet<Integer>> partitions = new HashMap<Integer, HashSet<Integer>>();

            for (Integer i : sol.keySet()) {
                if (!partitions.containsKey(sol.get(i)))
                    partitions.put(sol.get(i), new HashSet<Integer>());
                partitions.get(sol.get(i)).add(i);
            }


            HashSet<Route> ans = new HashSet<Route>();
            //now create the subgraphs
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
        return Problem.Type.UNDIRECTED_CHINESE_POSTMAN;
    }

    @Override
    protected HashMap<Integer, Integer> partition() {
        try {

            /*
             * Calls the METIS graph partitioning code after applying a transform to the graph to assign
             * vertex weights that represent incident edge weights.
             */

            //initialize transformer for turning edge-weighted graph into vertex-weighted graph
            UndirectedGraph mGraph = mInstance.getGraph();
            PreciseUndirectedKWayPartitionTransform transformer = new PreciseUndirectedKWayPartitionTransform(mGraph);

            //transform the graph
            UndirectedGraph vWeightedTest = transformer.transformGraph();

            String filename = "/Users/oliverlum/Desktop/RandomGraph.graph";

            //write it to a file
            GraphWriter gw = new GraphWriter(GraphFormat.Name.METIS);
            gw.writeGraph(vWeightedTest, filename);

            //num parts to partition into
            int numParts = mInstance.getmNumVehicles();

            //partition the graph
            runMetis(numParts, filename);

            //now read the partition and reconstruct the induced subgraphs on which we solve the UCPP on to get our final solution.
            PartitionReader pr = new PartitionReader(PartitionFormat.Name.METIS);
            //TODO: Make this just an array; no need to have HashMap
            return pr.readPartition(filename + ".part." + numParts);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    @Override
    protected Route route(HashSet<Integer> ids) {

        //grab the graph
        UndirectedGraph mGraph = mInstance.getGraph();

        //transform it
        UndirectedGraphFactory ugf = new UndirectedGraphFactory();
        EdgeInducedSubgraphTransform<UndirectedGraph> subgraphTransform = new EdgeInducedSubgraphTransform<UndirectedGraph>(mGraph, ugf, null, true);

        subgraphTransform.setEdges(ids);
        UndirectedGraph subgraph = subgraphTransform.transformGraph();

        //now solve the UCPP on it
        UndirectedCPP subInstance = new UndirectedCPP(subgraph);
        UCPPSolver_Edmonds solver = new UCPPSolver_Edmonds(subInstance);

        Route ret = solver.solve();

        //set the id map for the route
        int n = subgraph.getVertices().size();
        HashMap<Integer, UndirectedVertex> indexedVertices = subgraph.getInternalVertexMap();
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

        for (Link l : mInstance.getGraph().getEdges())
            addedCost -= l.getCost();


        String ans = "=======================================================";
        ans += "\n";
        ans += "\n";
        ans += "CapacitatedDCPPSolver: Printing current solution...";
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
