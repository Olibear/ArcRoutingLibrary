package oarlib.improvements.impl;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.ImprovementStrategy;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.WindyEdge;
import oarlib.vertex.impl.WindyVertex;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * Created by oliverlum on 12/4/14.
 */
public class Benavent_VND1 extends IntraRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    private static Logger LOGGER = Logger.getLogger(Benavent_VND1.class);

    public Benavent_VND1(Problem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }

    public Benavent_VND1(Problem<WindyVertex, WindyEdge, WindyGraph> problem, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, null, initialSol);
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    @Override
    public Route<WindyVertex, WindyEdge> improveRoute(Route<WindyVertex, WindyEdge> r) {

        int currBest = r.getCost();
        Route<WindyVertex, WindyEdge> ans = r;

        while (true) {
            OrInterchange oi = new OrInterchange(getProblem(), ImprovementStrategy.Type.SteepestDescent, getInitialSol());
            Route<WindyVertex, WindyEdge> postIP1 = oi.improveRoute(ans);
            LOGGER.info("VND1-ip1 obj value: " + postIP1.getCost());
            if(postIP1.getCost() < currBest) {
                currBest = postIP1.getCost();
                ans = postIP1;
                continue;
            }

            Reversal reversal = new Reversal(getProblem(), getInitialSol());
            Route<WindyVertex, WindyEdge> postIP2 = reversal.improveRoute(ans);
            LOGGER.info("VND1-ip2 obj value: " + postIP2.getCost());
            if(postIP2.getCost() < currBest) {
                currBest = postIP2.getCost();
                ans = postIP2;
                continue;
            }

            TwoInterchange ti = new TwoInterchange(getProblem(), ImprovementStrategy.Type.SteepestDescent, getInitialSol());
            Route<WindyVertex, WindyEdge> postIP3 = ti.improveRoute(ans);
            LOGGER.info("VND1-ip3 obj value: " + postIP3.getCost());
            if (postIP3.getCost() < currBest) {
                currBest = postIP3.getCost();
                ans = postIP3;
                continue;
            }

            break;
        }

        return ans;
    }
}
