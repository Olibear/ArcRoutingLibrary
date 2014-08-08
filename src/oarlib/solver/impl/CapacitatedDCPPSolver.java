package oarlib.solver.impl;

import oarlib.core.*;
import oarlib.graph.factory.impl.DirectedGraphFactory;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.io.GraphFormat;
import oarlib.graph.io.GraphWriter;
import oarlib.graph.io.PartitionFormat;
import oarlib.graph.io.PartitionReader;
import oarlib.graph.transform.impl.DirectedKWayPartitionTransform;
import oarlib.graph.transform.impl.EdgeInducedSubgraphTransform;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.problem.impl.CapacitatedDCPP;
import oarlib.problem.impl.DirectedCPP;
import oarlib.vertex.impl.DirectedVertex;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by oliverlum on 8/5/14.
 */
public class CapacitatedDCPPSolver extends CapacitatedVehicleSolver {

    CapacitatedDCPP mInstance;
    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    public CapacitatedDCPPSolver(CapacitatedDCPP instance) throws IllegalArgumentException {
        super(instance);
        mInstance = instance;
    }

    @Override
    protected boolean checkGraphRequirements() {
        // make sure the graph is connected
        if(mInstance.getGraph() == null)
            return false;
        else
        {
            DirectedGraph mGraph = mInstance.getGraph();
            if(!CommonAlgorithms.isStronglyConnected(mGraph))
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
            DirectedGraph mGraph = mInstance.getGraph();
            HashMap<Integer, Integer> sol = partition();

            //initialize vars
            int firstId, secondId;
            int m = mGraph.getEdges().size();
            double prob;
            Arc temp;
            HashMap<Integer, Arc> mGraphArcs = mGraph.getInternalEdgeMap();
            HashMap<Integer, Integer> arcSol = new HashMap<Integer, Integer>();
            HashMap<Integer, HashSet<Integer>> partitions = new HashMap<Integer, HashSet<Integer>>();
            HashSet<Integer> valueSet = new HashSet<Integer>(sol.values());

            for(Integer part: valueSet)
            {
                partitions.put(part, new HashSet<Integer>());
            }

            //for each arc, figure out if it's internal, or part of the cut induced by the partition
            for(int i = 1; i <= m; i++) {
                temp = mGraphArcs.get(i);
                firstId = temp.getTail().getId();
                secondId = temp.getHead().getId();

                //if it's internal or to the depot, just log the arc in the appropriate partition
                if(sol.get(firstId) == sol.get(secondId) || firstId == mGraph.getDepotId() || secondId == mGraph.getDepotId())
                {
                    arcSol.put(i, sol.get(firstId));
                    partitions.get(sol.get(firstId)).add(i);
                }
                //oth. with 50% probability, stick it in either one
                else {
                    prob = Math.random();
                    if(prob > .5) {
                        arcSol.put(i, sol.get(firstId));
                        partitions.get(sol.get(firstId)).add(i);
                    }
                    else {
                        arcSol.put(i, sol.get(secondId));
                        partitions.get(sol.get(secondId)).add(i);
                    }
                }

            }

            //now create the subgraphs
            HashSet<Route> ans = new HashSet<Route>();
            for(Integer part: partitions.keySet())
            {
                ans.add(route(partitions.get(part)));
            }

            return ans;
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.DIRECTED_CHINESE_POSTMAN;
    }

    @Override
    protected HashMap<Integer, Integer> partition() {

        try {

            //initialize transformer for turning edge-weighted graph into vertex-weighted graph
            DirectedGraph mGraph = mInstance.getGraph();
            DirectedKWayPartitionTransform transformer = new DirectedKWayPartitionTransform(mGraph);

            //transform the graph
            DirectedGraph vWeightedTest = transformer.transformGraph();

            String filename = "/Users/oliverlum/Desktop/RandomGraph.graph";

            //write it to a file
            GraphWriter gw = new GraphWriter(GraphFormat.Name.METIS);
            gw.writeGraph(vWeightedTest, filename);

            //num parts to partition into
            int numParts = mInstance.getmNumVehicles();

            //partition the graph
            runMetis(numParts, filename);

            //now read the partition and reconstruct the induced subgraphs on which we solve the DCPP to get our final solution
            PartitionReader pr = new PartitionReader(PartitionFormat.Name.METIS);

            return pr.readPartition(filename + ".part." + numParts);

        } catch (Exception e) {

            e.printStackTrace();
            return null;

        }
    }

    @Override
    protected Route route(HashSet<Integer> ids) {

        DirectedGraph mGraph = mInstance.getGraph();

        DirectedGraphFactory dgf = new DirectedGraphFactory();
        EdgeInducedSubgraphTransform<DirectedGraph> subgraphTransform = new EdgeInducedSubgraphTransform<DirectedGraph>(mGraph, dgf, null, true);

        subgraphTransform.setEdges(ids);
        DirectedGraph subgraph = subgraphTransform.transformGraph();

        //now solve the DCPP on it
        DirectedCPP subInstance = new DirectedCPP(subgraph);
        DCPPSolver_Edmonds solver = new DCPPSolver_Edmonds(subInstance);
        Route ret = solver.solve();

        //set the id map for the route
        int n = subgraph.getVertices().size();
        HashMap<Integer, DirectedVertex> indexedVertices = subgraph.getInternalVertexMap();
        HashMap<Integer, Integer> customIDMap = new HashMap<Integer, Integer>();
        for(int i = 1; i <= n; i++)
        {
            customIDMap.put(i, indexedVertices.get(i).getMatchId());
        }
        ret.setMapping(customIDMap);
        return ret;

    }
}
