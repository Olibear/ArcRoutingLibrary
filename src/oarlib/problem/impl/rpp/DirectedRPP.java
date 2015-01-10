package oarlib.problem.impl.rpp;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.DirectedGraph;
import oarlib.link.impl.Arc;
import oarlib.objfunc.SumObjectiveFunction;
import oarlib.problem.impl.RuralPostmanProblem;
import oarlib.vertex.impl.DirectedVertex;

import java.util.Collection;

/**
 * The Directed Rural Postman Problem.
 *
 * @author oliverlum
 */
public class DirectedRPP extends RuralPostmanProblem<DirectedVertex, Arc, DirectedGraph> {

    public DirectedRPP(DirectedGraph g) {
        this(g, "");
    }

    public DirectedRPP(DirectedGraph g, String name) {
        super(g, name, new SumObjectiveFunction());
        mGraph = g;
    }

    @Override
    public boolean isFeasible(Collection<Route> routes) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Type getProblemType() {
        return Problem.Type.DIRECTED_RURAL_POSTMAN;
    }

}
