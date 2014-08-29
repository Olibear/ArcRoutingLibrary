package oarlib.problem.impl;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.DirectedGraph;

import java.util.Collection;

/**
 * The Directed Rural Postman Problem.
 *
 * @author oliverlum
 */
public class DirectedRPP extends Problem {

    private DirectedGraph mGraph;

    public DirectedRPP(DirectedGraph g) {
        mGraph = g;
    }

    @Override
    public boolean isFeasible(Collection<Route> routes) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Type getType() {
        return Problem.Type.DIRECTED_RURAL_POSTMAN;
    }

    @Override
    public DirectedGraph getGraph() {
        return mGraph;
    }

}
