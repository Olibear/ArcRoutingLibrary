package oarlib.improvements.impl;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.WindyEdge;
import oarlib.vertex.impl.WindyVertex;

import java.util.Collection;

/**
 * Created by oliverlum on 12/4/14.
 */
public class Benavent_VND1 extends IntraRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    public Benavent_VND1(WindyGraph windyGraph, Collection<Route<WindyVertex, WindyEdge>> candidateSol) {
        super(windyGraph, candidateSol);
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    @Override
    public Route<WindyVertex, WindyEdge> improveRoute(Route<WindyVertex, WindyEdge> r) {

        OrInterchange oi = new OrInterchange(getGraph(), getInitialSol());
        Route<WindyVertex, WindyEdge> postIP1 = oi.improveRoute(r);
        Reversal reversal = new Reversal(getGraph(), getInitialSol());
        Route<WindyVertex, WindyEdge> postIP2 = reversal.improveRoute(postIP1);
        TwoInterchange ti = new TwoInterchange(getGraph(), getInitialSol());
        Route<WindyVertex, WindyEdge> postIP3 = ti.improveRoute(postIP2);

        return postIP3;
    }
}
