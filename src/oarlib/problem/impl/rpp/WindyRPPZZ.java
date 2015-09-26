package oarlib.problem.impl.rpp;

import gnu.trove.TIntArrayList;
import oarlib.core.Graph;
import oarlib.core.Route;
import oarlib.graph.impl.ZigZagGraph;
import oarlib.link.impl.ZigZagLink;
import oarlib.metrics.SumMetric;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.problem.impl.RuralPostmanProblem;
import oarlib.route.impl.ZigZagTour;
import oarlib.vertex.impl.ZigZagVertex;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oliverlum on 6/20/15.
 */
public class WindyRPPZZ extends RuralPostmanProblem<ZigZagVertex, ZigZagLink, ZigZagGraph> {

    private static Logger LOGGER = Logger.getLogger(WindyRPPZZ.class);

    public WindyRPPZZ(ZigZagGraph graph, String name) {
        super(graph, name, new SumMetric());
        mGraph = graph;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, null);
    }

    @Override
    public boolean isFeasible(Collection<Route<ZigZagVertex, ZigZagLink>> routes) {
        if (super.isFeasible(routes)) {
            //check to make sure that zig-zag status is obeyed
            for (Route<ZigZagVertex, ZigZagLink> r : routes) {
                if (!(r instanceof ZigZagTour)) {
                    LOGGER.warn("Routes must be zig-zag tours. Returning false.");
                    return false;
                }
                ZigZagTour rTemp = (ZigZagTour) r;
                TIntArrayList compactRep = rTemp.getCompactRepresentation();
                ArrayList<Boolean> zzList = rTemp.getCompactZZList();

                for (int i = 0; i < compactRep.size(); i++) {

                }
            }
        }

        return false;
    }
}
