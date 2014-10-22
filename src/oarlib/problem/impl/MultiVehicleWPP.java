package oarlib.problem.impl;

import oarlib.core.MultiVehicleProblem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;

import java.util.Collection;

/**
 * Created by oliverlum on 8/14/14.
 */
public class MultiVehicleWPP extends MultiVehicleProblem {

    WindyGraph mGraph;

    public MultiVehicleWPP(WindyGraph graph, int numVehicles) {
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
        return Type.WINDY_CHINESE_POSTMAN;
    }
}
