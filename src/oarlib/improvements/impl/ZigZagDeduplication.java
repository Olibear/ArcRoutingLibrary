package oarlib.improvements.impl;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.ZigZagGraph;
import oarlib.graph.util.Pair;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.ZigZagLink;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.route.impl.ZigZagTour;
import oarlib.route.util.ZigZagExpander;
import oarlib.vertex.impl.ZigZagVertex;

import java.util.ArrayList;

/**
 * Created by oliverlum on 9/26/15.
 */
public class ZigZagDeduplication extends IntraRouteImprovementProcedure<ZigZagVertex, ZigZagLink, ZigZagGraph> {

    public ZigZagDeduplication(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> problem) {
        super(problem);
    }

    /**
     * Improvement procedure which looks for cases where you zig-zag, but traverse the link in
     * the opposite direction anyways later on; thereby saving the marginal meander cost
     *
     * @param r
     * @return
     */
    @Override
    public ZigZagTour improveRoute(Route<ZigZagVertex, ZigZagLink> r) {

        if (!(r instanceof ZigZagTour))
            return null;

        ZigZagExpander ex = new ZigZagExpander(getGraph(), ((ZigZagTour) r).getPenalty());
        ZigZagTour rCopy = ex.unflattenRoute(r.getCompactRepresentation(), r.getCompactTraversalDirection(), ((ZigZagTour) r).getCompactZZList());

        ArrayList<Boolean> sl = rCopy.getServicingList();
        ArrayList<Boolean> td = rCopy.getTraversalDirection();
        ArrayList<ZigZagLink> path = rCopy.getPath();

        /*
         * key = id; value = 1: forward, 2: backward
         */
        TIntObjectHashMap<Pair<Integer>> nonReq = new TIntObjectHashMap<Pair<Integer>>();

        //see who we might be missing out on servicing
        for (int i = 0; i < path.size(); i++) {
            if (!sl.get(i)) {
                if (td.get(i))
                    nonReq.put(path.get(i).getId(), new Pair<Integer>(1, i));
                else
                    nonReq.put(path.get(i).getId(), new Pair<Integer>(2, i));
            }
        }

        //cross-check against who we're actually servicing
        ArrayList<Boolean> compactTD = rCopy.getCompactTraversalDirection();
        ArrayList<Boolean> compactZZ = rCopy.getCompactZZList();
        TIntArrayList compactRoute = rCopy.getCompactRepresentation();


        int tempId;
        boolean tempDir, tempDir2;
        for (int i = 0; i < compactRoute.size(); i++) {

            //only care about zig zag service
            if (!compactZZ.get(i))
                continue;

            tempId = compactRoute.get(i);
            tempDir = compactTD.get(i);
            if (nonReq.containsKey(tempId)) {
                //opposite directions?
                tempDir2 = nonReq.get(tempId).getFirst() == 1;
                if (tempDir != tempDir2) {
                    //perform the improvement by not zigzagging
                    rCopy.changeZigZagStatus(i);
                    //and then servicing the other guy
                    rCopy.changeService(nonReq.get(tempId).getSecond());
                }
            }
        }

        return rCopy;
    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, ProblemAttributes.Type.RURAL_POSTMAN, ProblemAttributes.NumVehicles.SINGLE_VEHICLE, ProblemAttributes.NumDepots.SINGLE_DEPOT, ProblemAttributes.Properties.TIME_WINDOWS);
    }
}
