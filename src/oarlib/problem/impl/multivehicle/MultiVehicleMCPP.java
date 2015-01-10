package oarlib.problem.impl.multivehicle;

import oarlib.core.Route;
import oarlib.graph.impl.MixedGraph;
import oarlib.link.impl.MixedEdge;
import oarlib.objfunc.MaxObjectiveFunction;
import oarlib.problem.impl.MultiVehicleProblem;
import oarlib.vertex.impl.MixedVertex;

import java.util.Collection;

/**
 * Problem class to represent the Capacitated Mixed Chinese Postman Problem.
 * Currently, this only supports a bound on the number of vehicles, and not a capacity constraint.
 * <p/>
 * Created by oliverlum on 8/12/14.
 */
public class MultiVehicleMCPP extends MultiVehicleProblem<MixedVertex, MixedEdge, MixedGraph> {

    public MultiVehicleMCPP(MixedGraph graph, int numVehicles) {

        super(graph, numVehicles, new MaxObjectiveFunction());
        mGraph = graph;

    }

    @Override
    public boolean isFeasible(Collection<Route> routes) {
        if (routes.size() > getmNumVehicles())
            return false;

        //TODO: Now check for real.
        return false;
    }

    @Override
    public Type getProblemType() {
        return Type.MIXED_CHINESE_POSTMAN;
    }
}
