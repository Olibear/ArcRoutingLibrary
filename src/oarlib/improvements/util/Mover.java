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

import java.util.*;

/**
 * Created by oliverlum on 11/29/14.
 */
public class Mover<V extends Vertex, E extends Link<V>, G extends Graph<V, E>> {

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
    }

    /**
     * Method that determines the cost of making a series of moves.
     *
     * @param moveList List of moves to be executed in the order provided.
     * @return - the cost associated with making the moves provided.
     */
    //TODO: add objective function type
    public int evalComplexMove(ArrayList<CompactMove<V, E>> moveList, Collection<Route<V,E>> routes) throws IllegalArgumentException {

        int ans;

        TIntIntHashMap costMap = new TIntIntHashMap();
        int tempCost;
        int max = Integer.MIN_VALUE;
        for(Route<V,E> r : routes) {
            tempCost = r.getCost();
            costMap.put(r.getGlobalId(),tempCost);
            if(tempCost > max) {
                max = tempCost;
            }
        }

        ans = max;

        int size = moveList.size();
        Pair<Integer> change;
        int tempId;
        CompactMove<V,E> tempMove;

        for (int i = 0; i < size; i++) {
            tempMove = moveList.get(i);
            change = assessMoveCost(tempMove);
            tempId = tempMove.getFrom().getGlobalId();
            costMap.put(tempId, costMap.get(tempId) - change.getFirst());
            tempId = tempMove.getTo().getGlobalId();
            costMap.put(tempId, costMap.get(tempId) + change.getSecond());
        }

        //for min max
        max = Integer.MIN_VALUE;
        for(int i: costMap.getValues()) {
            if(i > max)
                max = i;
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

            currTo = currMove.getTo();
            flatTo = currTo.getCompactRepresentation();
            flatTo.insert(currMove.getToPos(), currLinkId);
            newToDir = currTo.getCompactTraversalDirection();
            newToDir.add(currMove.getToPos(),currMove.isPrudentDirection());
            ans.put(currTo.getGlobalId(), re.unflattenRoute(flatTo,newToDir));

        }

        return ans;
    }

    private Pair<Integer> assessMoveCost(CompactMove<V,E> move) {
        Pair<Integer> ans = new Pair<Integer>(0,0);
        Pair<Integer> ans2 = new Pair<Integer>(0,0);

        Route<V,E> from = move.getFrom();
        Route<V,E> to = move.getTo();
        TIntArrayList fromList = from.getCompactRepresentation();
        ArrayList<Boolean> fromDir = from.getCompactTraversalDirection();
        TIntArrayList toList = to.getCompactRepresentation();
        ArrayList<Boolean> toDir = to.getCompactTraversalDirection();

        int prevId, nextId, currFirst, currSecond, currCost, currCostAlt, alt1, alt2;
        int fromPos = move.getFromPos();
        int toPos = move.getToPos();
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
                nextId = mGraph.getEdge(fromList.get(fromPos + 1)).getEndpoints().getSecond().getId();
            else
                nextId = mGraph.getEdge(fromList.get(fromPos + 1)).getEndpoints().getFirst().getId();

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
                nextId = mGraph.getEdge(fromList.get(fromPos + 1)).getEndpoints().getSecond().getId();
            else
                nextId = mGraph.getEdge(fromList.get(fromPos + 1)).getEndpoints().getFirst().getId();
        }

        ans.setFirst(dist[prevId][currFirst] + dist[currSecond][nextId] + currCost);
        ans2.setFirst(dist[prevId][currSecond] + dist[currFirst][nextId] + currCostAlt);


        //addition cost
        if(toPos == 0) {
            prevId = mGraph.getDepotId();

            if(toDir.get(toPos + 1))
                nextId = mGraph.getEdge(toList.get(toPos + 1)).getEndpoints().getSecond().getId();
            else
                nextId = mGraph.getEdge(toList.get(toPos + 1)).getEndpoints().getFirst().getId();

        } else if(toPos == toList.size() - 1) {
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

            if(toDir.get(toPos + 1))
                nextId = mGraph.getEdge(toList.get(toPos + 1)).getEndpoints().getSecond().getId();
            else
                nextId = mGraph.getEdge(toList.get(toPos + 1)).getEndpoints().getFirst().getId();
        }

        ans.setSecond(dist[prevId][currFirst] + dist[currSecond][nextId] + currCost);
        ans2.setSecond(dist[prevId][currSecond] + dist[currFirst][nextId] + currCostAlt);

        //TODO: we need to pick the better direction, which will depend on objective function type
        if(!isDirected) {
            if(Math.max(ans.getFirst(), ans.getSecond()) < Math.max(ans2.getFirst(), ans2.getSecond())) {
                move.setPrudentDirection(true);
                return ans;
            }
            else {
                move.setPrudentDirection(false);
                return ans2;
            }
        }


        return ans;
    }

}
