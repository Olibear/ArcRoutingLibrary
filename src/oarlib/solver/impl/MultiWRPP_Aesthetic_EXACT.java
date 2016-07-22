package oarlib.solver.impl;

import oarlib.core.*;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.vertex.impl.WindyVertex;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by oliverlum on 5/10/16.
 */
public class MultiWRPP_Aesthetic_EXACT extends MultiVehicleSolver<WindyVertex, WindyEdge, WindyGraph> {

    /**
     * Default constructor; must set problem instance.
     *
     * @param instance - instance for which this is a solver
     */
    protected MultiWRPP_Aesthetic_EXACT(Problem<WindyVertex, WindyEdge, WindyGraph> instance) throws IllegalArgumentException {
        super(instance);
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
    protected Problem<WindyVertex, WindyEdge, WindyGraph> getInstance() {
        return mInstance;
    }

    @Override
    protected Collection<? extends Route> solve() {
        return null;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.MULTI_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public String getSolverName() {
        return "Accelerated Corberan IP";
    }

    @Override
    public Solver<WindyVertex, WindyEdge, WindyGraph> instantiate(Problem<WindyVertex, WindyEdge, WindyGraph> p) {
        return new MultiWRPP_Aesthetic_EXACT(p);
    }

    @Override
    public HashMap<String, Double> getProblemParameters() {
        HashMap<String, Double> ret = new HashMap<String, Double>();
        return ret;
    }
}
