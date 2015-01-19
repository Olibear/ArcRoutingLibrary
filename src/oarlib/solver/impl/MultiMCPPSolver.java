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
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.cpp.MixedCPP;
import oarlib.vertex.impl.MixedVertex;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 8/12/14.
 */
public class MultiMCPPSolver extends MultiVehicleSolver<MixedVertex, MixedEdge, MixedGraph> {

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public MultiMCPPSolver(Problem<MixedVertex, MixedEdge, MixedGraph> instance) throws IllegalArgumentException {
        super(instance);
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
    protected Problem<MixedVertex, MixedEdge, MixedGraph> getInstance() {
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

            mInstance.setSol(ans);
            return ans;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.MIXED, ProblemAttributes.Type.CHINESE_POSTMAN, ProblemAttributes.NumVehicles.MULTI_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String getSolverName() {
        return "Min-Max K Mixed Chinese Postman Problem Solver";
    }

    @Override
    public Solver<MixedVertex, MixedEdge, MixedGraph> instantiate(Problem<MixedVertex, MixedEdge, MixedGraph> p) {
        return new MultiMCPPSolver(p);
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
        Route ret = solver.solve().iterator().next();

        return ret;
    }
}
