package oarlib.problem.impl.multivehicle;

import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.link.impl.WindyEdge;
import oarlib.objfunc.MaxObjectiveFunction;
import oarlib.problem.impl.MultiVehicleProblem;
import oarlib.vertex.impl.WindyVertex;

import java.util.Collection;

/**
 * Created by oliverlum on 10/17/14.
 */
public class MultiVehicleWRPP extends MultiVehicleProblem<WindyVertex, WindyEdge, WindyGraph> {

    public MultiVehicleWRPP(WindyGraph graph, int numVehicles) {
        super(graph, numVehicles, new MaxObjectiveFunction());
        mGraph = graph;
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
