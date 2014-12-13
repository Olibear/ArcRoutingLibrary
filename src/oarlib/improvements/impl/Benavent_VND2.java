package oarlib.improvements.impl;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.InterRouteImprovementProcedure;
import oarlib.link.impl.WindyEdge;
import oarlib.vertex.impl.WindyVertex;

import java.util.Collection;

/**
 * Created by oliverlum on 12/4/14.
 */
public class Benavent_VND2 extends InterRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    public Benavent_VND2(Problem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }
    public Benavent_VND2(Problem<WindyVertex, WindyEdge, WindyGraph> problem, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, initialSol);
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    @Override
    public Collection<Route<WindyVertex, WindyEdge>> improveSolution() {

        Collection<Route<WindyVertex, WindyEdge>> initialSol = getInitialSol();
        Change1to0 ip1 = new Change1to0(getProblem(),initialSol);
        Collection<Route<WindyVertex, WindyEdge>> postIP1 = ip1.improveSolution();
        Change2to0 ip2 = new Change2to0(getProblem(),postIP1);
        Collection<Route<WindyVertex, WindyEdge>> postIP2 = ip2.improveSolution();
        Change1to1 ip3 = new Change1to1(getProblem(),postIP2);
        Collection<Route<WindyVertex, WindyEdge>> postIP3 = ip3.improveSolution();

        return postIP3;
    }
}
