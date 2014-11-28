package oarlib.improvements.impl;

import gnu.trove.TIntArrayList;
import oarlib.core.Link;
import oarlib.core.Problem;
import oarlib.core.Route;
import oarlib.core.Vertex;
import oarlib.graph.impl.DirectedGraph;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.improvements.IntraRouteImprovementProcedure;
import oarlib.link.impl.WindyEdge;
import oarlib.problem.impl.rpp.WindyRPP;
import oarlib.route.util.RouteFlattener;
import oarlib.route.util.WindyRouteExpander;
import oarlib.vertex.impl.WindyVertex;
import org.apache.xpath.operations.Bool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by oliverlum on 11/16/14.
 */
public class Reversal extends IntraRouteImprovementProcedure<WindyVertex, WindyEdge, WindyGraph> {


    public Reversal(WindyGraph g, Collection<Route<WindyVertex, WindyEdge>> candidateRoute) {
        super(g, candidateRoute);
    }

    @Override
    protected Route improveRoute(Route r) {

        TIntArrayList flattenedRoute = RouteFlattener.flattenRoute(r);

        DirectedGraph optimalDirection = constructDirDAG(r, flattenedRoute);

        int m = flattenedRoute.size();
        ArrayList<Boolean> newDirection = determineDirection(optimalDirection, m);

        return reconstructRoute(newDirection, flattenedRoute);
    }

    private DirectedGraph constructDirDAG(Route r, TIntArrayList flattenedRoute){

        WindyGraph mGraph = getGraph();
        int n = mGraph.getVertices().size();

        //calculate shortest paths
        int[][] dist = new int[n+1][n+1];
        int[][] path = new int[n+1][n+1];

        CommonAlgorithms.fwLeastCostPaths(mGraph, dist, path);

        //check for argument legality
        int m = flattenedRoute.size();

        /*
         * In the following graph, the indexing is as follows:
         * 1 - (2m + 1): These are i1, j1, i2, j2, ..., im, jm, im+1
         * 2m + 2 - 4m + 2: These are j1', i1', j2', i2', ..., jm', im', jm+1'
         */
        DirectedGraph optimalDirection = new DirectedGraph(4 * m + 2);
        WindyEdge temp, temp2;
        int tempIndex = 1;
        int offset = 2 * m + 1;
        int currFirst, currSecond, nextFirst, nextSecond;

        try {

            for (int i = 1; i < m; i++) {

                temp = mGraph.getEdge(flattenedRoute.get(i));
                currFirst = temp.getEndpoints().getFirst().getId();
                currSecond = temp.getEndpoints().getSecond().getId();

                //add the traversal arcs
                optimalDirection.addEdge(tempIndex, tempIndex + 1, temp.getCost());
                optimalDirection.addEdge(tempIndex + offset, tempIndex + offset + 1, temp.getReverseCost());

                if(i+1 == m)
                    temp2 = mGraph.getEdge(flattenedRoute.get(0));
                else
                    temp2 = mGraph.getEdge(flattenedRoute.get(i+1));
                nextFirst = temp2.getEndpoints().getFirst().getId();
                nextSecond = temp2.getEndpoints().getSecond().getId();

                //add the shortest path arcs
                optimalDirection.addEdge(tempIndex + 1, tempIndex + offset + 2, dist[currSecond][nextFirst]);
                optimalDirection.addEdge(tempIndex + 1, tempIndex + offset + 3, dist[currSecond][nextSecond]);
                optimalDirection.addEdge(tempIndex + offset + 1, tempIndex + offset + 2, dist[currFirst][nextFirst]);
                optimalDirection.addEdge(tempIndex + offset + 1, tempIndex + offset + 3, dist[currFirst][nextSecond]);

                tempIndex += 2;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; //fail fast
        }

        return optimalDirection;
    }

    private ArrayList<Boolean> determineDirection(DirectedGraph g, int m) {

        ArrayList<Boolean> ans = new ArrayList<Boolean>();

        int n = g.getVertices().size();
        int[] dist = new int[n+1];
        int[] path = new int[n+1];
        int[] dist2 = new int[n+1];
        int[] path2 = new int[n+1];

        CommonAlgorithms.dijkstrasAlgorithm(g, 1, dist, path);
        CommonAlgorithms.dijkstrasAlgorithm(g, 2 * m + 2, dist2, path2);

        int start, end, next;
        boolean matters = true;

        //figure out which path
        if(dist[2 * m + 1] < dist2[4 * m + 2]) {
            start = 1;
            end = 2 * m + 1;
        } else {
            start = 2 * m + 2;
            end = 4 * m + 2;
        }

        //figure out the path
        int threshold = 2 * m + 1;
        do {
            next = path[end];
            if(matters) {
                if(next < threshold)
                    ans.add(true);
                else
                    ans.add(false);
            }
            matters = !matters;
        } while((end = next) != start);


        return ans;
    }

    private Route reconstructRoute(ArrayList<Boolean> newDirection, TIntArrayList origRoute) {

        WindyRouteExpander wre = new WindyRouteExpander(getGraph());
        return wre.unflattenRoute(origRoute,newDirection);

    }

    @Override
    public Problem.Type getProblemType() {
        return Problem.Type.WINDY_RURAL_POSTMAN;
    }
}