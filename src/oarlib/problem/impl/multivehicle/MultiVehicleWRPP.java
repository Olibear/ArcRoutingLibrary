package oarlib.problem.impl.multivehicle;

import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.problem.impl.MultiVehicleProblem;

import java.util.Collection;

/**
 * Created by oliverlum on 10/17/14.
 */
public class MultiVehicleWRPP extends MultiVehicleProblem<WindyGraph> {

    public MultiVehicleWRPP(WindyGraph graph, int numVehicles) {
        super(graph, numVehicles);
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
    public Type getProblemType() {
        return Type.WINDY_RURAL_POSTMAN;
    }

}
