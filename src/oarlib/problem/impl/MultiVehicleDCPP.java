package oarlib.problem.impl;

import oarlib.core.MultiVehicleProblem;
import oarlib.core.Route;
import oarlib.graph.impl.DirectedGraph;

import java.util.Collection;

/**
 * Created by oliverlum on 8/5/14.
 */
public class MultiVehicleDCPP extends MultiVehicleProblem {

    DirectedGraph mGraph;

    public MultiVehicleDCPP(DirectedGraph graph, int numVehicles) {
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

        //TODO: Now check for real
        return false;
    }

    @Override
    public DirectedGraph getGraph() {
        return mGraph;
    }

    @Override
    public Type getType() {
        return Type.DIRECTED_CHINESE_POSTMAN;
    }
}
