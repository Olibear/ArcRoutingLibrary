package oarlib.problem.impl;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;

import java.util.Collection;

public class WindyCPP extends Problem {

    WindyGraph mGraph;

    public WindyCPP(WindyGraph g) {
        mGraph = g;
    }

    @Override
    public boolean isFeasible(Collection<Route> routes) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Type getType() {
        return Problem.Type.WINDY_CHINESE_POSTMAN;
    }

    @Override
    public WindyGraph getGraph() {
        return mGraph;
    }

}
