package oarlib.solver.impl;

import oarlib.core.*;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.graph.io.GraphFormat;
import oarlib.graph.io.GraphWriter;
import oarlib.graph.io.PartitionFormat;
import oarlib.graph.io.PartitionReader;
import oarlib.graph.transform.impl.UndirectedKWayPartitionTransform;
import oarlib.problem.impl.CapacitatedUCPP;

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
    protected CapacitatedUCPPSolver(CapacitatedUCPP instance) throws IllegalArgumentException {
        super(instance);
        mInstance = instance;
    }

    @Override
    protected boolean checkGraphRequirements() {
        return false;
    }

    @Override
    protected CapacitatedUCPP getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<Route> solve() {

        try {

            UndirectedGraph mGraph = mInstance.getGraph();
            UndirectedKWayPartitionTransform transformer = new UndirectedKWayPartitionTransform(mGraph);
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
            HashMap<Integer, Integer> sol = pr.readPartition(filename + ".part." + numParts);

            int m = vWeightedTest.getEdges().size();
            HashMap<Integer, Edge> mGraphEdges = mGraph.getInternalEdgeMap();
            Edge temp;
            int firstId, secondId;
            HashMap<Integer, Integer> edgeSol = new HashMap<Integer, Integer>();
            double prob;

            //for each edge, figure out if it's internal, or part of the cut induced by the partition
            for (int i = 1; i <= m; i++) {
                temp = mGraphEdges.get(i);
                firstId = temp.getEndpoints().getFirst().getId();
                secondId = temp.getEndpoints().getSecond().getId();

                //if it's internal, just
                if(sol.get(firstId) == sol.get(secondId)) {
                    edgeSol.put(i, sol.get(firstId));
                }
                //oth. with 50% probability, stick it in either one
                else {
                    prob = Math.random();
                    if(prob > .5)
                        edgeSol.put(i, sol.get(firstId));
                    else
                        edgeSol.put(i, sol.get(secondId));
                }
            }

            //now create the subgraphs

            return null;
        } catch (Exception e)
        {
            e.printStackTrace();;
            return null;
        }
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.UNDIRECTED_CHINESE_POSTMAN;
    }
}
