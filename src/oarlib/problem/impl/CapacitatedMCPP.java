package oarlib.problem.impl;

import oarlib.core.CapacitatedProblem;
import oarlib.core.Route;
import oarlib.graph.impl.MixedGraph;

import java.util.Collection;

/**
 * Problem class to represent the Capacitated Mixed Chinese Postman Problem.
 * Currently, this only supports a bound on the number of vehicles, and not a capacity constraint.
 * <p/>
 * Created by oliverlum on 8/12/14.
 */
public class CapacitatedMCPP extends CapacitatedProblem {

    MixedGraph mGraph;

    public CapacitatedMCPP(MixedGraph graph, int numVehicles) {

        super(numVehicles);
        mGraph = graph;

    }

    @Override
    public CapacitatedObjective getObjectiveType() {
        return CapacitatedObjective.MinMax;
    }

    @Override
    public boolean isFeasible(Collection<Route> routes) {
        if (routes.size() > getmNumVehicles())
            return false;

        //TODO: Now check for real.
        return false;
    }

    @Override
    public MixedGraph getGraph() {
        return mGraph;
    }

    @Override
    public Type getType() {
        return Type.MIXED_CHINESE_POSTMAN;
    }
}
