package oarlib.problem.impl;

import oarlib.core.MultiVehicleProblem;
import oarlib.core.MultiVehicleSolver;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;

import java.util.Collection;

/**
 * Created by oliverlum on 10/17/14.
 */
public class MultiVehicleWRPP extends MultiVehicleProblem {

    WindyGraph mGraph;

    public MultiVehicleWRPP(WindyGraph graph, int numVehicles) {
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
    public WindyGraph getGraph() {
        return mGraph;
    }

    @Override
    public Type getType() {
        return Type.WINDY_RURAL_POSTMAN;
    }

}
