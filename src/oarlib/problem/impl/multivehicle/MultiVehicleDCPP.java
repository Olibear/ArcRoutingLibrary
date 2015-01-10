package oarlib.problem.impl.multivehicle;

import oarlib.core.Route;
import oarlib.graph.impl.DirectedGraph;
import oarlib.link.impl.Arc;
import oarlib.objfunc.MaxObjectiveFunction;
import oarlib.problem.impl.MultiVehicleProblem;
import oarlib.vertex.impl.DirectedVertex;

import java.util.Collection;

/**
 * Created by oliverlum on 8/5/14.
 */
public class MultiVehicleDCPP extends MultiVehicleProblem<DirectedVertex, Arc, DirectedGraph> {

    public MultiVehicleDCPP(DirectedGraph graph, int numVehicles) {
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
        return Type.DIRECTED_CHINESE_POSTMAN;
    }
}
