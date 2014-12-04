package oarlib.improvements.impl;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.InterRouteImprovementProcedure;
import oarlib.improvements.util.CompactMove;
import oarlib.improvements.util.Mover;
import oarlib.improvements.util.Utils;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oliverlum on 11/20/14.
 */
public class Change2to0 extends InterRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {

    public Change2to0(WindyGraph g, Collection<Route<WindyVertex, WindyEdge>> candidateRoute) {
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
        return offloadTwoEdges(longestRoute);
    }

    private Collection<Route<WindyVertex, WindyEdge>> offloadTwoEdges(Route<WindyVertex, WindyEdge> longestRoute) {

        Collection<Route<WindyVertex, WindyEdge>> initialSol = getInitialSol();
        int skipId = longestRoute.getGlobalId();
        Mover<WindyVertex, WindyEdge, WindyGraph> mover = new Mover<WindyVertex, WindyEdge, WindyGraph>(getGraph());

        for(Route<WindyVertex, WindyEdge> r: initialSol) {
            //don't try and move to yourself.
            if(r.getGlobalId() == skipId)
                continue;

            //business logic
            int lim = longestRoute.getCompactRepresentation().size()-1;
            int lim2 = r.getCompactRepresentation().size()-1;
            CompactMove<WindyVertex, WindyEdge> temp, temp2;
            ArrayList<CompactMove<WindyVertex, WindyEdge>> moveList = new ArrayList<CompactMove<WindyVertex, WindyEdge>>();
            int savings;
            Collection<Route<WindyVertex, WindyEdge>> ans = new ArrayList<Route<WindyVertex, WindyEdge>>();

            for(int i = 0; i < lim; i++) {
                for (int j = 0; j < lim2; j++) {
                    temp = new CompactMove<WindyVertex, WindyEdge>(longestRoute, r, i, j);
                    temp2 = new CompactMove<WindyVertex, WindyEdge>(longestRoute, r, i, j + 1);
                    moveList.clear();
                    moveList.add(temp);
                    moveList.add(temp2);
                    savings = mover.evalComplexMove(moveList, initialSol);
                    if(savings < 0) {
                        System.out.println("Savings: " + savings);
                        TIntObjectHashMap<Route<WindyVertex, WindyEdge>> routesToChange = mover.makeComplexMove(moveList);
                        for(Route r2: initialSol) {
                            if(routesToChange.containsKey(r2.getGlobalId())) {
                                ans.add(routesToChange.get(r2.getGlobalId()));
                            }
                            else {
                                ans.add(r2);
                            }
                        }
                        return ans;
                    }
                }
            }
        }
        return initialSol;
    }
}
