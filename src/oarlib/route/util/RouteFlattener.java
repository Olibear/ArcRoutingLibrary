package oarlib.route.util;

import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
import oarlib.core.Link;
import oarlib.core.Route;
import oarlib.core.Vertex;

import java.util.List;

/**
 * Turns a route that consists of windy links into the compact Benavent representation (an ordered list of internal ids
 * that indicates the order that the required edges are traversed, (or vice versa for unflatten)
 * Created by oliverlum on 11/16/14.
 */
public class RouteFlattener {

    private RouteFlattener(){};

    public static TIntArrayList flattenRoute(Route r) {
        return flattenRoute(r, false);
    }

    public static TIntArrayList flattenRoute(Route r, boolean removeRepeats) {

        TIntArrayList ans = new TIntArrayList();

        TIntHashSet alreadyTraversed = new TIntHashSet();
        List<? extends Link<? extends Vertex>> path = r.getRoute();
        int n = path.size();
        for(int i = 0; i < n; i ++) {
            Link l = path.get(i);
            if(l.isRequired() && ! alreadyTraversed.contains(l.getId())) {
                ans.add(l.getId());
                if(removeRepeats)
                    alreadyTraversed.add(l.getId());
            }
        }

        return ans;

    }
}
