package oarlib.route.util;

import gnu.trove.TIntArrayList;
import oarlib.core.Route;
import oarlib.graph.impl.WindyGraph;
import oarlib.graph.util.CommonAlgorithms;
import oarlib.link.impl.WindyEdge;
import oarlib.route.impl.Tour;
import org.apache.log4j.Logger;

import java.util.ArrayList;

/**
 * Created by oliverlum on 11/20/14.
 */
public class WindyRouteExpander {

    private static final Logger LOGGER = Logger.getLogger(WindyRouteExpander.class);

    WindyGraph mGraph;
    int[][] dist;
    int[][] path;
    int[][] edgePath;

    public WindyRouteExpander(WindyGraph g){

        mGraph = g;

        int n = g.getVertices().size();
        dist = new int[n+1][n+1];
        path = new int[n+1][n+1];
        edgePath = new int[n+1][n+1];

        //TODO: If the graph changes after this point, we're screwed.  Either detect changes, or create finalized state
        CommonAlgorithms.fwLeastCostPaths(g, dist, path, edgePath);

    }

    public Tour unflattenRoute(TIntArrayList flattenedRoute, ArrayList<Boolean> direction) {

        //arg checking
        if(!(flattenedRoute.size() == direction.size())) {
            LOGGER.error("The flattened route and direction arrays are of different size.");
            throw new IllegalArgumentException();
        }

        Tour ans = new Tour();

        int prev = mGraph.getDepotId();
        int to, nextPrev, curr, next, end;
        WindyEdge temp;
        for(int i = 0; i < flattenedRoute.size(); i++) {
            temp = mGraph.getEdge(flattenedRoute.get(i));
            if(!temp.isRequired())
                continue;
            if(direction.get(i)) {
                to = temp.getEndpoints().getFirst().getId();
                nextPrev = temp.getEndpoints().getSecond().getId();
            }
            else {
                to = temp.getEndpoints().getSecond().getId();
                nextPrev = temp.getEndpoints().getFirst().getId();
            }

            //add the path from prev to to, and then the edge
            curr = prev;
            end = to;

            if(curr != end) {
                do {
                    next = path[curr][end];
                    ans.appendEdge(mGraph.getEdge(edgePath[curr][end]));
                } while ((curr = next) != end);
            }

            ans.appendEdge(temp);
            prev = nextPrev;

        }

        //add the path back to the depot
        curr = prev;
        end = mGraph.getDepotId();

        if(curr != end) {
            do {
                next = path[curr][end];
                ans.appendEdge(mGraph.getEdge(edgePath[curr][end]));
            } while ((curr = next) != end);
        }

        return ans;
    }

}
