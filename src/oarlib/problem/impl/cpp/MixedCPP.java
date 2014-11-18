package oarlib.problem.impl.cpp;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.MixedGraph;
import oarlib.problem.impl.ChinesePostmanProblem;

import java.util.Collection;

public class MixedCPP extends ChinesePostmanProblem<MixedGraph> {

    public MixedCPP(MixedGraph g) {
        this(g, "");
    }

    public MixedCPP(MixedGraph g, String name) {
        super(g, name);
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
