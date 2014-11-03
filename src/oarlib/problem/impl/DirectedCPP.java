package oarlib.problem.impl;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.DirectedGraph;

import java.util.Collection;

/**
 * The Directed Chinese Postman Problem.
 *
 * @author oliverlum
 */
public class DirectedCPP extends Problem {

    private DirectedGraph mGraph;

    public DirectedCPP(DirectedGraph g) {
        this(g, "");
    }

    public DirectedCPP(DirectedGraph g, String name) {
        super(name);
        mGraph = g;
    }

    @Override
    public boolean isFeasible(Collection<Route> routes) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Type getType() {
        return Problem.Type.DIRECTED_CHINESE_POSTMAN;
    }

    @Override
    public DirectedGraph getGraph() {
        return mGraph;
    }

}
