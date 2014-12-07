package oarlib.problem.impl.cpp;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.ChinesePostmanProblem;
import oarlib.vertex.impl.WindyVertex;

import java.util.Collection;

public class WindyCPP extends ChinesePostmanProblem<WindyVertex, WindyEdge, WindyGraph> {

    public WindyCPP(WindyGraph g) {
        this(g, "");
    }

    public WindyCPP(WindyGraph g, String name) {
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
        return Problem.Type.WINDY_CHINESE_POSTMAN;
    }

}
