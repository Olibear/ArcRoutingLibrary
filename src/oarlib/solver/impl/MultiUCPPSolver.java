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

import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import oarlib.core.*;
import oarlib.graph.factory.impl.UndirectedGraphFactory;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.io.GraphFormat;
import oarlib.graph.io.GraphWriter;
import oarlib.graph.io.PartitionFormat;
import oarlib.graph.io.PartitionReader;
import oarlib.graph.transform.impl.EdgeInducedSubgraphTransform;
import oarlib.graph.transform.partition.impl.PreciseUndirectedKWayPartitionTransform;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.Edge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.cpp.UndirectedCPP;
import oarlib.vertex.impl.UndirectedVertex;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Oliver Lum on 7/25/2014.
 */
public class MultiUCPPSolver extends MultiVehicleSolver<UndirectedVertex, Edge, UndirectedGraph> {

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public MultiUCPPSolver(Problem<UndirectedVertex, Edge, UndirectedGraph> instance) throws IllegalArgumentException {
        super(instance);
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
    protected Problem<UndirectedVertex, Edge, UndirectedGraph> getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<Route<UndirectedVertex, Edge>> solve() {

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


            HashSet<Route<UndirectedVertex, Edge>> ans = new HashSet<Route<UndirectedVertex, Edge>>();
            //now create the subgraphs
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
        return new ProblemAttributes(Graph.Type.UNDIRECTED, ProblemAttributes.Type.CHINESE_POSTMAN, ProblemAttributes.NumVehicles.MULTI_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String getSolverName() {
        return "Min-Max K Undirected Chinese Postman Solver";
    }

    @Override
    public Solver<UndirectedVertex, Edge, UndirectedGraph> instantiate(Problem<UndirectedVertex, Edge, UndirectedGraph> p) {
        return null;
    }

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
            return pr.readPartition(filename + ".part." + numParts);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

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

        Route ret = solver.solve().iterator().next();

        //set the id map for the route
        int n = subgraph.getVertices().size();
        TIntObjectHashMap<UndirectedVertex> indexedVertices = subgraph.getInternalVertexMap();
        TIntIntHashMap customIDMap = new TIntIntHashMap();
        for (int i = 1; i <= n; i++) {
            customIDMap.put(i, indexedVertices.get(i).getMatchId());
        }
        ret.setMapping(customIDMap);

        return ret;
    }
}
