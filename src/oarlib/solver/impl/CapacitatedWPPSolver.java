package oarlib.solver.impl;

import oarlib.core.*;
import oarlib.graph.factory.impl.WindyGraphFactory;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.io.GraphFormat;
import oarlib.graph.io.GraphWriter;
import oarlib.graph.io.PartitionFormat;
import oarlib.graph.io.PartitionReader;
import oarlib.graph.transform.impl.EdgeInducedSubgraphTransform;
import oarlib.graph.transform.partition.impl.WindyKWayPartitionTransform;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.problem.impl.CapacitatedWPP;
import oarlib.problem.impl.WindyRPP;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 8/14/14.
 */
public class CapacitatedWPPSolver extends CapacitatedVehicleSolver {

    CapacitatedWPP mInstance;

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public CapacitatedWPPSolver(CapacitatedWPP instance) throws IllegalArgumentException {
        super(instance);
        mInstance = instance;
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
    protected CapacitatedProblem getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<Route> solve() {

        try {

            //partition
            WindyGraph mGraph = mInstance.getGraph();
            HashMap<Integer, Integer> sol = partition();

            //initialize vars
            int firstId, secondId;
            int m = mGraph.getEdges().size();
            double prob;
            WindyEdge temp;
            HashMap<Integer, WindyEdge> mGraphEdges = mGraph.getInternalEdgeMap();
            HashMap<Integer, Integer> edgeSol = new HashMap<Integer, Integer>();
            HashMap<Integer, HashSet<Integer>> partitions = new HashMap<Integer, HashSet<Integer>>();
            HashSet<Integer> valueSet = new HashSet<Integer>(sol.values());

            for (Integer part : valueSet) {
                partitions.put(part, new HashSet<Integer>());
            }

            //for each edge, figure out if it's internal, or part of the cut induced by the partition
            for (int i = 1; i <= m; i++) {
                temp = mGraphEdges.get(i);
                firstId = temp.getEndpoints().getFirst().getId();
                secondId = temp.getEndpoints().getSecond().getId();

                //if it's internal, just log the edge in the appropriate partition
                if (sol.get(firstId).equals(sol.get(secondId)) || secondId == mGraph.getDepotId()) {
                    edgeSol.put(i, sol.get(firstId));
                    partitions.get(sol.get(firstId)).add(i);
                } else if (firstId == mGraph.getDepotId()) {
                    edgeSol.put(i, sol.get(secondId));
                    partitions.get(sol.get(secondId)).add(i);
                }
                //oth. with 50% probability, stick it in either one
                else {
                    prob = Math.random();
                    if (prob > .5) {
                        edgeSol.put(i, sol.get(firstId));
                        partitions.get(sol.get(firstId)).add(i);
                    } else {
                        edgeSol.put(i, sol.get(secondId));
                        partitions.get(sol.get(secondId)).add(i);
                    }
                }
            }

            //now create the subgraphs
            HashSet<Route> ans = new HashSet<Route>();
            for (Integer part : partitions.keySet()) {
                ans.add(route(partitions.get(part)));
            }

            return ans;
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
            WindyGraph mGraph = mInstance.getGraph();
            WindyKWayPartitionTransform transformer = new WindyKWayPartitionTransform(mGraph);

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

        WindyGraph mGraph = mInstance.getGraph();

        WindyGraphFactory wgf = new WindyGraphFactory();
        EdgeInducedSubgraphTransform<WindyGraph> subgraphTransform = new EdgeInducedSubgraphTransform<WindyGraph>(mGraph, wgf, null, true);

        subgraphTransform.setEdges(ids);
        WindyGraph subgraph = subgraphTransform.transformGraph();

        //now solve the WPP on it
        WindyRPP subInstance = new WindyRPP(subgraph);
        WRPPSolver_Benavent_H1 solver = new WRPPSolver_Benavent_H1(subInstance);
        Route ret = solver.solve();
        return ret;
    }
}
