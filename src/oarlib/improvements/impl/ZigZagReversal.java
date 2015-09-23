package oarlib.improvements.impl;

import gnu.trove.TIntArrayList;
import oarlib.core.Graph;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.ZigZagGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.ZigZagLink;
import oarlib.problem.impl.ProblemAttributes;
import oarlib.route.util.RouteExpander;
import oarlib.vertex.impl.ZigZagVertex;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by oliverlum on 8/22/15.
 */
public class ZigZagReversal extends IntraRouteImprovementProcedure<ZigZagVertex, ZigZagLink, ZigZagGraph> {

    private static final Logger LOGGER = Logger.getLogger(Reversal.class);

    public ZigZagReversal(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> problem) {
        super(problem);
    }

    public ZigZagReversal(Problem<ZigZagVertex, ZigZagLink, ZigZagGraph> problem, Collection<Route<ZigZagVertex, ZigZagLink>> initialSol) {
        super(problem, null, initialSol);
    }

    @Override
    public Route<ZigZagVertex, ZigZagLink> improveRoute(Route<ZigZagVertex, ZigZagLink> r) {

        TIntArrayList compactRoute = r.getCompactRepresentation();
        DirectedGraph optimalDirection = constructDirDAG(r, compactRoute);

        int m = compactRoute.size();
        ArrayList<Boolean> newDirection = determineDirection(optimalDirection, m);

        Route ret = reconstructRoute(newDirection, compactRoute);

        LOGGER.debug("Original route cost: " + r.getCost());
        LOGGER.debug("New route cost: " + ret.getCost());

        return ret;
    }

    private DirectedGraph constructDirDAG(Route<ZigZagVertex, ZigZagLink> r, TIntArrayList flattenedRoute) {

        //TODO: price the penalties
        ZigZagGraph g = mProblem.getGraph();
        int mReq = flattenedRoute.size();

        //initialize sol.
        int numVertices = 8 * mReq + 2;
        DirectedGraph ans = new DirectedGraph(numVertices);

        int n = g.getVertices().size();

        //calculate shortest paths
        int[][] dist = new int[n + 1][n + 1];
        int[][] path = new int[n + 1][n + 1];

        CommonAlgorithms.fwLeastCostPaths(g, dist, path);

        //self-distances need to be zero
        for (int i = 1; i <= n; i++) {
            if (dist[i][i] > 0)
                dist[i][i] = 0;
        }

        try {
            //add the depot conns.
            ZigZagLink tempLink, tempLink2;
            int fromFirstId, fromSecondId, toFirstId, toSecondId, base, base2;

            tempLink = g.getEdge(flattenedRoute.get(0));
            toFirstId = tempLink.getFirstEndpointId();
            toSecondId = tempLink.getSecondEndpointId();
            ans.addEdge(1, 2, dist[g.getDepotId()][toFirstId]); //meander forward
            ans.addEdge(1, 3, dist[g.getDepotId()][toFirstId]); //traverse forward
            ans.addEdge(1, 4, dist[g.getDepotId()][toSecondId]); //meander backward
            ans.addEdge(1, 5, dist[g.getDepotId()][toSecondId]); //traverse backward

            base = 2;
            for (int i = 0; i < mReq - 1; i++) {

                tempLink = g.getEdge(flattenedRoute.get(i));
                fromFirstId = tempLink.getFirstEndpointId();
                fromSecondId = tempLink.getSecondEndpointId();

                //add the traversal cost (meander or service)
                ans.addEdge(base, base + 4, (int) tempLink.getZigzagCost() + tempLink.getCost());
                ans.addEdge(base + 1, base + 5, tempLink.getCost() + tempLink.getServiceCost() + tempLink.getReverseCost() + tempLink.getReverseServiceCost() + dist[fromFirstId][fromSecondId]);
                ans.addEdge(base + 2, base + 6, (int) tempLink.getZigzagCost() + tempLink.getReverseCost());
                ans.addEdge(base + 3, base + 7, tempLink.getCost() + tempLink.getServiceCost() + tempLink.getReverseCost() + tempLink.getReverseServiceCost() + dist[fromSecondId][fromFirstId]);

                base += 4;

                //connect to the next set
                tempLink2 = g.getEdge(flattenedRoute.get(i + 1));
                toFirstId = tempLink2.getFirstEndpointId();
                toSecondId = tempLink2.getSecondEndpointId();
                for (int j = base; j < base + 2; j++) {
                    ans.addEdge(j, base + 4, dist[fromSecondId][toFirstId]);
                    ans.addEdge(j, base + 5, dist[fromSecondId][toFirstId]);
                    ans.addEdge(j, base + 6, dist[fromSecondId][toSecondId]);
                    ans.addEdge(j, base + 7, dist[fromSecondId][toSecondId]);
                }
                for (int j = base + 3; j < base + 4; j++) {
                    ans.addEdge(j, base + 4, dist[fromFirstId][toFirstId]);
                    ans.addEdge(j, base + 5, dist[fromFirstId][toFirstId]);
                    ans.addEdge(j, base + 6, dist[fromFirstId][toSecondId]);
                    ans.addEdge(j, base + 7, dist[fromFirstId][toSecondId]);
                }

                base += 4;

            }

            //add terminal conns
            base -= 4;

            ans.addEdge(base, numVertices, dist[toSecondId][numVertices]);
            ans.addEdge(base + 1, numVertices, dist[toSecondId][numVertices]);
            ans.addEdge(base + 2, numVertices, dist[toFirstId][numVertices]);
            ans.addEdge(base + 3, numVertices, dist[toFirstId][numVertices]);


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return ans;
    }

    private ArrayList<Boolean> determineDirection(DirectedGraph g, int m) {

        ArrayList<Boolean> ans = new ArrayList<Boolean>();
        ArrayList<Boolean> meander = new ArrayList<Boolean>();

        int n = g.getVertices().size();
        int[] dist = new int[n + 1];
        int[] path = new int[n + 1];

        CommonAlgorithms.dijkstrasAlgorithm(g, 1, dist, path);

        int start, end, next;
        start = 1;
        end = n;

        boolean real = false;

        do {
            next = path[end];
            if (!real)
                continue;

            if (next % 4 == 2) { //top
                ans.add(0, true);
                meander.add(0, true);
            } else if (next % 4 == 3) { //2
                ans.add(0, true);
                meander.add(0, false);
            } else if (next % 4 == 0) { //3
                ans.add(0, false);
                meander.add(0, true);
            } else if (next % 4 == 1) { //4
                ans.add(0, false);
                meander.add(0, false);
            }

            real = !real;
        } while ((end = next) != start);

        return ans;
    }

    private Route reconstructRoute(ArrayList<Boolean> newDirection, TIntArrayList origRoute) {

        RouteExpander wre = new RouteExpander(getGraph());
        return wre.unflattenRoute(origRoute, newDirection);

    }

    @Override
    public ProblemAttributes getProblemAttributes() {
        return new ProblemAttributes(Graph.Type.WINDY, null, null, ProblemAttributes.NumDepots.SINGLE_DEPOT, ProblemAttributes.Properties.SERVICE_COSTS, ProblemAttributes.Properties.TIME_WINDOWS, ProblemAttributes.Properties.ZIGZAG_COSTS);
    }
}
