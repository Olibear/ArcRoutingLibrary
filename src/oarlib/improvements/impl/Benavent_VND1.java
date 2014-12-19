package oarlib.improvements.impl;

import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
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
        super(problem, initialSol);
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    @Override
    public Route<WindyVertex, WindyEdge> improveRoute(Route<WindyVertex, WindyEdge> r) {

        int currBest = r.getCost();
        Route<WindyVertex, WindyEdge> ans = null;

        while (true) {
            OrInterchange oi = new OrInterchange(getProblem(), getInitialSol());
            Route<WindyVertex, WindyEdge> postIP1 = oi.improveRoute(r);
            LOGGER.info("VND1-ip1 obj value: " + postIP1.getCost());
            if(postIP1.getCost() < currBest) {
                currBest = postIP1.getCost();
                continue;
            }

            Reversal reversal = new Reversal(getProblem(), getInitialSol());
            Route<WindyVertex, WindyEdge> postIP2 = reversal.improveRoute(postIP1);
            LOGGER.info("VND1-ip2 obj value: " + postIP1.getCost());
            if(postIP2.getCost() < currBest) {
                currBest = postIP2.getCost();
                continue;
            }

            TwoInterchange ti = new TwoInterchange(getProblem(), getInitialSol());
            ans = ti.improveRoute(postIP2);
            LOGGER.info("VND1-ip3 obj value: " + postIP1.getCost());
            if(ans.getCost() < currBest) {
                currBest = ans.getCost();
                continue;
            }
            break;
        }

        return ans;
    }
}
