package oarlib.problem.impl.multivehicle;

import oarlib.core.Route;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.link.impl.Edge;
import oarlib.objfunc.MaxObjectiveFunction;
import oarlib.problem.impl.MultiVehicleProblem;
import oarlib.vertex.impl.UndirectedVertex;

import java.util.Collection;

/**
 * Problem class to represent the Capacitated Undirected Chinese Postman Problem.
 * Currently, this only supports a bound on the number of vehicles, and not a capacity constraint.
 * <p/>
 * Created by Oliver Lum on 7/25/2014.
 */
public class MultiVehicleUCPP extends MultiVehicleProblem<UndirectedVertex, Edge, UndirectedGraph> {

    public MultiVehicleUCPP(UndirectedGraph graph, int numVehicles) {
        super(graph, numVehicles, new MaxObjectiveFunction());
        mGraph = graph;
    }

    @Override
    public Type getProblemType() {
        return Type.UNDIRECTED_CHINESE_POSTMAN;
    }

    @Override
    public boolean isFeasible(Collection<Route> routes) {
        if (routes.size() > getmNumVehicles())
            return false;

        //TODO: Now check for real.
        return false;
    }
}
