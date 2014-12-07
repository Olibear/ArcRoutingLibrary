package oarlib.problem.impl.cpp;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.UndirectedGraph;
import oarlib.link.impl.Edge;
import oarlib.problem.impl.ChinesePostmanProblem;
import oarlib.vertex.impl.UndirectedVertex;

import java.util.Collection;

public class UndirectedCPP extends ChinesePostmanProblem<UndirectedVertex, Edge, UndirectedGraph> {

    public UndirectedCPP(UndirectedGraph g) {
        this(g, "");
    }

    public UndirectedCPP(UndirectedGraph g, String name) {
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
        return Problem.Type.UNDIRECTED_CHINESE_POSTMAN;
    }

}
