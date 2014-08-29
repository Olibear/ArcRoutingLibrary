package oarlib.problem.impl;

import oarlib.core.CapacitatedProblem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;

import java.util.Collection;

/**
 * Created by oliverlum on 8/14/14.
 */
public class CapacitatedWPP extends CapacitatedProblem {

    WindyGraph mGraph;

    protected CapacitatedWPP(WindyGraph graph, int numVehicles) {
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
