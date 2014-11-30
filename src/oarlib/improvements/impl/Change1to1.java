package oarlib.improvements.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.InterRouteImprovementProcedure;
import oarlib.improvements.util.CompactMove;
import oarlib.improvements.util.Mover;
import oarlib.improvements.util.Utils;
import oarlib.link.impl.WindyEdge;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oliverlum on 11/20/14.
 */
public class Change1to1 extends InterRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {
    protected Change1to1(WindyGraph g, Collection<Route<WindyVertex, WindyEdge>> candidateRoute) {
        super(g, candidateRoute);
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    @Override
    public Collection<Route<WindyVertex, WindyEdge>> improveSolution() {

        //find the longest route
        Route longestRoute = Utils.findLongestRoute(getInitialSol());

        //try to offload each of the links to a cheaper route.
        return swapOneEdge(longestRoute);
    }

    private Collection<Route<WindyVertex, WindyEdge>> swapOneEdge(Route<WindyVertex, WindyEdge> longestRoute) {

        Collection<Route<WindyVertex, WindyEdge>> initialSol = getInitialSol();
        int skipId = longestRoute.getGlobalId();
        Mover<WindyVertex, WindyEdge, WindyGraph> mover = new Mover<WindyVertex, WindyEdge, WindyGraph>(getGraph());

        for (Route<WindyVertex, WindyEdge> r : initialSol) {
            //don't try and move to yourself.
            if (r.getGlobalId() == skipId)
                continue;

            //business logic
            int lim = longestRoute.getCompactRepresentation().size();
            int lim2 = r.getCompactRepresentation().size();
            CompactMove<WindyVertex, WindyEdge> temp, temp2;
            ArrayList<CompactMove<WindyVertex, WindyEdge>> moveList = new ArrayList<CompactMove<WindyVertex, WindyEdge>>();

            for (int i = 0; i < lim; i++) {
                for (int j = 0; j < lim2; j++) {
                    temp = new CompactMove<WindyVertex, WindyEdge>(longestRoute, r, i, j);
                    temp2 = new CompactMove<WindyVertex, WindyEdge>(r, longestRoute, j + 1, i);
                    moveList.clear();
                    moveList.add(temp);
                    moveList.add(temp2);
                    if (mover.evalComplexMove(moveList, initialSol) < 0) {
                        TIntObjectHashMap<Route<WindyVertex, WindyEdge>> routesToChange = mover.makeComplexMove(moveList);
                        for (Route r2 : initialSol) {
                            if (routesToChange.containsKey(r2.getGlobalId())) {
                                initialSol.remove(r2);
                                initialSol.add(routesToChange.get(r2.getGlobalId()));
                            }
                        }
                        return initialSol;

                    }
                }
            }
        }
        return null;
    }
}
