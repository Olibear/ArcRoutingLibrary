package oarlib.improvements.perturbation;

import gnu.trove.TIntObjectHashMap;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.improvements.ImprovementStrategy;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.improvements.util.CompactMove;
import oarlib.improvements.util.Mover;
import oarlib.link.impl.WindyEdge;
import oarlib.vertex.impl.WindyVertex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by oliverlum on 12/5/14.
 */
public class TwoSwapPerturb extends IntraRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {
    
    public TwoSwapPerturb(Problem<WindyVertex, WindyEdge, WindyGraph> problem) {
        super(problem);
    }

    public TwoSwapPerturb(Problem<WindyVertex, WindyEdge, WindyGraph> problem, ImprovementStrategy.Type strat, Collection<Route<WindyVertex, WindyEdge>> initialSol) {
        super(problem, strat, initialSol);
    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }

    @Override
    public Route<WindyVertex, WindyEdge> improveRoute(Route<WindyVertex, WindyEdge> r) {

        Route<WindyVertex, WindyEdge> ans = null;

        Random rng = new Random();
        List<WindyEdge> rPath = r.getRoute();
        int routeLength = r.getCompactRepresentation().size();
        int index1 = rng.nextInt(routeLength);
        int index2 = rng.nextInt(routeLength);

        //in case they're the same
        if(index1 == index2) {
            index2 = (index2 + 1) % routeLength;
        }

        CompactMove<WindyVertex, WindyEdge> temp, temp2;
        ArrayList<CompactMove<WindyVertex, WindyEdge>> moveList = new ArrayList<CompactMove<WindyVertex, WindyEdge>>();
        //swap them
        temp = new CompactMove<WindyVertex, WindyEdge>(r, r, index1, index2);
        if(index1 < index2)
            temp2 = new CompactMove<WindyVertex, WindyEdge>(r, r, index2 - 1 , index1);
        else
            temp2 = new CompactMove<WindyVertex, WindyEdge>(r, r, index2 + 1, index1);//or + 1
        moveList.add(temp);
        moveList.add(temp2);

        Mover<WindyVertex, WindyEdge, WindyGraph> mover = new Mover<WindyVertex, WindyEdge, WindyGraph>(getGraph());
        ArrayList<Route<WindyVertex, WindyEdge>> tempList = new ArrayList<Route<WindyVertex, WindyEdge>>();
        tempList.add(r);
        mover.evalComplexMove(moveList, tempList);
        TIntObjectHashMap<Route<WindyVertex, WindyEdge>> newRoutes = mover.makeComplexMove(moveList);

        for(int i : newRoutes.keys())
            return newRoutes.get(i);

        return null;
    }
}
