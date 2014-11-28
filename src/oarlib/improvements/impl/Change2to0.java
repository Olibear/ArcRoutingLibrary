package oarlib.improvements.impl;

import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.InterRouteImprovementProcedure;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.vertex.impl.WindyVertex;

import java.util.Collection;

/**
 * Created by oliverlum on 11/20/14.
 */
public class Change2to0 extends InterRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {
    protected Change2to0(WindyGraph g, Collection<Route<WindyVertex, WindyEdge>> candidateRoute) {
        super(g, candidateRoute);
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    @Override
    public Collection<Route> improveSolution() {
        return null;
    }
}
