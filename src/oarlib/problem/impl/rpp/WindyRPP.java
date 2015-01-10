package oarlib.problem.impl.rpp;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.link.impl.WindyEdge;
import oarlib.objfunc.SumObjectiveFunction;
import oarlib.problem.impl.RuralPostmanProblem;
import oarlib.vertex.impl.WindyVertex;

import java.util.Collection;

public class WindyRPP extends RuralPostmanProblem<WindyVertex, WindyEdge, WindyGraph> {

    public WindyRPP(WindyGraph g) {
        this(g, "");
    }

    public WindyRPP(WindyGraph g, String name) {
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
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

}
