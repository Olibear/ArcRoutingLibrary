package oarlib.improvements.util;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import oarlib.core.Graph;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.graph.util.Pair;
import oarlib.link.impl.WindyEdge;
import oarlib.route.util.RouteExpander;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oliverlum on 11/29/14.
 */
public class Mover<V extends Vertex, E extends Link<V>, G extends Graph<V, E>> {

    private static final Logger LOGGER = Logger.getLogger(Mover.class);
    private G mGraph;
    private int[][] dist;
    private int[][] path;
    private int[][] edgePath;

    public Mover(G g) {
        mGraph = g;
        int n = g.getVertices().size();
        dist = new int[n + 1][n + 1];
        path = new int[n + 1][n + 1];
        edgePath = new int[n + 1][n + 1];

        CommonAlgorithms.fwLeastCostPaths(g, dist, path, edgePath);

        //zero out self-distances
        for(int i = 1 ; i <= n; i++) {
            dist[i][i] = 0;
        }
    }

    /**
     * Method that determines the cost of making a series of moves.
     *
     * @param moveList List of moves to be executed in the order provided.
     * @return - the cost associated with making the moves provided.
     */
    public int evalComplexMove(ArrayList<CompactMove<V, E>> moveList, Collection<Route<V,E>> routes) throws IllegalArgumentException {

        //check for trivial arg
        if(moveList.size() == 0)
            return 0;

        //ans; if it's < 0, it's anticipated that you reap savings
        int ans;

        //init
        TIntIntHashMap costMap = new TIntIntHashMap();
        TIntObjectHashMap<TIntArrayList> compactReps = new TIntObjectHashMap<TIntArrayList>();
        TIntObjectHashMap<ArrayList<Boolean>> compactTDs = new TIntObjectHashMap<ArrayList<Boolean>>();

        int tempCost;
        int max = Integer.MIN_VALUE;
        String origCost = "";

        //setup a hashmap that keeps track of initial costs
        for(Route<V,E> r : routes) {
            tempCost = r.getCost();
            costMap.put(r.getGlobalId(),tempCost);
            compactReps.put(r.getGlobalId(), new TIntArrayList(r.getCompactRepresentation().toNativeArray()));
            compactTDs.put(r.getGlobalId(), new ArrayList<Boolean>(r.getCompactTraversalDirection()));
            origCost += "Originally, route: " + r.getGlobalId() + " had cost: " + tempCost + "\n";
            if(tempCost > max) {
                max = tempCost;
            }
        }

        ans = max;

        int size = moveList.size();
        Pair<Integer> change = new Pair<Integer>(0,0);
        int tempId;
        CompactMove<V,E> tempMove;
        boolean forward;
        int tempLinkId;

        for (int i = 0; i < size; i++) {

            tempMove = moveList.get(i);
            TIntArrayList flatFrom = compactReps.get(tempMove.getFrom().getGlobalId());
            TIntArrayList flatTo = compactReps.get(tempMove.getTo().getGlobalId());
            ArrayList<Boolean> fromDir = compactTDs.get(tempMove.getFrom().getGlobalId());
            ArrayList<Boolean> toDir = compactTDs.get(tempMove.getTo().getGlobalId());
            int fromPos = tempMove.getFromPos();
            int toPos = tempMove.getToPos();

            if(tempMove.getFrom().getGlobalId() == tempMove.getTo().getGlobalId()) {
                if(tempMove.getFromPos() < tempMove.getToPos())
                    toPos++;
                else if(tempMove.getFromPos() == tempMove.getToPos())
                    continue;
            }

            forward = assessMoveCost(flatFrom, fromDir, flatTo, toDir, fromPos, toPos, change);
            tempMove.setPrudentDirection(forward);
            tempId = tempMove.getFrom().getGlobalId();
            costMap.put(tempId, costMap.get(tempId) - change.getFirst());
            tempId = tempMove.getTo().getGlobalId();
            costMap.put(tempId, costMap.get(tempId) + change.getSecond());

            //make the mods for future moves

            //remove link
            tempLinkId = flatFrom.get(tempMove.getFromPos());
            fromDir.remove(tempMove.getFromPos());
            flatFrom.remove(tempMove.getFromPos());

            //add it back
            flatTo.insert(tempMove.getToPos(), tempLinkId);
            toDir.add(tempMove.getToPos(), forward);


        }

        String expectedCosts = "";
        expectedCosts += "Predicted costs: \n";
        //for min max
        max = Integer.MIN_VALUE;
        for(int i: costMap.keys()) {
            expectedCosts += "Global id: " + i + " cost: " + costMap.get(i) + "\n";
            if(costMap.get(i) > max)
                max = costMap.get(i);
        }
        expectedCosts += "End predicted costs.\n";

        if(max - ans < 0) {
            LOGGER.debug(origCost + expectedCosts);
        }

        return max - ans;
    }

    public TIntObjectHashMap<Route<V,E>> makeComplexMove(ArrayList<CompactMove<V,E>> moveList) throws IllegalArgumentException {

        TIntObjectHashMap<Route<V,E>> ans = new TIntObjectHashMap<Route<V, E>>();
        int n = moveList.size();
        CompactMove<V,E> currMove;
        Route<V,E> currFrom, currTo;
        TIntArrayList flatFrom, flatTo;
        RouteExpander<G> re = new RouteExpander<G>(mGraph);
        int currLinkId;
        ArrayList<Boolean> newFromDir, newToDir;

        for(int i = 0; i < n; i ++) {

            currMove = moveList.get(i);

            //remove link
            currFrom = currMove.getFrom();
            flatFrom = currFrom.getCompactRepresentation();
            currLinkId = flatFrom.get(currMove.getFromPos());
            newFromDir = currFrom.getCompactTraversalDirection();
            newFromDir.remove(currMove.getFromPos());

            flatFrom.remove(currMove.getFromPos());
            ans.put(currFrom.getGlobalId(), re.unflattenRoute(flatFrom,newFromDir));

            LOGGER.debug("The route with id: " + currFrom.getGlobalId() + " was replaced with a route costing: " + re.unflattenRoute(flatFrom,newFromDir).getCost());

            currTo = currMove.getTo();
            if(currTo.getGlobalId() != currFrom.getGlobalId()) {
                flatTo = currTo.getCompactRepresentation();
                flatTo.insert(currMove.getToPos(), currLinkId);
                newToDir = currTo.getCompactTraversalDirection();
                newToDir.add(currMove.getToPos(), currMove.isPrudentDirection());
                ans.put(currTo.getGlobalId(), re.unflattenRoute(flatTo, newToDir));
            } else {
                flatTo = flatFrom;
                flatTo.insert(currMove.getToPos(), currLinkId);
                newToDir = newFromDir;
                newToDir.add(currMove.getToPos(), currMove.isPrudentDirection());
                ans.put(currTo.getGlobalId(), re.unflattenRoute(flatTo, newToDir));
            }

            LOGGER.debug("The route with id: " + currTo.getGlobalId() + " was replaced with a route costing: " + re.unflattenRoute(flatTo, newToDir).getCost());

        }

        return ans;
    }

    private boolean assessMoveCost(TIntArrayList fromList, ArrayList<Boolean> fromDir, TIntArrayList toList, ArrayList<Boolean> toDir, int fromPos, int toPos, Pair<Integer> ans) {
        Pair<Integer> ans1 = new Pair<Integer>(0,0);
        Pair<Integer> ans2 = new Pair<Integer>(0,0);

        int prevId, nextId, currFirst, currSecond, currCost, currCostAlt;
        E curr = mGraph.getEdge(fromList.get(fromPos));
        boolean isDirected = curr.isDirected();

        if(fromDir.get(fromPos)) {
            currFirst = curr.getEndpoints().getFirst().getId();
            currSecond = curr.getEndpoints().getSecond().getId();
            currCost = curr.getCost();
            if(curr.isWindy())
                currCostAlt = ((WindyEdge)curr).getReverseCost();
            else
                currCostAlt = currCost;
        } else {
            currSecond = curr.getEndpoints().getFirst().getId();
            currFirst = curr.getEndpoints().getSecond().getId();
            currCostAlt = curr.getCost();
            if(curr.isWindy())
                currCost = ((WindyEdge)curr).getReverseCost();
            else
                currCost = curr.getCost();
        }

        //removal savings
        if(fromPos == 0) {
            prevId = mGraph.getDepotId();

            if(fromDir.get(fromPos + 1))
                nextId = mGraph.getEdge(fromList.get(fromPos + 1)).getEndpoints().getFirst().getId();
            else
                nextId = mGraph.getEdge(fromList.get(fromPos + 1)).getEndpoints().getSecond().getId();

        } else if(fromPos == fromList.size() - 1) {
            nextId = mGraph.getDepotId();

            if(fromDir.get(fromPos - 1))
                prevId = mGraph.getEdge(fromList.get(fromPos - 1)).getEndpoints().getSecond().getId();
            else
                prevId = mGraph.getEdge(fromList.get(fromPos - 1)).getEndpoints().getFirst().getId();

        } else {
            if(fromDir.get(fromPos - 1))
                prevId = mGraph.getEdge(fromList.get(fromPos - 1)).getEndpoints().getSecond().getId();
            else
                prevId = mGraph.getEdge(fromList.get(fromPos - 1)).getEndpoints().getFirst().getId();

            if(fromDir.get(fromPos + 1))
                nextId = mGraph.getEdge(fromList.get(fromPos + 1)).getEndpoints().getFirst().getId();
            else
                nextId = mGraph.getEdge(fromList.get(fromPos + 1)).getEndpoints().getSecond().getId();
        }

        ans1.setFirst(dist[prevId][currFirst] + dist[currSecond][nextId] + currCost - dist[prevId][nextId]);
        ans2.setFirst(ans1.getFirst()); //the savings is fixed by the current orientation

        //addition cost
        if(toPos == 0) {
            prevId = mGraph.getDepotId();

            if(toDir.get(toPos))
                nextId = mGraph.getEdge(toList.get(toPos)).getEndpoints().getFirst().getId();
            else
                nextId = mGraph.getEdge(toList.get(toPos)).getEndpoints().getSecond().getId();

        } else if(toPos == toList.size()) {
            nextId = mGraph.getDepotId();

            if(toDir.get(toPos - 1))
                prevId = mGraph.getEdge(toList.get(toPos - 1)).getEndpoints().getSecond().getId();
            else
                prevId = mGraph.getEdge(toList.get(toPos - 1)).getEndpoints().getFirst().getId();

        } else {
            if(toDir.get(toPos - 1))
                prevId = mGraph.getEdge(toList.get(toPos - 1)).getEndpoints().getSecond().getId();
            else
                prevId = mGraph.getEdge(toList.get(toPos - 1)).getEndpoints().getFirst().getId();

            if(toDir.get(toPos))
                nextId = mGraph.getEdge(toList.get(toPos)).getEndpoints().getFirst().getId();
            else
                nextId = mGraph.getEdge(toList.get(toPos)).getEndpoints().getSecond().getId();
        }

        ans1.setSecond(dist[prevId][currFirst] + dist[currSecond][nextId] + currCost - dist[prevId][nextId]);
        ans2.setSecond(dist[prevId][currSecond] + dist[currFirst][nextId] + currCostAlt - dist[prevId][nextId]);

        //TODO: we need to pick the better direction, which will depend on objective function type
        if(!isDirected) {
            if(ans.getSecond() < ans2.getSecond()) {
                ans.setFirst(ans1.getFirst());
                ans.setSecond(ans1.getSecond());
                return fromDir.get(fromPos);
            }
            else {
                ans.setFirst(ans2.getFirst());
                ans.setSecond(ans2.getSecond());
                return !fromDir.get(fromPos);
            }
        }

        ans.setFirst(ans1.getFirst());
        ans.setSecond(ans1.getSecond());
        return true;
    }

}
