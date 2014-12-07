package oarlib.problem.impl.cpp;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.DirectedGraph;
import oarlib.link.impl.Arc;
import oarlib.problem.impl.ChinesePostmanProblem;
import oarlib.vertex.impl.DirectedVertex;

import java.util.Collection;

/**
 * The Directed Chinese Postman Problem.
 *
 * @author oliverlum
 */
public class DirectedCPP extends ChinesePostmanProblem<DirectedVertex, Arc, DirectedGraph> {

    public DirectedCPP(DirectedGraph g) {
        this(g, "");
    }

    public DirectedCPP(DirectedGraph g, String name) {
        super(g, name);
        mGraph = g;
    }

    @Override
    public boolean isFeasible(Collection<Route> routes) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Type getProblemType() {
        return Type.DIRECTED_CHINESE_POSTMAN;
    }

}
