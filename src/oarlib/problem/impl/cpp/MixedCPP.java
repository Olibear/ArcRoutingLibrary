package oarlib.problem.impl.cpp;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.MixedGraph;
import oarlib.link.impl.MixedEdge;
import oarlib.objfunc.SumObjectiveFunction;
import oarlib.problem.impl.ChinesePostmanProblem;
import oarlib.vertex.impl.MixedVertex;

import java.util.Collection;

public class MixedCPP extends ChinesePostmanProblem<MixedVertex, MixedEdge, MixedGraph> {

    public MixedCPP(MixedGraph g) {
        this(g, "");
    }

    public MixedCPP(MixedGraph g, String name) {
        super(g, name, new SumObjectiveFunction());
        mGraph = g;
    }

    @Override
    public boolean isFeasible(Collection<Route> routes) {
        return false;
    }

    @Override
    public Type getProblemType() {
        return Problem.Type.MIXED_CHINESE_POSTMAN;
    }

}
