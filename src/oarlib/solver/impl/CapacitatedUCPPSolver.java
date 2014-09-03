package oarlib.solver.impl;

import oarlib.core.CapacitatedVehicleSolver;
import oarlib.core.Edge;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.factory.impl.UndirectedGraphFactory;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.io.GraphFormat;
import oarlib.graph.io.GraphWriter;
import oarlib.graph.io.PartitionFormat;
import oarlib.graph.io.PartitionReader;
import oarlib.graph.transform.impl.EdgeInducedSubgraphTransform;
import oarlib.graph.transform.partition.impl.UndirectedKWayPartitionTransform;
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

            /*
             * initialize vars
             *
             * firstId, secondId - we're going to iterate through the edges, and figure out which partition to put them in.
             * Since we solved a vertex partitioning problem, we need to try and recover the edge partition.  These are the ids of
             * the vertex endpoints
             *
             * m - number of edges in the full graph.
             *
             * prob - random number between 0 and 1 to determine which partition to stick edges in the cut.
             *
             * temp - the edge we're considering right now
             *
             * mGraphEdges - the edge map of the graph
             *
             * edgeSol - key: edge id, value: partition we're placing it in
             *
             * partitions - key: partition #, value: set containing edge ids in this partition
             *
             * valueSet - set of partition numbers
             */

            int firstId, secondId;
            int m = mGraph.getEdges().size();
            double prob;
            Edge temp;
            HashMap<Integer, Edge> mGraphEdges = mGraph.getInternalEdgeMap();
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

            HashSet<Route> ans = new HashSet<Route>();
            //now create the subgraphs
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
            UndirectedKWayPartitionTransform transformer = new UndirectedKWayPartitionTransform(mGraph);

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
}
